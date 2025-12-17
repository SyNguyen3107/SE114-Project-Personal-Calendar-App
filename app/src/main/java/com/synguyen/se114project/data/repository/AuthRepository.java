package com.synguyen.se114project.data.repository;

import com.google.gson.JsonObject;
import com.synguyen.se114project.BuildConfig;
import com.synguyen.se114project.data.remote.RetrofitClient;
import com.synguyen.se114project.data.remote.SupabaseService;
import com.synguyen.se114project.data.remote.response.AuthResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * AuthRepository
 * - Signup (Supabase Auth)
 * - Insert profile (public.profiles)
 */
public class AuthRepository {

    private final SupabaseService supabaseService;

    public AuthRepository() {
        supabaseService = RetrofitClient.getRetrofitInstance().create(SupabaseService.class);
    }

    // ================= CALLBACK =================
    public interface ResultCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    // ================= RESULT MODEL =================
    public static class SignUpResult {
        public final String userId;
        public final String email;
        public final String accessToken;

        public SignUpResult(String userId, String email, String accessToken) {
            this.userId = userId;
            this.email = email;
            this.accessToken = accessToken;
        }
    }

    // ================= SIGN UP FLOW =================
    /**
     * Flow:
     * 1. Gọi API Signup của Supabase Auth
     * 2. Nếu thành công -> Lấy ID user -> Gọi API Insert vào bảng 'profiles'
     */
    // ĐÃ SỬA TÊN HÀM TẠI ĐÂY:
    public void signUpAndCreateProfile(String email, String password, String fullName, ResultCallback<SignUpResult> callback) {
        JsonObject body = new JsonObject();
        body.addProperty("email", email);
        body.addProperty("password", password);

        // Gọi API Đăng ký
        supabaseService.signUpUser(BuildConfig.SUPABASE_KEY, body).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError("Sign up failed: " + response.code());
                    return;
                }

                AuthResponse authData = response.body();
                // Dùng hàm getValidAccessToken() để lấy token an toàn
                String accessToken = authData.getValidAccessToken();

                if (authData.user == null || accessToken == null) {
                    callback.onError("Please check your email to confirm registration.");
                    return;
                }

                String userId = authData.user.id;
                String userEmail = authData.user.email;

                // --- BƯỚC 2: TẠO PROFILE (public.profiles) ---
                JsonObject profileBody = new JsonObject();
                profileBody.addProperty("id", userId);
                profileBody.addProperty("full_name", fullName);
                profileBody.addProperty("email", userEmail);
                profileBody.addProperty("avatar_url", "default_avatar.png");
                profileBody.addProperty("role", "student");

                supabaseService.insertProfile(
                        BuildConfig.SUPABASE_KEY,
                        "Bearer " + accessToken,
                        profileBody
                ).enqueue(new Callback<Void>() {

                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (!response.isSuccessful()) {
                            // Nếu insert profile lỗi, vẫn báo lỗi để user biết
                            callback.onError("Insert profile failed: " + response.code());
                            return;
                        }

                        // Thành công cả 2 bước
                        callback.onSuccess(
                                new SignUpResult(userId, userEmail, accessToken)
                        );
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        callback.onError("Insert profile error: " + t.getMessage());
                    }
                });
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                callback.onError("Sign up error: " + t.getMessage());
            }
        });
    }
}
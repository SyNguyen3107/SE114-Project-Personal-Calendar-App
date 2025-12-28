package com.synguyen.se114project.data.repository;

import com.google.gson.JsonObject;
import com.synguyen.se114project.BuildConfig;
import com.synguyen.se114project.data.entity.Profile;
import com.synguyen.se114project.data.remote.RetrofitClient;
import com.synguyen.se114project.data.remote.SupabaseService;
import com.synguyen.se114project.data.remote.response.AuthResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    private final SupabaseService supabaseService;

    public AuthRepository() {
        supabaseService = RetrofitClient.getRetrofitInstance().create(SupabaseService.class);
    }

    // ================= CALLBACK =================
    // Interface chung cho cả Login và Signup
    public interface ResultCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    // ================= RESULT MODEL (Cho Signup) =================
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

    // ================= 1. LOGIN FLOW (MỚI THÊM) =================
    public void login(String email, String password, ResultCallback<AuthResponse> callback) {
        JsonObject body = new JsonObject();
        body.addProperty("email", email);
        body.addProperty("password", password);

        // Gọi API Login
        supabaseService.loginUser(body).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Trả về AuthResponse (chứa Token và User info)
                    callback.onSuccess(response.body());
                } else {
                    // Xử lý lỗi (sai pass, email không tồn tại...)
                    callback.onError("Đăng nhập thất bại (" + response.code() + "). Vui lòng kiểm tra lại Email/Pass.");
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    // ================= 2. SIGN UP FLOW (GIỮ NGUYÊN) =================
    public void signUpAndCreateProfile(String email, String password, String fullName, ResultCallback<SignUpResult> callback) {
        // Bước 1: Sign up Auth
        JsonObject body = new JsonObject();
        body.addProperty("email", email);
        body.addProperty("password", password);

        supabaseService.signUpUser( body).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError("Sign up failed: " + response.code());
                    return;
                }

                AuthResponse authData = response.body();
                // Sử dụng getAccessToken() thay vì getValidAccessToken() để khớp với model AuthResponse chuẩn
                String accessToken = authData.getAccessToken();

                // Kiểm tra null an toàn hơn
                if (authData.getUser() == null || accessToken == null) {
                    callback.onError("Vui lòng kiểm tra email để xác nhận đăng ký.");
                    return;
                }

                String userId = authData.getUserId(); // Dùng getter
                String userEmail = authData.getUser().getEmail(); // Nếu User có getEmail

                // --- BƯỚC 2: TẠO PROFILE (public.profiles) ---
                Profile profile = new Profile();
                profile.setId(userId);
                profile.setFullName(fullName);
                profile.setEmail(email);
                profile.setAvatarUrl("default_avatar.png");
                profile.setRole("student");
                String randomCode = String.valueOf(System.currentTimeMillis() % 100000000);
                profile.setUserCode("SV" + randomCode);

                supabaseService.insertProfile(
                        "Bearer " + accessToken,
                        profile
                ).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (!response.isSuccessful()) {
                            callback.onError("Tạo profile thất bại: " + response.code());
                            return;
                        }
                        // Thành công
                        callback.onSuccess(new SignUpResult(userId, email, accessToken));
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        callback.onError("Lỗi tạo profile: " + t.getMessage());
                    }
                });
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                callback.onError("Lỗi đăng ký: " + t.getMessage());
            }
        });
    }
}
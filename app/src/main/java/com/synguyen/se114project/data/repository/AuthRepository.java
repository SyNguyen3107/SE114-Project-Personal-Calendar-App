package com.synguyen.se114project.data.repository;

import com.google.gson.JsonObject;
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
        supabaseService =
                RetrofitClient.getClient().create(SupabaseService.class);
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
     * 1) Supabase Auth SignUp
     * 2) Insert row into public.profiles
     */
    public void signUpAndCreateProfile(
            String email,
            String password,
            String fullName,
            ResultCallback<SignUpResult> callback
    ) {

        // ---------- 1. SIGN UP ----------
        JsonObject signUpBody = new JsonObject();
        signUpBody.addProperty("email", email);
        signUpBody.addProperty("password", password);

        supabaseService.signUpUser(
                RetrofitClient.SUPABASE_KEY,
                signUpBody
        ).enqueue(new Callback<AuthResponse>() {

            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {

                if (!response.isSuccessful() || response.body() == null || response.body().user == null) {
                    callback.onError("Sign up failed: " + response.code());
                    return;
                }

                String userId = response.body().user.id;
                String userEmail = response.body().user.email;
                String accessToken = response.body().accessToken;

                if (userId == null || userId.isEmpty()) {
                    callback.onError("Missing userId from Supabase");
                    return;
                }

                if (accessToken == null || accessToken.isEmpty()) {
                    callback.onError("Missing access token (check email confirm setting)");
                    return;
                }

                // ---------- 2. INSERT PROFILE ----------
                JsonObject profileBody = new JsonObject();
                profileBody.addProperty("id", userId);
                profileBody.addProperty("full_name", fullName);
                profileBody.addProperty("email", userEmail);
                profileBody.addProperty("avatar_url", "default_avatar.png");

                supabaseService.insertProfile(
                        RetrofitClient.SUPABASE_KEY,
                        "Bearer " + accessToken,
                        profileBody
                ).enqueue(new Callback<Void>() {

                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (!response.isSuccessful()) {
                            callback.onError("Insert profile failed: " + response.code());
                            return;
                        }

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

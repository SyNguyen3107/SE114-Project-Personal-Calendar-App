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
                    AuthResponse authData = response.body();
                    // DEBUG: Log all token fields to diagnose refresh token issue
                    android.util.Log.d("AuthRepository", "=== LOGIN RESPONSE DEBUG ===");
                    android.util.Log.d("AuthRepository", "Root refreshToken field: " + 
                        (authData.refreshToken != null ? (authData.refreshToken.length() + " chars") : "NULL"));
                    android.util.Log.d("AuthRepository", "Session object: " + (authData.session != null ? "present" : "NULL"));
                    if (authData.session != null) {
                        android.util.Log.d("AuthRepository", "Session.refresh_token: " + 
                            (authData.session.refresh_token != null ? (authData.session.refresh_token.length() + " chars") : "NULL"));
                    }
                    String rt = authData.getRefreshToken();
                    android.util.Log.d("AuthRepository", "getRefreshToken() result: " + 
                        (rt != null ? (rt.length() + " chars, preview: " + rt.substring(0, Math.min(10, rt.length())) + "...") : "NULL"));
                    android.util.Log.d("AuthRepository", "accessToken length: " + 
                        (authData.getAccessToken() != null ? authData.getAccessToken().length() : 0));
                    android.util.Log.d("AuthRepository", "=== END DEBUG ===");
                    // Trả về AuthResponse (chứa Token và User info)
                    callback.onSuccess(authData);
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
    public void signUpAndCreateProfile(String email, String password, String fullName, String role, ResultCallback<SignUpResult> callback) {
        // Bước 1: Sign up Auth
        JsonObject body = new JsonObject();
        body.addProperty("email", email);
        body.addProperty("password", password);

        supabaseService.signUpUser( body).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    String errMsg = "Sign up failed: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            String errBody = response.errorBody().string();
                            android.util.Log.e("AuthRepository", "signUp errorBody: " + errBody);
                            try {
                                com.google.gson.JsonObject jo = com.google.gson.JsonParser.parseString(errBody).getAsJsonObject();
                                if (jo.has("msg")) errMsg = jo.get("msg").getAsString();
                                else if (jo.has("message")) {
                                    com.google.gson.JsonElement m = jo.get("message");
                                    if (m.isJsonArray()) errMsg = m.getAsJsonArray().toString();
                                    else errMsg = m.getAsString();
                                } else errMsg = errBody;
                            } catch (Exception ex) {
                                errMsg = errBody;
                            }
                        }
                    } catch (Exception e) {
                        // ignore
                    }
                    callback.onError(errMsg);
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
                profile.setRole(role == null || role.isEmpty() ? "student" : role);
                String randomCode = String.valueOf(System.currentTimeMillis() % 100000000);
                profile.setUserCode("SV" + randomCode);

                supabaseService.insertProfile(
                        "Bearer " + accessToken,
                        profile
                ).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (!response.isSuccessful()) {
                            String errMsg = "Tạo profile thất bại: " + response.code();
                            try {
                                if (response.errorBody() != null) {
                                    String errBody = response.errorBody().string();
                                    android.util.Log.e("AuthRepository", "insertProfile errorBody: " + errBody);
                                    try {
                                        com.google.gson.JsonObject jo = com.google.gson.JsonParser.parseString(errBody).getAsJsonObject();
                                        if (jo.has("message")) {
                                            com.google.gson.JsonElement m = jo.get("message");
                                            if (m.isJsonArray()) errMsg = m.getAsJsonArray().toString();
                                            else errMsg = m.getAsString();
                                        } else if (jo.has("msg")) errMsg = jo.get("msg").getAsString();
                                        else errMsg = errBody;
                                    } catch (Exception ex) {
                                        errMsg = errBody;
                                    }
                                }
                            } catch (Exception e) {
                                // ignore
                            }
                            callback.onError(errMsg);
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

    // ================= 3. REFRESH ACCESS TOKEN =================
    /**
     * Try to refresh access token using refresh_token stored in SharedPreferences.
     * On success, stores new ACCESS_TOKEN and REFRESH_TOKEN back to prefs and returns the new access token.
     */
    public void refreshAccessToken(android.content.Context ctx, ResultCallback<String> callback) {
        android.content.SharedPreferences prefs = ctx.getSharedPreferences("AppPrefs", android.content.Context.MODE_PRIVATE);
        String refreshToken = prefs.getString("REFRESH_TOKEN", null);
        if (refreshToken == null || refreshToken.isEmpty()) {
            android.util.Log.e("AuthRepository", "refreshAccessToken: no REFRESH_TOKEN in prefs");
            callback.onError("Missing refresh token");
            return;
        }

        // Supabase refresh tokens can be short (12+ chars) in newer versions
        android.util.Log.d("AuthRepository", "refreshAccessToken: using refreshToken of " + refreshToken.length() + " chars");

        supabaseService.refreshToken("refresh_token", refreshToken).enqueue(new Callback<com.google.gson.JsonObject>() {
            @Override
            public void onResponse(Call<com.google.gson.JsonObject> call, Response<com.google.gson.JsonObject> response) {
                android.util.Log.d("AuthRepository", "refreshAccessToken: response code=" + response.code());
                try {
                    if (!response.isSuccessful()) {
                        String err = "";
                        try { if (response.errorBody() != null) err = response.errorBody().string(); } catch (Exception ex) { err = "<no body>"; }
                        android.util.Log.e("AuthRepository", "refreshAccessToken failed: " + err);
                        
                        // On 400/401, the refresh token is invalid - clear all tokens to force re-login
                        if (response.code() == 400 || response.code() == 401) {
                            android.util.Log.w("AuthRepository", "Refresh token rejected. Clearing all tokens.");
                            prefs.edit()
                                .remove("ACCESS_TOKEN")
                                .remove("REFRESH_TOKEN")
                                .remove("USER_ID")
                                .remove("USER_ROLE")
                                .apply();
                        }
                        callback.onError("Failed to refresh token: " + response.code());
                        return;
                    }

                    com.google.gson.JsonObject jo = response.body();
                    android.util.Log.d("AuthRepository", "refreshAccessToken success body=" + (jo == null ? "null" : jo.toString()));
                    String newAccess = jo.has("access_token") ? jo.get("access_token").getAsString() : null;
                    String newRefresh = jo.has("refresh_token") ? jo.get("refresh_token").getAsString() : null;
                    if (newAccess == null) {
                        android.util.Log.e("AuthRepository", "refreshAccessToken: missing access_token in body");
                        callback.onError("No access_token in refresh response");
                        return;
                    }
                    // Save new tokens
                    prefs.edit().putString("ACCESS_TOKEN", newAccess).apply();
                    if (newRefresh != null) prefs.edit().putString("REFRESH_TOKEN", newRefresh).apply();
                    android.util.Log.d("AuthRepository", "refreshAccessToken: stored new access token (masked): " + (newAccess.length()>8 ? newAccess.substring(0,4)+"..."+newAccess.substring(newAccess.length()-4) : newAccess));
                    callback.onSuccess(newAccess);
                } catch (Exception e) {
                    android.util.Log.e("AuthRepository", "refreshAccessToken parse error", e);
                    callback.onError("Refresh parse error: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<com.google.gson.JsonObject> call, Throwable t) {
                android.util.Log.e("AuthRepository", "refreshAccessToken network failure", t);
                callback.onError("Refresh failed: " + t.getMessage());
            }
        });
    }
}
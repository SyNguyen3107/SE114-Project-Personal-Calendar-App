package com.synguyen.se114project.data.remote.response;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {
    // Dùng cho API Login (Token nằm ở root)
    @SerializedName("access_token")
    public String accessToken;

    @SerializedName("token_type")
    public String tokenType;

    @SerializedName("user")
    public UserObj user;

    // THÊM: Dùng cho API Sign-up (Token nằm trong session)
    @SerializedName("session")
    public SessionObj session;

    // --- CÁC CLASS CON ---

    public static class UserObj {
        @SerializedName("id")
        public String id;

        @SerializedName("email")
        public String email;
    }

    public static class SessionObj {
        @SerializedName("access_token")
        public String access_token;

        @SerializedName("token_type")
        public String token_type;

        @SerializedName("user")
        public UserObj user;
    }

    // Helper method để lấy Token dù là Login hay Signup
    public String getValidAccessToken() {
        if (accessToken != null && !accessToken.isEmpty()) {
            return accessToken;
        }
        if (session != null && session.access_token != null) {
            return session.access_token;
        }
        return null;
    }
}
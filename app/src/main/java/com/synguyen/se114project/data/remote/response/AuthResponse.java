package com.synguyen.se114project.data.remote.response;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {
    // 1. Trường hợp Login: Token & User nằm ngay ở root
    @SerializedName("access_token")
    public String accessToken;

    @SerializedName("token_type")
    public String tokenType;

    @SerializedName("user")
    public UserObj user;

    // 2. Trường hợp Sign-up: Token & User nằm trong object session
    @SerializedName("session")
    public SessionObj session;

    // --- CÁC CLASS CON (Inner Classes) ---

    public static class UserObj {
        @SerializedName("id")
        public String id;

        @SerializedName("email")
        public String email;

        // Getter cho UserObj (để AuthRepository gọi .getEmail())
        public String getEmail() {
            return email;
        }

        public String getId() {
            return id;
        }
    }

    public static class SessionObj {
        @SerializedName("access_token")
        public String access_token;

        @SerializedName("token_type")
        public String token_type;

        @SerializedName("user")
        public UserObj user;
    }

    // --- HELPER METHODS (QUAN TRỌNG) ---

    // 1. Hàm getUser() mà AuthRepository đang cần gọi
    public UserObj getUser() {
        // Ưu tiên 1: Tìm User ở root (Login)
        if (user != null) {
            return user;
        }
        // Ưu tiên 2: Tìm User trong session (Signup)
        if (session != null && session.user != null) {
            return session.user;
        }
        return null;
    }

    // 2. Lấy Token an toàn
    public String getAccessToken() {
        if (accessToken != null && !accessToken.isEmpty()) {
            return accessToken;
        }
        if (session != null && session.access_token != null) {
            return session.access_token;
        }
        return null;
    }

    // 3. Lấy User ID an toàn (Rút gọn cho Repository)
    public String getUserId() {
        UserObj u = getUser();
        return (u != null) ? u.id : null;
    }
}
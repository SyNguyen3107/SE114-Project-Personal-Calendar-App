package com.synguyen.se114project.data.remote.response;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {
    @SerializedName("access_token")
    public String accessToken;

    @SerializedName("token_type")
    public String tokenType;

    @SerializedName("user")
    public UserObj user;

    public static class UserObj {
        @SerializedName("id")
        public String id;

        @SerializedName("email")
        public String email;
    }
}
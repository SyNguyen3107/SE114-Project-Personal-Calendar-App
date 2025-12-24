package com.synguyen.se114project.data.entity;

import com.google.gson.annotations.SerializedName;
import org.json.JSONObject;
import java.io.Serializable; // Thêm thư viện này để truyền Object qua Intent

public class Profile implements Serializable {
    @SerializedName("id")
    private String id;          // UUID from auth.users.id

    @SerializedName("full_name")
    private String fullName;

    @SerializedName("email")
    private String email;

    @SerializedName("avatar_url")
    private String avatarUrl;

    @SerializedName("role")
    private String role;        // "student" hoặc "teacher"

    // --- MỚI THÊM: Mã định danh (MSSV/Mã GV) ---
    @SerializedName("user_code")
    private String userCode;

    // Constructor rỗng
    public Profile() {}

    // Constructor đầy đủ
    public Profile(String id, String fullName, String email, String avatarUrl, String role, String userCode) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.role = role;
        this.userCode = userCode;
    }

    // ---------- Getters / Setters ----------
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    // Getter & Setter cho User Code
    public String getUserCode() { return userCode; }
    public void setUserCode(String userCode) { this.userCode = userCode; }

    // ---------- JSON Helpers (Đã cập nhật thêm user_code) ----------

    // Convert Profile -> JSON
    public JSONObject toJsonForUpsert() {
        JSONObject obj = new JSONObject();
        try {
            if (id != null) obj.put("id", id);
            if (fullName != null) obj.put("full_name", fullName);
            if (email != null) obj.put("email", email);
            if (avatarUrl != null) obj.put("avatar_url", avatarUrl);
            if (role != null) obj.put("role", role);

            // Thêm user_code vào JSON gửi đi
            if (userCode != null) obj.put("user_code", userCode);
        } catch (Exception ignored) {}
        return obj;
    }

    // Convert JSON -> Profile
    public static Profile fromJson(JSONObject obj) {
        if (obj == null) return null;

        Profile p = new Profile();
        p.id = obj.optString("id", null);
        p.fullName = obj.optString("full_name", null);
        p.email = obj.optString("email", null);
        p.avatarUrl = obj.optString("avatar_url", null);
        p.role = obj.optString("role", "student");

        // Đọc user_code từ JSON trả về
        p.userCode = obj.optString("user_code", null);
        return p;
    }
}
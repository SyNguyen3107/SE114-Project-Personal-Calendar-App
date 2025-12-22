package com.synguyen.se114project.data.entity;

import com.google.gson.annotations.SerializedName; // Cần import thư viện này
import org.json.JSONObject;

public class Profile {
    @SerializedName("id")
    private String id;          // UUID from auth.users.id

    @SerializedName("full_name") // Map JSON "full_name" -> Java "fullName"
    private String fullName;

    @SerializedName("email")
    private String email;       // optional

    @SerializedName("avatar_url") // Map JSON "avatar_url" -> Java "avatarUrl"
    private String avatarUrl;

    @SerializedName("role")     // Thêm field Role
    private String role;        // "student" hoặc "teacher"

    public Profile() {}

    public Profile(String id, String fullName, String email, String avatarUrl, String role) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.role = role;
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

    // --- MỚI: Getter & Setter cho Role ---
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    // ---------- JSON Helpers (Giữ lại để hỗ trợ code cũ nếu cần) ----------
    // Convert Profile -> JSON for insert/update Supabase
    public JSONObject toJsonForUpsert() {
        JSONObject obj = new JSONObject();
        try {
            if (id != null) obj.put("id", id);
            if (fullName != null) obj.put("full_name", fullName);
            if (email != null) obj.put("email", email);
            if (avatarUrl != null) obj.put("avatar_url", avatarUrl);
            if (role != null) obj.put("role", role); // Thêm role vào JSON
        } catch (Exception ignored) {}
        return obj;
    }

    // Convert JSON -> Profile (from Supabase response)
    public static Profile fromJson(JSONObject obj) {
        if (obj == null) return null;

        Profile p = new Profile();
        p.id = obj.optString("id", null);
        p.fullName = obj.optString("full_name", null);
        p.email = obj.optString("email", null);
        p.avatarUrl = obj.optString("avatar_url", null);
        p.role = obj.optString("role", "student"); // Thêm role (mặc định student)
        return p;
    }
}
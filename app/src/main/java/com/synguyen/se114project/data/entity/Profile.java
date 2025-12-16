package com.synguyen.se114project.data.entity;

import org.json.JSONObject;

public class Profile {
    private String id;          // UUID from auth.users.id
    private String fullName;    // maps to full_name
    private String email;       // optional
    private String avatarUrl;   // maps to avatar_url

    public Profile() {}

    public Profile(String id, String fullName, String email, String avatarUrl) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.avatarUrl = avatarUrl;
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

    // ---------- JSON Helpers ----------
    // Convert Profile -> JSON for insert/update Supabase
    public JSONObject toJsonForUpsert() {
        JSONObject obj = new JSONObject();
        try {
            if (id != null) obj.put("id", id);
            if (fullName != null) obj.put("full_name", fullName);
            if (email != null) obj.put("email", email);
            if (avatarUrl != null) obj.put("avatar_url", avatarUrl);
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
        return p;
    }
}

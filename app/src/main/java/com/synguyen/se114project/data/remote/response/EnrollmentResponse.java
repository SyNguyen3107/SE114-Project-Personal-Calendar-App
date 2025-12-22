package com.synguyen.se114project.data.remote.response;

import com.google.gson.annotations.SerializedName;
import com.synguyen.se114project.data.entity.Profile;

public class EnrollmentResponse {
    // Tên trường này phải khớp với tên bảng được join trong câu query select=profiles(*)
    @SerializedName("profiles")
    private Profile profile;

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }
}
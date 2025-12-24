package com.synguyen.se114project.data.entity;

import com.google.gson.annotations.SerializedName;

public class Enrollment {
    // ID của dòng ghi danh (tự sinh)
    @SerializedName("id")
    private String id;

    // ID khóa học
    @SerializedName("course_id")
    private String courseId;

    // ID sinh viên (Trong ảnh của bạn là user_id)
    @SerializedName("user_id")
    private String userId;

    // Constructor
    public Enrollment(String courseId, String userId) {
        this.courseId = courseId;
        this.userId = userId;
    }

    // Getters
    public String getId() { return id; }
    public String getCourseId() { return courseId; }
    public String getUserId() { return userId; }
}
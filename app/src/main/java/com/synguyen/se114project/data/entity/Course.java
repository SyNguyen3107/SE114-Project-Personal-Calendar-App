package com.synguyen.se114project.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.UUID;

// Đổi tên bảng thành "course_table" cho đồng bộ
@Entity(tableName = "course_table")
public class Course {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    public String id;

    @ColumnInfo(name = "name")
    public String name; // Tên môn

    @ColumnInfo(name = "description")
    public String description; // Mô tả ngắn

    @ColumnInfo(name = "teacher_name")
    public String teacherName; // Tên giáo viên

    @ColumnInfo(name = "time_slot")
    public String timeSlot; // Giờ học

    @ColumnInfo(name = "date_info")
    public String dateInfo;

    @ColumnInfo(name = "color_hex")
    public String colorHex; // Mã màu nền (VD: #2196F3)

    // --- SYNC FIELDS ---
    @ColumnInfo(name = "owner_id")
    public String ownerId;

    @ColumnInfo(name = "is_synced")
    public boolean isSynced;

    @ColumnInfo(name = "is_deleted")
    public boolean isDeleted;

    @ColumnInfo(name = "last_updated")
    public long lastUpdated;

    // Constructor mặc định
    public Course() {
        this.id = UUID.randomUUID().toString();
        this.isSynced = false;
        this.isDeleted = false;
        this.lastUpdated = System.currentTimeMillis();
        this.colorHex = "#2196F3"; // Mặc định màu xanh Blue
    }

    @Ignore
    public Course(String name, String description, String teacherName, String timeSlot, String dateInfo, String colorHex) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.teacherName = teacherName;
        this.timeSlot = timeSlot;
        this.dateInfo = dateInfo;
        this.colorHex = colorHex;

        this.isSynced = false;
        this.isDeleted = false;
        this.lastUpdated = System.currentTimeMillis();
    }

    // Getter & Setter
    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }

    public String getDateInfo() { return dateInfo; }
    public void setDateInfo(String dateInfo) { this.dateInfo = dateInfo; }

    public String getColorHex() { return colorHex; }
    public void setColorHex(String colorHex) { this.colorHex = colorHex; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public boolean isSynced() { return isSynced; }
    public void setSynced(boolean synced) { isSynced = synced; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    public long getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(long lastUpdated) { this.lastUpdated = lastUpdated; }
}
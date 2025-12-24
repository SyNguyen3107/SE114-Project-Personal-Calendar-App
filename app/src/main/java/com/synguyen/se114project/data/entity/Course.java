package com.synguyen.se114project.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName; // 1. Thêm Import này

import java.io.Serializable; // Nên implement Serializable để truyền qua Intent
import java.util.UUID;

@Entity(tableName = "courses")
public class Course implements Serializable {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    @SerializedName("id") // Map với cột "id" trên Supabase
    public String id;

    @ColumnInfo(name = "name")
    @SerializedName("name") // Map với cột "name"
    public String name;

    @ColumnInfo(name = "description")
    @SerializedName("description") // Map với cột "description"
    public String description;

    @ColumnInfo(name = "teacher_name")
    @SerializedName("teacher_name") // QUAN TRỌNG: Java là teacherName -> JSON là teacher_name
    public String teacherName;

    @ColumnInfo(name = "time_slot")
    @SerializedName("time_slot")
    public String timeSlot;

    @ColumnInfo(name = "date_info")
    @SerializedName("date_info")
    public String dateInfo;

    @ColumnInfo(name = "color_hex")
    @SerializedName("color_hex")
    public String colorHex;
    @SerializedName("student_count") // Tên cột trong View SQL
    private int studentCount;

    // --- SYNC FIELDS ---

    @ColumnInfo(name = "teacher_id") // Tên cột trong Local DB (Room)
    @SerializedName("teacher_id")    // Tên cột trên Server (Supabase) - QUAN TRỌNG
    public String teacherId;         // Tên biến trong Java

    @ColumnInfo(name = "is_deleted")
    @SerializedName("is_deleted") // <--- THÊM DÒNG NÀY
    public boolean isDeleted;

    // 2. SỬA LUÔN lastUpdated (Nếu không sẽ bị lỗi tương tự sau này)
    @ColumnInfo(name = "last_updated")
    @SerializedName("last_updated") // <--- THÊM DÒNG NÀY
    public long lastUpdated;

    // 3. Xử lý isSynced (Trường này chỉ dùng ở Local, không nên gửi lên Server)
    @ColumnInfo(name = "is_synced")
    // Không thêm SerializedName, nhưng nên dùng transient để GSON bỏ qua nó khi gửi API
    public transient boolean isSynced;

    // --- CONSTRUCTOR ---
    public Course() {
        this.id = UUID.randomUUID().toString();
        this.isSynced = false;
        this.isDeleted = false;
        this.lastUpdated = System.currentTimeMillis();
        this.colorHex = "#2196F3";
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

    // --- GETTER & SETTER (Giữ nguyên) ---
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

    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public boolean isSynced() { return isSynced; }
    public void setSynced(boolean synced) { isSynced = synced; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    public long getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(long lastUpdated) { this.lastUpdated = lastUpdated; }
    public int getStudentCount() {
        return studentCount;
    }
    public void setStudentCount(int studentCount) {
        this.studentCount = studentCount;
    }
}
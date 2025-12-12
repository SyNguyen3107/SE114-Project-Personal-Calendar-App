package com.synguyen.se114project.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity(tableName = "task_table")
public class Task {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    public String id;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "date")
    public long date;

    @ColumnInfo(name = "priority")
    public int priority;

    @ColumnInfo(name = "tag")
    public String tag;

    @ColumnInfo(name = "subtitle")
    public String subtitle;

    @ColumnInfo(name = "time")
    public String time;

    @ColumnInfo(name = "is_completed")
    public boolean isCompleted;

    // --- LMS Fields ---
    @ColumnInfo(name = "course_id")
    public String courseId;

    @ColumnInfo(name = "attachment_url")
    public String attachmentUrl;

    // --- MỚI: THỜI LƯỢNG (Duration) ---
    // Lưu thời gian dự kiến hoàn thành Task (tính bằng mili giây)
    @ColumnInfo(name = "duration")
    public long duration;

    // --- Sync Fields ---
    @ColumnInfo(name = "owner_id")
    public String ownerId;

    @ColumnInfo(name = "is_synced")
    public boolean isSynced;

    @ColumnInfo(name = "is_deleted")
    public boolean isDeleted;

    @ColumnInfo(name = "last_updated")
    public long lastUpdated;

    public Task() {
        this.id = UUID.randomUUID().toString();
        this.isCompleted = false;
        this.isSynced = false;
        this.isDeleted = false;
        this.lastUpdated = System.currentTimeMillis();
        this.courseId = null;
        this.attachmentUrl = null;
        this.duration = 25 * 60 * 1000; // Mặc định 25 phút nếu không nhập
    }

    @Ignore
    public Task(String title, String subtitle, long date, String time, String tag, int priority) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.subtitle = subtitle;
        this.date = date;
        this.time = time;
        this.tag = tag;
        this.priority = priority;

        this.isCompleted = false;
        this.isSynced = false;
        this.isDeleted = false;
        this.lastUpdated = System.currentTimeMillis();

        this.courseId = null;
        this.attachmentUrl = null;
        this.duration = 25 * 60 * 1000; // Mặc định 25 phút
    }

    // --- Getter và Setter ---
    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public long getDate() { return date; }
    public void setDate(long date) { this.date = date; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public String getSubTitle() { return subtitle; }
    public void setSubTitle(String subtitle) { this.subtitle = subtitle; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public String getcourseId() { return courseId; }
    public void setcourseId(String courseId) { this.courseId = courseId; }

    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }

    // Getter/Setter cho Duration
    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public boolean isSynced() { return isSynced; }
    public void setSynced(boolean synced) { isSynced = synced; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    public long getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(long lastUpdated) { this.lastUpdated = lastUpdated; }
}
package com.synguyen.se114project.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "task_table")
public class Task {
    @PrimaryKey(autoGenerate = true)
    public long id;

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
    public String time; // Giữ nguyên String để hiển thị giờ (VD: "14:30"), hoặc đổi sang long nếu muốn sắp xếp theo giờ

    // 1. Constructor mặc định (Bắt buộc cho Room)
    public Task() {
    }

    // 2. Constructor tiện lợi (Dùng để tạo Task mới - KHÔNG truyền ID vì nó tự tăng)
    @Ignore // Báo cho Room bỏ qua constructor này, chỉ dùng constructor mặc định
    public Task(String title, String subtitle, long date, String time, String tag, int priority) {
        this.title = title;
        this.subtitle = subtitle;
        this.date = date; // Nhận vào long
        this.time = time;
        this.tag = tag;
        this.priority = priority;
    }

    // --- Getter và Setter ---

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
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
}
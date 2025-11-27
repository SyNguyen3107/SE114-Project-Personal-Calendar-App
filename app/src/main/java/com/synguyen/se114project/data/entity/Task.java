package com.synguyen.se114project.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
@Entity(tableName = "task_table")
public class Task {
    @PrimaryKey(autoGenerate = true)
    public int id;
    @ColumnInfo(name = "title")
    public String title;
    @ColumnInfo(name = "date")
    public long date;
    // Constructor mặc định (Room cần cái này)
    public Task() {
    }
    // Constructor tiện lợi để tạo object nhanh
    public Task(String title, long date) {
        this.title = title;
        this.date = date;
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}

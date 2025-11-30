package com.synguyen.se114project.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
@Entity(tableName = "task_table")
public class Task {
    @PrimaryKey(autoGenerate = true)
    public long id;
    @ColumnInfo(name = "title")
    public String title;
    @ColumnInfo(name = "date")
    public String date;
    @ColumnInfo(name = "priority")
    public int priority;
    @ColumnInfo(name = "tag")
    public String tag;
    @ColumnInfo(name = "subtitle")
    public String subtitle;
    @ColumnInfo(name = "time")
    public String time;

    public Task() {
    }
    public Task( long id,String date,String title,String subtitle , String time, String tag, int priority) {
        this.title = title;
        this.id = id;
        this.subtitle = subtitle;
        this.time = time;
        this.tag = tag;
        this.priority = priority;
        this.date=date;
    }
    public long getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public String getSubTitle() { return subtitle; }
    public void setSubTitle(String subtitle) { this.subtitle = subtitle; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }



    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}

package com.synguyen.se114project.data.entity; // Nhớ sửa package

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "subtask_table",
        foreignKeys = @ForeignKey(entity = Task.class,
                parentColumns = "id",
                childColumns = "task_id",
                onDelete = CASCADE)) // Xóa Task cha thì Subtask con tự mất
public class Subtask {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "task_id", index = true) // Cần đánh index cho khóa ngoại
    public int taskId;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "is_completed")
    public boolean isCompleted;

    // Constructor
    public Subtask(int taskId, String title) {
        this.taskId = taskId;
        this.title = title;
        this.isCompleted = false;
    }
}
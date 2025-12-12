package com.synguyen.se114project.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.UUID;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "subtask_table",
        foreignKeys = @ForeignKey(entity = Task.class,
                parentColumns = "id",     // Cột id bên Task (String)
                childColumns = "task_id", // Cột task_id bên Subtask (String)
                onDelete = CASCADE))      // Xóa cứng Task cha -> Xóa cứng Subtask con (Local)
public class Subtask {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    public String id;

    @ColumnInfo(name = "task_id", index = true)
    public String taskId; // QUAN TRỌNG: Đã đổi int -> String để khớp với Task cha

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "is_completed")
    public boolean isCompleted;

    // --- Thêm trường hỗ trợ Sync ---
    // Subtask cũng cần biết nó đã được đồng bộ hay chưa
    @ColumnInfo(name = "is_synced")
    public boolean isSynced;

    @ColumnInfo(name = "is_deleted")
    public boolean isDeleted; // Để hỗ trợ xóa mềm từng subtask riêng lẻ

    // 1. Constructor mặc định (Bắt buộc cho Room)
    public Subtask() {
        this.id = UUID.randomUUID().toString();
        this.isSynced = false;
        this.isDeleted = false;
    }

    // 2. Constructor tiện lợi
    @Ignore
    public Subtask(String taskId, String title) {
        this.id = UUID.randomUUID().toString(); // Tự sinh UUID
        this.taskId = taskId;
        this.title = title;
        this.isCompleted = false; // Mặc định chưa hoàn thành

        this.isSynced = false;
        this.isDeleted = false;
    }

    // --- Getter & Setter (Cần thiết) ---
    // Lưu ý: Các getter/setter id giờ trả về String

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public boolean isSynced() { return isSynced; }
    public void setSynced(boolean synced) { isSynced = synced; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }
}
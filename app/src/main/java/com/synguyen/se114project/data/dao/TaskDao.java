package com.synguyen.se114project.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.synguyen.se114project.data.entity.Subtask;
import com.synguyen.se114project.data.entity.Task;

import java.util.List;

@Dao
public interface TaskDao {

    // --- WRITE ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertTask(Task task);

    @Update
    void updateTask(Task task);

    @Delete
    void deletePhysicalTask(Task task);

    @Query("UPDATE task_table SET is_deleted = 1, is_synced = 0, last_updated = :currentTime WHERE id = :taskId")
    void softDeleteTask(String taskId, long currentTime);

    // --- READ ---

    // 1. Lấy tất cả (trừ soft deleted)
    @Query("SELECT * FROM task_table WHERE is_deleted = 0 ORDER BY date DESC, time DESC")
    LiveData<List<Task>> getAllTasks();

    // 2. Tìm theo ngày
    @Query("SELECT * FROM task_table WHERE date >= :startOfDay AND date <= :endOfDay AND is_deleted = 0 ORDER BY priority DESC")
    LiveData<List<Task>> getTasksByDateRange(long startOfDay, long endOfDay);

    // 3. Lấy Task theo ID
    @Query("SELECT * FROM task_table WHERE id = :taskId LIMIT 1")
    LiveData<Task> getTaskById(String taskId);

    // 4. MỚI: Lấy danh sách Task của một Môn học cụ thể
    @Query("SELECT * FROM task_table WHERE course_id = :courseId AND is_deleted = 0 ORDER BY date ASC")
    LiveData<List<Task>> getTasksByCourseId(String courseId);

    // --- SYNC ---
    @Query("SELECT * FROM task_table WHERE is_synced = 0")
    List<Task> getUnsyncedTasks();

    @Query("UPDATE task_table SET is_synced = 1 WHERE id = :taskId")
    void markTaskAsSynced(String taskId);

    // --- SUBTASK ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSubtask(Subtask subtask);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSubtasks(List<Subtask> subtasks);

    @Update
    void updateSubtask(Subtask subtask);

    @Delete
    void deleteSubtask(Subtask subtask);

    @Query("SELECT * FROM subtask_table WHERE task_id = :taskId")
    LiveData<List<Subtask>> getSubtasksOfTask(String taskId);

    @Query("UPDATE subtask_table SET is_deleted = 1, is_synced = 0 WHERE task_id = :taskId")
    void softDeleteSubtasksOfTask(String taskId);
}
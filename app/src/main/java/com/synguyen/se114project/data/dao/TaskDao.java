package com.synguyen.se114project.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.synguyen.se114project.data.entity.Subtask;
import com.synguyen.se114project.data.entity.Task;

import java.util.List;
@Dao
public interface TaskDao {
    @Insert
    void insertTask(Task task);

    @Update
    void updateTask(Task task);

    @Delete
    void deleteTask(Task task);

    @Query("SELECT * FROM task_table ORDER BY time DESC")
    LiveData<List<Task>> getAllTasks();

    // Phần SUBTASK
    @Insert
    void insertSubtask(Subtask subtask);

    @Update
    void updateSubtask(Subtask subtask);

    @Delete
    void deleteSubtask(Subtask subtask);

    // Lấy danh sách Subtask của một Task cụ thể
    @Query("SELECT * FROM subtask_table WHERE task_id = :taskId")
    LiveData<List<Subtask>> getSubtasksOfTask(int taskId);
}

package com.synguyen.se114project.repository;
import android.app.Application;

import androidx.lifecycle.LiveData;

import com.synguyen.se114project.data.entity.Subtask;
import com.synguyen.se114project.data.entity.Task;
import com.synguyen.se114project.data.dao.TaskDao;
import com.synguyen.se114project.database.AppDatabase;

import java.util.List;
public class TaskRepository {

    private TaskDao mTaskDao;
    private LiveData<List<Task>> mAllTasks;

    // Constructor: Khởi tạo Repository
    public TaskRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mTaskDao = db.taskDao();
        mAllTasks = mTaskDao.getAllTasks();
    }
    // Hàm lấy danh sách Task (Trả về LiveData để UI tự cập nhật)
    public LiveData<List<Task>> getAllTasks() {
        return mAllTasks;
    }
    // Hàm thêm Task (Chạy dưới background để không đơ máy)
    public void insert(Task task) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mTaskDao.insertTask(task);
        });
    }

    // Hàm xóa Task
    public void delete(Task task) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mTaskDao.deleteTask(task);
        });
    }

    // Hàm cập nhật Task
    public void update(Task task) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mTaskDao.updateTask(task);
        });
    }
    // --- Các hàm cho SUBTASK ---

    // Lấy Subtask (LiveData)
    public LiveData<List<Subtask>> getSubtasksOfTask(int taskId) {
        return mTaskDao.getSubtasksOfTask(taskId);
    }

    public void insertSubtask(Subtask subtask) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mTaskDao.insertSubtask(subtask);
        });
    }

    public void updateSubtask(Subtask subtask) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mTaskDao.updateSubtask(subtask);
        });
    }

    public void deleteSubtask(Subtask subtask) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mTaskDao.deleteSubtask(subtask);
        });
    }
}

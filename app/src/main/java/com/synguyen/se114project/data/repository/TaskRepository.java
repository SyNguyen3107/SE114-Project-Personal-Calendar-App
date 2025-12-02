package com.synguyen.se114project.data.repository;
import android.app.Application;
import java.util.Calendar;
import androidx.lifecycle.LiveData;

import com.synguyen.se114project.data.entity.Subtask;
import com.synguyen.se114project.data.entity.Task;
import com.synguyen.se114project.data.dao.TaskDao;
import com.synguyen.se114project.data.database.AppDatabase;

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
    /**
     * Hàm Combo: Lưu Task cha trước, lấy ID, rồi lưu Subtasks
     */
    public void insertTaskWithSubtasks(Task task, List<Subtask> subtasks) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // 1. Lưu Task cha và lấy ID trả về
            long taskId = mTaskDao.insertTask(task);

            // 2. Gán ID đó cho tất cả Subtask con
            for (Subtask sub : subtasks) {
                sub.taskId = (int) taskId; // Ép kiểu long về int
            }

            // 3. Lưu danh sách Subtask

            mTaskDao.insertSubtasks(subtasks);

        });
    }
    // Hàm API để ViewModel gọi
    public LiveData<List<Task>> getTasksByDate(long dateTimestamp) {
        long start = getStartOfDay(dateTimestamp);
        long end = getEndOfDay(dateTimestamp);
        return mTaskDao.getTasksByDateRange(start, end);
    }
    // Helper: Lấy 00:00:00
    private long getStartOfDay(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    // Helper: Lấy 23:59:59
    private long getEndOfDay(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }
}

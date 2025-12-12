package com.synguyen.se114project.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.synguyen.se114project.data.dao.TaskDao;
import com.synguyen.se114project.data.database.AppDatabase;
import com.synguyen.se114project.data.entity.Subtask;
import com.synguyen.se114project.data.entity.Task;

import java.util.Calendar;
import java.util.List;

public class TaskRepository {

    private final TaskDao mTaskDao;
    private final LiveData<List<Task>> mAllTasks;

    public TaskRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mTaskDao = db.taskDao();
        mAllTasks = mTaskDao.getAllTasks();
    }

    // --- READ OPERATIONS ---

    public LiveData<List<Task>> getAllTasks() {
        return mAllTasks;
    }

    public LiveData<Task> getTaskById(String taskId) {
        return mTaskDao.getTaskById(taskId);
    }

    public LiveData<List<Task>> getTasksByDate(long dateTimestamp) {
        long start = getStartOfDay(dateTimestamp);
        long end = getEndOfDay(dateTimestamp);
        return mTaskDao.getTasksByDateRange(start, end);
    }

    // MỚI: Lấy danh sách Task theo Course ID
    public LiveData<List<Task>> getTasksByCourseId(String courseId) {
        return mTaskDao.getTasksByCourseId(courseId);
    }

    // --- WRITE OPERATIONS (TASK) ---

    public void insert(Task task) {
        AppDatabase.databaseWriteExecutor.execute(() -> mTaskDao.insertTask(task));
    }

    public void update(Task task) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            task.setLastUpdated(System.currentTimeMillis());
            task.setSynced(false);
            mTaskDao.updateTask(task);
        });
    }

    public void delete(Task task) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long currentTime = System.currentTimeMillis();
            mTaskDao.softDeleteTask(task.getId(), currentTime);
            mTaskDao.softDeleteSubtasksOfTask(task.getId());
        });
    }

    // Insert Task + Subtasks (Logic cho AddEditFragment)
    public void insertTaskWithSubtasks(Task task, List<Subtask> subtasks) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // ID của Task cha đã có sẵn (UUID)
            String parentId = task.getId();

            // Phải lưu Task cha TRƯỚC để thỏa mãn ràng buộc khóa ngoại
            mTaskDao.insertTask(task);

            // Gán ID cho con và lưu con
            if (subtasks != null && !subtasks.isEmpty()) {
                for (Subtask sub : subtasks) {
                    sub.setTaskId(parentId);
                }
                mTaskDao.insertSubtasks(subtasks);
            }
        });
    }

    // --- WRITE OPERATIONS (SUBTASK) ---

    public LiveData<List<Subtask>> getSubtasksOfTask(String taskId) {
        return mTaskDao.getSubtasksOfTask(taskId);
    }

    public void insertSubtask(Subtask subtask) {
        AppDatabase.databaseWriteExecutor.execute(() -> mTaskDao.insertSubtask(subtask));
    }

    public void updateSubtask(Subtask subtask) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            subtask.setSynced(false);
            mTaskDao.updateSubtask(subtask);
        });
    }

    public void deleteSubtask(Subtask subtask) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            subtask.setDeleted(true);
            subtask.setSynced(false);
            mTaskDao.updateSubtask(subtask);
        });
    }

    // --- HELPERS ---
    private long getStartOfDay(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

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
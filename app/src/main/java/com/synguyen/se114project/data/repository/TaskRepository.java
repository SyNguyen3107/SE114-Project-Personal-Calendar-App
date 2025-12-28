package com.synguyen.se114project.data.repository;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.synguyen.se114project.BuildConfig;
import com.synguyen.se114project.data.dao.TaskDao;
import com.synguyen.se114project.data.database.AppDatabase;
import com.synguyen.se114project.data.entity.Subtask;
import com.synguyen.se114project.data.entity.Task;
import com.synguyen.se114project.data.remote.RetrofitClient;
import com.synguyen.se114project.data.remote.SupabaseService;

import java.util.Calendar;
import java.util.List;
// [THÊM MỚI]: Import Executor
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Response;

public class TaskRepository {

    private final TaskDao mTaskDao;
    private final AppDatabase mDatabase;
    private final LiveData<List<Task>> mAllTasks;
    private final SupabaseService mSupabaseService;
    private final SharedPreferences mPrefs;
    private static final String SUPABASE_URL = BuildConfig.SUPABASE_URL;
    private static final String SUPABASE_KEY = BuildConfig.SUPABASE_KEY;

    // [THÊM MỚI]: Khai báo Executor để chạy tác vụ background (API, Sync)
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public TaskRepository(Application application) {
        mDatabase = AppDatabase.getDatabase(application);
        mTaskDao = mDatabase.taskDao();

        mAllTasks = mTaskDao.getAllTasks();
        mSupabaseService = RetrofitClient.getRetrofitInstance().create(SupabaseService.class);
        mPrefs = application.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
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

    public LiveData<List<Task>> getTasksByCourseId(String courseId) {
        return mTaskDao.getTasksByCourseId(courseId);
    }

    // --- WRITE OPERATIONS (TASK) ---

    public void insert(Task task) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            String userId = mPrefs.getString("USER_ID", "");
            task.setOwnerId(userId);
            task.setSynced(false);
            mTaskDao.insertTask(task);

            String token = "Bearer " + mPrefs.getString("ACCESS_TOKEN", "");
            try {
                Call<List<Task>> call = mSupabaseService.createTask( token, task);
                Response<List<Task>> response = call.execute();
                if (response.isSuccessful()) {
                    task.setSynced(true);
                    mTaskDao.updateTask(task);
                    Log.d("Sync", "Pushed task to Cloud success: " + task.getTitle());
                } else {
                    Log.e("Sync", "Failed to push task: " + response.code() + " - " + response.message());
                }
            } catch (Exception e) {
                Log.e("Sync", "Network error: " + e.getMessage());
            }
        });
    }

    public void update(Task task) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            task.setLastUpdated(System.currentTimeMillis());
            task.setSynced(false);
            mTaskDao.updateTask(task);

            String token = "Bearer " + mPrefs.getString("ACCESS_TOKEN", "");
            try {
                String queryId = "eq." + task.getId();
                Call<Void> call = mSupabaseService.updateTask(token, queryId, task);
                Response<Void> response = call.execute();

                if (response.isSuccessful()) {
                    task.setSynced(true);
                    mTaskDao.updateTask(task);
                    Log.d("Sync", "Updated task on Cloud");
                }
            } catch (Exception e) {
                Log.e("Sync", "Update failed: " + e.getMessage());
            }
        });
    }

    public void delete(Task task) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long currentTime = System.currentTimeMillis();
            mTaskDao.softDeleteTask(task.getId(), currentTime);

            String token = "Bearer " + mPrefs.getString("ACCESS_TOKEN", "");
            try {
                String queryId = "eq." + task.getId();
                Call<Void> call = mSupabaseService.deleteTask( token, queryId);
                Response<Void> response = call.execute();
                if (response.isSuccessful()) {
                    mTaskDao.deletePhysicalTask(task);
                }
            } catch (Exception e) {
                Log.e("Sync", "Delete failed: " + e.getMessage());
            }
        });
    }

    public void insertTaskWithSubtasks(Task task, List<Subtask> subtasks) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mDatabase.runInTransaction(() -> {
                mTaskDao.insertTask(task);
                if (subtasks != null && !subtasks.isEmpty()) {
                    for (Subtask sub : subtasks) {
                        sub.setTaskId(task.getId());
                    }
                    mTaskDao.insertSubtasks(subtasks);
                }
            });
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
    public void refreshCourseTasks(String courseId) {
        executor.execute(() -> {
            try {
                String token = mPrefs.getString("ACCESS_TOKEN", "");
                if (token.isEmpty()) return;

                // Query filter: "eq." + courseId
                String query = "eq." + courseId;

                Log.d("SYNC_TASK", "Bắt đầu tải task cho course: " + courseId);

                Call<List<Task>> call = mSupabaseService.getTasks(
                        "Bearer " + token,
                        query,
                        "*",
                        "id.desc"
                );

                Response<List<Task>> response = call.execute();

                if (response.isSuccessful() && response.body() != null) {
                    List<Task> tasks = response.body();

                    if (!tasks.isEmpty()) {
                        for (Task t : tasks) {
                            t.setSynced(true);
                            // Nếu API không trả về course_id, ta gán thủ công để chắc chắn
                            if (t.getCourseId() == null || t.getCourseId().isEmpty()) {
                                t.setCourseId(courseId);
                            }
                        }
                        // Gọi hàm insertTasks vừa thêm vào TaskDao
                        mTaskDao.insertTasks(tasks);
                    }
                    Log.d("SYNC_TASK", "Đã tải " + tasks.size() + " task cho khóa " + courseId);
                } else {
                    Log.e("SYNC_TASK", "Lỗi API: " + response.code());
                }
            } catch (Exception e) {
                Log.e("SYNC_TASK", "Lỗi Exception: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
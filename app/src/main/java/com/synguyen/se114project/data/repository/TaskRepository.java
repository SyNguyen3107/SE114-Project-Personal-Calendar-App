package com.synguyen.se114project.data.repository;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.LiveData;
import com.synguyen.se114project.BuildConfig;
import com.synguyen.se114project.data.remote.RetrofitClient;
import com.synguyen.se114project.data.dao.TaskDao;
import com.synguyen.se114project.data.database.AppDatabase;
import com.synguyen.se114project.data.entity.Subtask;
import com.synguyen.se114project.data.entity.Task;
import com.synguyen.se114project.data.remote.SupabaseService;

import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
public class TaskRepository {

    private final TaskDao mTaskDao;
    private final LiveData<List<Task>> mAllTasks;
    private final SupabaseService mSupabaseService;
    private final SharedPreferences mPrefs;
    private static final String SUPABASE_URL = BuildConfig.SUPABASE_URL;
    private static final String SUPABASE_KEY = BuildConfig.SUPABASE_KEY;

    public TaskRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mTaskDao = db.taskDao();
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

    // MỚI: Lấy danh sách Task theo Course ID
    public LiveData<List<Task>> getTasksByCourseId(String courseId) {
        return mTaskDao.getTasksByCourseId(courseId);
    }

    // --- WRITE OPERATIONS (TASK) ---

    public void insert(Task task) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Lấy USER_ID từ SharedPreferences để làm owner_id
            String userId = mPrefs.getString("USER_ID", "");
            task.setOwnerId(userId);

            // Bước 1: Lưu vào Room trước (Để UI hiện ngay lập tức)
            task.setSynced(false);
            mTaskDao.insertTask(task);

            // Bước 2: Lấy Token (đã lưu khi Login)
            String token = "Bearer " + mPrefs.getString("ACCESS_TOKEN", "");

            // Bước 3: Gọi API đồng bộ lên Cloud
            try {
                Call<List<Task>> call = mSupabaseService.createTask(SUPABASE_KEY, token, task);
                Response<List<Task>> response = call.execute(); // Dùng execute() vì đã ở trong Thread background

                if (response.isSuccessful()) {
                    // Bước 4: Nếu thành công, update lại trạng thái Local
                    task.setSynced(true);
                    mTaskDao.updateTask(task);
                    Log.d("Sync", "Pushed task to Cloud success: " + task.getTitle());
                } else {
                    Log.e("Sync", "Failed to push task: " + response.code() + " - " + response.message());
                    // Nếu thất bại, task vẫn còn trong Room với isSynced = false
                }
            } catch (Exception e) {
                Log.e("Sync", "Network error: " + e.getMessage());
            }
        });
    }

    // 2. UPDATE
    public void update(Task task) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Bước 1: Update Local
            task.setLastUpdated(System.currentTimeMillis());
            task.setSynced(false);
            mTaskDao.updateTask(task);

            // Bước 2: Sync Cloud
            String token = "Bearer " + mPrefs.getString("ACCESS_TOKEN", "");
            try {
                // Lưu ý: task.id phải đúng định dạng "eq.UUID" khi truyền vào @Query
                String queryId = "eq." + task.getId();
                Call<Void> call = mSupabaseService.updateTask(SUPABASE_KEY, token, queryId, task);
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

    // 3. DELETE (Soft Delete Sync)
    public void delete(Task task) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Bước 1: Soft Delete ở Local (đánh dấu đã xóa)
            long currentTime = System.currentTimeMillis();
            mTaskDao.softDeleteTask(task.getId(), currentTime);

            // Bước 2: Gọi API Xóa trên Cloud
            String token = "Bearer " + mPrefs.getString("ACCESS_TOKEN", "");
            try {
                String queryId = "eq." + task.getId();
                Call<Void> call = mSupabaseService.deleteTask(SUPABASE_KEY, token, queryId);
                Response<Void> response = call.execute();

                if (response.isSuccessful()) {
                    // Nếu xóa trên cloud thành công -> Xóa cứng luôn ở Local để dọn dẹp
                    mTaskDao.deletePhysicalTask(task);
                }
            } catch (Exception e) {
                Log.e("Sync", "Delete failed: " + e.getMessage());
                // Để lại trạng thái isDeleted=true trong Room để WorkManager xóa sau
            }
        });
    }

    // Insert Task + Subtasks (Logic cho AddEditFragment)
    public void insertTaskWithSubtasks(Task task, List<Subtask> subtasks) {
        insert(task); // Sử dụng hàm insert đã có logic Sync

        // Gán ID cho con và lưu con
        if (subtasks != null && !subtasks.isEmpty()) {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                for (Subtask sub : subtasks) {
                    sub.setTaskId(task.getId());
                }
                mTaskDao.insertSubtasks(subtasks);
            });
        }
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

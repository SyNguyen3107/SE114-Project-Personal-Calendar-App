package com.synguyen.se114project.worker;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.synguyen.se114project.BuildConfig;
import com.synguyen.se114project.data.dao.TaskDao;
import com.synguyen.se114project.data.database.AppDatabase;
import com.synguyen.se114project.data.entity.Task;
import com.synguyen.se114project.data.remote.RetrofitClient;
import com.synguyen.se114project.data.remote.SupabaseService;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class SyncWorker extends Worker {

    private final TaskDao taskDao;
    private final SupabaseService supabaseService;

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        // Khởi tạo DAO và Service
        taskDao = AppDatabase.getDatabase(context).taskDao();
        supabaseService = RetrofitClient.getRetrofitInstance().create(SupabaseService.class);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d("SyncWorker", "Bắt đầu đồng bộ dữ liệu...");

        // 1. Lấy Token (để gọi API)
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("ACCESS_TOKEN", null);

        if (token == null) {
            return Result.failure(); // Không có token thì không sync được
        }

        // 2. Lấy danh sách Task chưa đồng bộ từ Room (isSynced = 0)
        // Lưu ý: TaskDao cần có hàm getUnsyncedTasks() trả về List<Task> (không phải LiveData)
        List<Task> unsyncedTasks = taskDao.getUnsyncedTasks();

        if (unsyncedTasks == null || unsyncedTasks.isEmpty()) {
            Log.d("SyncWorker", "Không có gì để đồng bộ.");
            return Result.success();
        }

        // 3. Duyệt và đẩy từng Task lên Server
        for (Task task : unsyncedTasks) {
            try {
                boolean success = false;

                if (task.isDeleted()) {
                    // TRƯỜNG HỢP XÓA: Gọi API Delete
                    Call<Void> call = supabaseService.deleteTask(
                            BuildConfig.SUPABASE_KEY, "Bearer " + token, "eq." + task.getId());
                    Response<Void> response = call.execute(); // Dùng execute() đồng bộ

                    if (response.isSuccessful()) {
                        taskDao.deletePhysicalTask(task); // Xóa hẳn khỏi máy
                        success = true;
                    }
                } else {
                    // TRƯỜNG HỢP THÊM/SỬA: Gọi API Upsert (Create)
                    // Lưu ý: create dùng POST, nếu ID đã tồn tại nó sẽ báo lỗi 409.
                    // Tốt nhất là dùng header Prefer: resolution=merge-duplicates ở SupabaseService
                    // Hoặc đơn giản: Try Create -> Fail -> Try Update.

                    // Cách đơn giản nhất cho đồ án: Gọi Update trước (PATCH)
                    Call<Void> callUpdate = supabaseService.updateTask(
                            BuildConfig.SUPABASE_KEY, "Bearer " + token, "eq." + task.getId(), task);
                    Response<Void> resUpdate = callUpdate.execute();

                    if (resUpdate.isSuccessful()) {
                        success = true;
                    } else {
                        // Nếu Update không tìm thấy ID (404) -> Gọi Create (POST)
                        Call<List<Task>> callCreate = supabaseService.createTask(
                                BuildConfig.SUPABASE_KEY, "Bearer " + token, task);
                        Response<List<Task>> resCreate = callCreate.execute();
                        if (resCreate.isSuccessful()) success = true;
                    }
                }

                // 4. Nếu thành công -> Update trạng thái Local
                if (success) {
                    task.setSynced(true);
                    if (!task.isDeleted()) {
                        taskDao.updateTask(task);
                    }
                    Log.d("SyncWorker", "Sync thành công Task: " + task.getTitle());
                }

            } catch (IOException e) {
                e.printStackTrace();
                return Result.retry(); // Gặp lỗi mạng -> Thử lại sau
            }
        }

        return Result.success();
    }
}
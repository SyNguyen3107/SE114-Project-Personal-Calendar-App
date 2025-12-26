package com.synguyen.se114project.data.repository;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.google.gson.JsonObject;
import com.synguyen.se114project.BuildConfig;
import com.synguyen.se114project.data.dao.CourseDao;
import com.synguyen.se114project.data.dao.TaskDao;
import com.synguyen.se114project.data.database.AppDatabase;
import com.synguyen.se114project.data.entity.Course;
import com.synguyen.se114project.data.remote.RetrofitClient;
import com.synguyen.se114project.data.remote.SupabaseService;
import com.synguyen.se114project.data.remote.response.FileObject;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Response;

public class CourseRepository {
    private TaskDao mTaskDao;
    private final CourseDao mCourseDao;
    private final SupabaseService mSupabaseService;
    private final SharedPreferences mPrefs;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private LiveData<List<Course>> mAllCourses;

    public CourseRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);

        mSupabaseService = RetrofitClient.getRetrofitInstance().create(SupabaseService.class);
        mCourseDao = db.courseDao();
        mTaskDao = db.taskDao();
        mPrefs = application.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);

        // Khởi tạo LiveData từ Room (để hiển thị lên UI)
        mAllCourses = mCourseDao.getAllCourses();
    }

    public LiveData<List<Course>> getAllCourses() {
        return mAllCourses;
    }

    public LiveData<Course> getCourseById(String id) {
        return mCourseDao.getCourseById(id);
    }

    public void insert(Course Course) {
        AppDatabase.databaseWriteExecutor.execute(() -> mCourseDao.insertCourse(Course));
    }

    public void insertAll(List<Course> Courses) {
        AppDatabase.databaseWriteExecutor.execute(() -> mCourseDao.insertCourses(Courses));
    }

    public void update(Course Course) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Course.setLastUpdated(System.currentTimeMillis());
            Course.setSynced(false);
            mCourseDao.updateCourse(Course);
        });
    }

    public void delete(Course Course) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mCourseDao.softDeleteCourse(Course.getId(), System.currentTimeMillis());
        });
    }

    // Hàm Sync chung (nếu cần mở rộng sau này)
    public void sync() {
        executor.execute(() -> {
            try {
                Log.d("SYNC", "Bắt đầu đồng bộ dữ liệu...");
            } catch (Exception e) {
                Log.e("SYNC", "Lỗi khi đồng bộ: " + e.getMessage());
            }
        });
    }

    // =========================================================================
    // [UPDATE QUAN TRỌNG] Hàm Sync Student Courses dùng RPC
    // =========================================================================
    public void syncStudentCourses() {
        executor.execute(() -> {
            try {
                String userId = mPrefs.getString("USER_ID", "");
                String token = mPrefs.getString("ACCESS_TOKEN", "");
                String authHeader = "Bearer " + token;

                if (userId.isEmpty()) {
                    Log.e("SYNC_COURSE", "User ID trống, không thể đồng bộ.");
                    return;
                }

                Log.d("SYNC_COURSE", "Bắt đầu gọi RPC lấy lớp cho User: " + userId);

                // 1. Tạo Body JSON chứa tham số 'sid'
                JsonObject jsonBody = new JsonObject();
                jsonBody.addProperty("sid", userId);

                // 2. Gọi hàm RPC từ SupabaseService (đã cập nhật dùng POST & JsonObject)
                Call<List<Course>> call = mSupabaseService.getStudentCoursesRPC(
                        BuildConfig.SUPABASE_KEY,
                        authHeader,
                        jsonBody
                );

                // 3. Thực thi request đồng bộ (execute) vì đang ở trong background thread
                Response<List<Course>> response = call.execute();

                if (response.isSuccessful() && response.body() != null) {
                    List<Course> courses = response.body();
                    // Bước 1: Xóa sạch dữ liệu cũ (Fake data, data cũ...)
                    mCourseDao.deleteAllCourses();
                    Log.d("SYNC_COURSE", "Đã xóa cache cũ.");

                    // Bước 2: Lưu dữ liệu mới sạch sẽ từ Server
                    if (!courses.isEmpty()) {
                        // Mặc định dữ liệu từ server về là đã sync
                        for (Course c : courses) {
                            c.setSynced(true);
                            c.setDeleted(false);
                        }
                        mCourseDao.insertCourses(courses);
                    }

                    Log.d("SYNC_COURSE", "Đã cập nhật " + courses.size() + " lớp học từ Server.");
                } else {
                    Log.e("SYNC_COURSE", "Lỗi API: " + response.code() + " - " + response.message());
                    if (response.errorBody() != null) {
                        Log.e("SYNC_COURSE", "Error Body: " + response.errorBody().string());
                    }
                }

            } catch (Exception e) {
                Log.e("SYNC_COURSE", "Lỗi Exception khi sync: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    // Hàm gọi API update trạng thái Task
    public void updateTaskStatusCloud(String taskId, boolean isCompleted) {
        executor.execute(() -> {
            try {
                Log.d("SYNC", "Đang gửi trạng thái Task " + taskId + " (" + isCompleted + ") lên Cloud...");
                // Logic update status implementation here...
            } catch (Exception e) {
                Log.e("SYNC", "Lỗi update cloud: " + e.getMessage());
            }
        });
    }

    public void getMaterials(String courseId, retrofit2.Callback<List<FileObject>> callback) {
        String token = "Bearer " + mPrefs.getString("ACCESS_TOKEN", "");
        fetchMaterials(token, courseId, callback);
    }

    public void fetchMaterials(String token, String courseId, retrofit2.Callback<List<FileObject>> callback) {
        JsonObject body = new JsonObject();
        body.addProperty("prefix", "course_" + courseId);
        body.addProperty("limit", 100);

        mSupabaseService.listFiles(BuildConfig.SUPABASE_KEY, token, "materials", body)
                .enqueue(callback);
    }
}
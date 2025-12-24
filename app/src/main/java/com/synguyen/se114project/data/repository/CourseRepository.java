package com.synguyen.se114project.data.repository;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.synguyen.se114project.BuildConfig;
import com.synguyen.se114project.data.dao.CourseDao;
import com.synguyen.se114project.data.dao.TaskDao;
import com.synguyen.se114project.data.database.AppDatabase;
import com.synguyen.se114project.data.entity.Course;
import com.synguyen.se114project.data.remote.RetrofitClient;
import com.synguyen.se114project.data.remote.SupabaseService;
import com.synguyen.se114project.data.remote.response.FileObject;
//import io.supabase.gotrue.GoTrueClient;
//import io.supabase.postgrest.PostgrestClient;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;

public class CourseRepository {
    private TaskDao mTaskDao;
    private final CourseDao mCourseDao;
    private final SupabaseService mSupabaseService;
    private final SharedPreferences mPrefs; // 1. THÊM: Biến SharedPreferences
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Thêm biến này để tránh lỗi "Cannot resolve symbol 'mAllCourses'"
    private LiveData<List<Course>> mAllCourses;

    public CourseRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);

        // 2. SỬA: Gán vào biến mSupabaseService thay vì tên class
        mSupabaseService = RetrofitClient.getRetrofitInstance().create(SupabaseService.class);

        mCourseDao = db.courseDao();
        mTaskDao = db.taskDao();

        // 3. THÊM: Khởi tạo SharedPreferences
        mPrefs = application.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);

        // Khởi tạo LiveData
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

    // Hàm chèn nhiều lớp (cho dữ liệu mẫu)
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

    // Hàm Sync: Kéo từ Cloud -> Local (Giả lập)
    public void sync() {
        executor.execute(() -> {
            try {
                Log.d("SYNC", "Bắt đầu đồng bộ dữ liệu từ Supabase...");
                // Logic sync chung (nếu cần)
                Log.d("SYNC", "Đồng bộ hoàn tất!");

            } catch (Exception e) {
                Log.e("SYNC", "Lỗi khi đồng bộ: " + e.getMessage());
            }
        });
    }

    // Hàm Sync Student (Bước 2 của bạn)
    public void syncStudentCourses() {
        executor.execute(() -> {
            try {
                // 4. SỬA: Dùng mPrefs đã khai báo
                String userId = mPrefs.getString("USER_ID", "");
                String token = "Bearer " + mPrefs.getString("ACCESS_TOKEN", "");

                if (userId.isEmpty()) return;

                Log.d("SYNC_COURSE", "Bắt đầu tải khóa học cho SV: " + userId);

                // Gọi API lấy các môn có trong bảng enrollments của user này
                // Cú pháp: select=*,enrollments!inner(student_id) để join bảng
                Call<List<Course>> call = mSupabaseService.getStudentCourses(
                        BuildConfig.SUPABASE_KEY,
                        token,
                        "*,enrollments!inner(student_id)",
                        "eq." + userId
                );

                retrofit2.Response<List<Course>> response = call.execute();

                if (response.isSuccessful() && response.body() != null) {
                    List<Course> courses = response.body();

                    // Lưu vào Local DB (Room)
                    if (!courses.isEmpty()) {
                        mCourseDao.insertCourses(courses);
                    }
                    Log.d("SYNC_COURSE", "Đã tải về " + courses.size() + " môn học.");
                } else {
                    Log.e("SYNC_COURSE", "Lỗi API: " + response.code() + " - " + (response.errorBody() != null ? response.errorBody().string() : "null"));
                }

            } catch (Exception e) {
                Log.e("SYNC_COURSE", "Lỗi Exception: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    // Hàm gọi API update trạng thái Task lên Server
    public void updateTaskStatusCloud(String taskId, boolean isCompleted) {
        executor.execute(() -> {
            try {
                Log.d("SYNC", "Đang gửi trạng thái Task " + taskId + " (" + isCompleted + ") lên Cloud...");
                // Logic update status
            } catch (Exception e) {
                Log.e("SYNC", "Lỗi update cloud: " + e.getMessage());
            }
        });
    }

    public void getMaterials(String courseId, retrofit2.Callback<List<FileObject>> callback) {
        // Lưu ý: Token lấy từ mPrefs sẽ tốt hơn mTaskDao.toString()
        String token = "Bearer " + mPrefs.getString("ACCESS_TOKEN", "");
        fetchMaterials(token, courseId, callback);
    }

    public void fetchMaterials(String token, String courseId, retrofit2.Callback<List<FileObject>> callback) {
        // Có thể dùng mSupabaseService đã khởi tạo sẵn thay vì tạo mới
        com.google.gson.JsonObject body = new com.google.gson.JsonObject();
        body.addProperty("prefix", "course_" + courseId);
        body.addProperty("limit", 100);

        mSupabaseService.listFiles(BuildConfig.SUPABASE_KEY, token, "materials", body)
                .enqueue(callback);
    }
}
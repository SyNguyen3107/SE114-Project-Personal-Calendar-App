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
    private final Context mContext;
    // Executor riêng cho các tác vụ mạng (Network operations)
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private LiveData<List<Course>> mAllCourses;

    public CourseRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);

        mSupabaseService = RetrofitClient.getRetrofitInstance().create(SupabaseService.class);
        mCourseDao = db.courseDao();
        mTaskDao = db.taskDao();
        mPrefs = application.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        mContext = application.getApplicationContext();

        // Khởi tạo LiveData từ Room (để hiển thị lên UI)
        mAllCourses = mCourseDao.getAllCourses();
    }

    // --- GETTERS ---

    public LiveData<List<Course>> getAllCourses() {
        return mAllCourses;
    }

    public LiveData<Course> getCourseById(String id) {
        return mCourseDao.getCourseById(id);
    }

    // --- LOCAL DATABASE OPERATIONS (CRUD) ---
    // Sử dụng databaseWriteExecutor của AppDatabase để đảm bảo thread-safe với Room

    public void insert(Course course) {
        AppDatabase.databaseWriteExecutor.execute(() -> mCourseDao.insertCourse(course));
    }

    public void insertAll(List<Course> courses) {
        AppDatabase.databaseWriteExecutor.execute(() -> mCourseDao.insertCourses(courses));
    }

    public void update(Course course) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            course.setLastUpdated(System.currentTimeMillis());
            course.setSynced(false); // Đánh dấu chưa đồng bộ nếu sửa local
            mCourseDao.updateCourse(course);
        });
    }

    public void delete(Course course) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Soft delete: Chỉ đánh dấu đã xóa và cập nhật thời gian
            mCourseDao.softDeleteCourse(course.getId(), System.currentTimeMillis());
        });
    }

    // --- REMOTE OPERATIONS (SYNC & API) ---

    /**
     * Đồng bộ danh sách khóa học của sinh viên từ Server về Local.
     * @param studentId ID của sinh viên cần lấy danh sách lớp.
     */
    public void syncStudentCourses(String studentId) {
        // Kick off sync in background and allow one retry on 401 via refresh token
        syncStudentCoursesInternal(studentId, /*retryOn401*/ true);
    }

    private void syncStudentCoursesInternal(String studentId, boolean retryOn401) {
        executor.execute(() -> {
            try {
                // 1. Lấy Token xác thực (vẫn lấy từ Prefs vì nó thuộc về session đăng nhập)
                String token = mPrefs.getString("ACCESS_TOKEN", "");
                String authHeader = "Bearer " + token;

                // Kiểm tra điều kiện đầu vào
                if (studentId == null || studentId.isEmpty()) {
                    Log.e("SYNC_COURSE", "Student ID truyền vào bị trống, hủy đồng bộ.");
                    return;
                }

                if (token.isEmpty()) {
                    Log.e("SYNC_COURSE", "Token trống (chưa đăng nhập?), hủy đồng bộ.");
                    return;
                }

                Log.d("SYNC_COURSE", "Bắt đầu gọi RPC lấy lớp cho User: " + studentId);

                // 2. Tạo Body JSON chứa tham số 'sid'
                JsonObject jsonBody = new JsonObject();
                jsonBody.addProperty("sid", studentId);

                // 3. Gọi hàm RPC từ SupabaseService
                Call<List<Course>> call = mSupabaseService.getStudentCoursesRPC(
                        authHeader,
                        jsonBody
                );

                // 4. Thực thi request (Synchronous vì đang ở background thread)
                Response<List<Course>> response = call.execute();

                if (response.isSuccessful() && response.body() != null) {
                    List<Course> courses = response.body();

                    // --- XỬ LÝ DATABASE LOCAL ---
                    mCourseDao.deleteAllCourses();
                    Log.d("SYNC_COURSE", "Đã xóa cache cũ.");

                    if (!courses.isEmpty()) {
                        for (Course c : courses) {
                            c.setSynced(true); // Data từ server về mặc định là đã sync
                            c.setDeleted(false);
                        }
                        mCourseDao.insertCourses(courses);
                    }

                    Log.d("SYNC_COURSE", "Đã cập nhật " + courses.size() + " lớp học từ Server.");
                } else if (response.code() == 401 && retryOn401) {
                    // Try to refresh access token once
                    Log.w("SYNC_COURSE", "Received 401, attempting to refresh access token and retry.");
                    // Use AuthRepository to refresh token (async) and retry
                    new AuthRepository().refreshAccessToken(mContext, new AuthRepository.ResultCallback<String>() {
                        @Override
                        public void onSuccess(String newToken) {
                            // Update prefs already done in AuthRepository; retry once with new token
                            Log.d("SYNC_COURSE", "Token refreshed, retrying sync.");
                            syncStudentCoursesInternal(studentId, false);
                        }

                        @Override
                        public void onError(String message) {
                            Log.e("SYNC_COURSE", "Refresh token failed: " + message);
                        }
                    });
                } else {
                    Log.e("SYNC_COURSE", "Lỗi API: " + response.code() + " - " + response.message());
                    try {
                        if (response.errorBody() != null) Log.e("SYNC_COURSE", "Error Body: " + response.errorBody().string());
                    } catch (Exception ex) { /* ignore */ }
                }

            } catch (Exception e) {
                Log.e("SYNC_COURSE", "Lỗi Exception khi sync: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    // Hàm Sync chung (Placeholder nếu cần mở rộng)
    public void sync() {
        executor.execute(() -> {
            try {
                Log.d("SYNC", "Hàm sync chung được gọi (chưa implement logic cụ thể).");
            } catch (Exception e) {
                Log.e("SYNC", "Lỗi khi đồng bộ: " + e.getMessage());
            }
        });
    }

    // Hàm gọi API update trạng thái Task (Placeholder)
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

    // --- FILE/MATERIAL OPERATIONS ---

    public void getMaterials(String courseId, retrofit2.Callback<List<FileObject>> callback) {
        String token = "Bearer " + mPrefs.getString("ACCESS_TOKEN", "");
        fetchMaterials(token, courseId, callback);
    }

    public void fetchMaterials(String token, String courseId, retrofit2.Callback<List<FileObject>> callback) {
        JsonObject body = new JsonObject();
        // Giả định bucket structure là course_{id} hoặc folder prefix
        body.addProperty("prefix", "course_" + courseId);
        body.addProperty("limit", 100);

        mSupabaseService.listFiles(BuildConfig.SUPABASE_KEY, "materials", body)
                .enqueue(callback);
    }
}
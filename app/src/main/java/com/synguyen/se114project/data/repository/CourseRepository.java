package com.synguyen.se114project.data.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.synguyen.se114project.data.dao.CourseDao;
import com.synguyen.se114project.data.dao.TaskDao;
import com.synguyen.se114project.data.database.AppDatabase;
import com.synguyen.se114project.data.entity.Course;
//import io.supabase.gotrue.GoTrueClient;
//import io.supabase.postgrest.PostgrestClient;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CourseRepository {
    private TaskDao mTaskDao;
    private final CourseDao mCourseDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Thêm biến này để tránh lỗi "Cannot resolve symbol 'mAllCourses'"
    private LiveData<List<Course>> mAllCourses;

    public CourseRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mCourseDao = db.courseDao();
        mTaskDao = db.taskDao();
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

                // TODO: Gọi API Supabase tại đây
                // Ví dụ: List<Task> remoteTasks = supabase.from("tasks").select()...

                // Sau đó lưu vào Room:
                // if (remoteTasks != null) {
                //     for (Task task : remoteTasks) {
                //         mTaskDao.insertTask(task);
                //     }
                // }

                Log.d("SYNC", "Đồng bộ hoàn tất!");

            } catch (Exception e) {
                Log.e("SYNC", "Lỗi khi đồng bộ: " + e.getMessage());
            }
        });
    }

    // Hàm gọi API update trạng thái Task lên Server
    public void updateTaskStatusCloud(String taskId, boolean isCompleted) {
        executor.execute(() -> {
            try {
                Log.d("SYNC", "Đang gửi trạng thái Task " + taskId + " (" + isCompleted + ") lên Cloud...");

                // TODO: Gọi API Update Supabase
                // supabase.from("tasks").update(Map.of("is_completed", isCompleted)).eq("id", taskId).execute();

            } catch (Exception e) {
                Log.e("SYNC", "Lỗi update cloud: " + e.getMessage());
            }
        });
    }
}
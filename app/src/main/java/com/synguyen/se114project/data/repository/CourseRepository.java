package com.synguyen.se114project.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.synguyen.se114project.data.dao.CourseDao;
import com.synguyen.se114project.data.database.AppDatabase;
import com.synguyen.se114project.data.entity.Course;

import java.util.List;

public class CourseRepository {

    private final CourseDao mCourseDao;
    private final LiveData<List<Course>> mAllCourses;

    public CourseRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mCourseDao = db.courseDao();
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
}
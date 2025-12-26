package com.synguyen.se114project.viewmodel.student;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.synguyen.se114project.data.entity.Course;
import com.synguyen.se114project.data.repository.CourseRepository; // Đảm bảo bạn đã có class này

import java.util.ArrayList;
import java.util.List;

public class CourseViewModel extends AndroidViewModel {

    private final CourseRepository mRepository;
    private final LiveData<List<Course>> mAllCourses;

    public CourseViewModel(@NonNull Application application) {
        super(application);
        mRepository = new CourseRepository(application);
        // LiveData này tự động cập nhật khi Room Database thay đổi
        mAllCourses = mRepository.getAllCourses();
    }

    public LiveData<List<Course>> getAllCourses() {
        return mAllCourses;
    }
    public void refreshStudentCourses() {
        // Gọi hàm syncStudentCourses() mà chúng ta đã viết ở bước trước trong Repository
        mRepository.syncStudentCourses();
    }
}
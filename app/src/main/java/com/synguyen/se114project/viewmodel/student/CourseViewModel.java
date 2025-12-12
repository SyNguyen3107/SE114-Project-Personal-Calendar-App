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
        // Lưu ý: Tôi giả định bạn đã đổi tên ClassroomRepository thành CourseRepository
        mRepository = new CourseRepository(application);
        mAllCourses = mRepository.getAllCourses();
    }

    public LiveData<List<Course>> getAllCourses() {
        return mAllCourses;
    }

    // Hàm tạo dữ liệu mẫu (Chỉ dùng để test khi chưa có data)
    public void createSampleData() {
        List<Course> samples = new ArrayList<>();
        samples.add(new Course("Mathematics", "Chapter 5: Calculus", "Jason Mayor", "05:30 - 06:30", "04/21", "#2196F3")); // Blue
        samples.add(new Course("History", "Introduction to WW2", "Hana Montana", "07:30 - 08:30", "05/21", "#FFC107")); // Amber
        samples.add(new Course("Science", "Chapter 12: Physics", "Albert Kelberg", "07:30 - 08:00", "06/21", "#4CAF50")); // Green
        samples.add(new Course("Spanish", "Chapter 5: Basics", "Sofia Vergara", "09:00 - 10:00", "04/21", "#FF5722")); // Deep Orange

        mRepository.insertAll(samples);
    }
}
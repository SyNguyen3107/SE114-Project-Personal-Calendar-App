package com.synguyen.se114project.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.synguyen.se114project.data.entity.Course;
import com.synguyen.se114project.data.entity.Task;
import com.synguyen.se114project.data.repository.CourseRepository;
import com.synguyen.se114project.data.repository.TaskRepository;

import java.util.List;

public class CourseDetailViewModel extends AndroidViewModel {

    private final CourseRepository courseRepository;
    private final TaskRepository taskRepository;

    public CourseDetailViewModel(@NonNull Application application) {
        super(application);
        // Đảm bảo bạn đã có các Repository này
        courseRepository = new CourseRepository(application);
        taskRepository = new TaskRepository(application);
    }

    // Lấy thông tin chi tiết môn học theo ID
    public LiveData<Course> getCourseById(String courseId) {
        // Lưu ý: Kiểm tra tên hàm trong Repository (getClassroomById hay getCourseById)
        return courseRepository.getCourseById(courseId);
    }

    // Lấy danh sách bài tập của môn học này
    public LiveData<List<Task>> getTasksByClassId(String classId) {
        return taskRepository.getTasksByCourseId(classId);
    }
}
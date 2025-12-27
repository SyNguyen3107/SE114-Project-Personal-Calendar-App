package com.synguyen.se114project.viewmodel.student;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.synguyen.se114project.data.entity.Task;
import com.synguyen.se114project.data.repository.TaskRepository;
import java.util.List;

public class TaskViewModel extends AndroidViewModel {
    private final TaskRepository mRepository;

    public TaskViewModel(@NonNull Application application) {
        super(application);
        mRepository = new TaskRepository(application);
    }

    // Hàm này sẽ được gọi từ Fragment
    public LiveData<List<Task>> getCourseTasks(String courseId) {
        return mRepository.getTasksByCourseId(courseId);
    }
    public void update(Task task) {
        mRepository.update(task); // Đảm bảo TaskRepository đã có hàm update
    }
    public void refreshTasks(String courseId) {
        // Gọi sang Repository
        mRepository.refreshCourseTasks(courseId);
    }
}
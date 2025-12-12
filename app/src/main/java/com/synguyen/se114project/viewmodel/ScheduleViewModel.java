package com.synguyen.se114project.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.synguyen.se114project.data.entity.Task;
import com.synguyen.se114project.data.repository.TaskRepository;

import java.util.List;

public class ScheduleViewModel extends AndroidViewModel {

    private final TaskRepository mRepository;

    // 1. Dùng để load toàn bộ task -> Hiển thị dấu chấm trên lịch
    private final LiveData<List<Task>> allTasks;

    // 2. Dùng để lọc list task bên dưới khi chọn ngày
    private final MutableLiveData<Long> selectedDate = new MutableLiveData<>();
    private final LiveData<List<Task>> tasksBySelectedDate;

    public ScheduleViewModel(@NonNull Application application) {
        super(application);
        mRepository = new TaskRepository(application);

        // Load toàn bộ (cho Calendar Dots)
        allTasks = mRepository.getAllTasks();

        // Load theo ngày chọn (cho RecyclerView)
        tasksBySelectedDate = Transformations.switchMap(selectedDate, date ->
                mRepository.getTasksByDate(date)
        );

        // Mặc định chọn ngày hôm nay
        selectedDate.setValue(System.currentTimeMillis());
    }

    // --- API ---

    public LiveData<List<Task>> getAllTasks() {
        return allTasks;
    }

    public LiveData<List<Task>> getTasksBySelectedDate() {
        return tasksBySelectedDate;
    }

    public void setSelectedDate(long date) {
        selectedDate.setValue(date);
    }
}
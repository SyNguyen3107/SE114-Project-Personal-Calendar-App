package com.synguyen.se114project.viewmodel.student;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.synguyen.se114project.data.entity.Task;
import com.synguyen.se114project.data.repository.CourseRepository;
import com.synguyen.se114project.data.repository.TaskRepository;

import java.util.List;

public class HomeViewModel extends AndroidViewModel {

    private final TaskRepository taskRepository;
    private CourseRepository courseRepository;
    // 1. Biến lưu trạng thái ngày đang chọn (State)
    private final MutableLiveData<Long> selectedDate = new MutableLiveData<>();

    // 2. Danh sách Task tự động cập nhật theo ngày (Trigger)
    private final LiveData<List<Task>> tasksBySelectedDate;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        taskRepository = new TaskRepository(application);
        courseRepository = new CourseRepository(application);

        // 3. Thiết lập SwitchMap: Cứ khi nào selectedDate đổi -> gọi Repository query lại
        tasksBySelectedDate = Transformations.switchMap(selectedDate, date ->
                taskRepository.getTasksByDate(date)
        );

        // Mặc định chọn ngày hôm nay
        selectedDate.setValue(System.currentTimeMillis());
    }

    // --- INPUT: UI gọi hàm này khi user chọn ngày trên lịch ---
    public void setSelectedDate(long date) {
        selectedDate.setValue(date);
    }

    // --- OUTPUT: UI quan sát biến này để hiển thị list ---
    public LiveData<List<Task>> getTasksBySelectedDate() {
        return tasksBySelectedDate;
    }

    // Hàm xóa nhanh (Swipe to delete)
    public void deleteTask(Task task) {
        taskRepository.delete(task);
    }

    public void syncData() {
        courseRepository.sync();
    }

    // Hàm update trạng thái
    public void updateTaskStatus(Task task, boolean isChecked) {
        // 1. Update Local (Room) trước để UI phản hồi nhanh
        // Lưu ý: Cần đảm bảo TaskRepository có hàm update(Task)
        task.setCompleted(isChecked);
        taskRepository.update(task);

        // 2. Update Cloud (Supabase)
        courseRepository.updateTaskStatusCloud(task.getId(), isChecked);
    }
}
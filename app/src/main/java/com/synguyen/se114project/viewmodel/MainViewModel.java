package com.synguyen.se114project.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.synguyen.se114project.data.entity.Task;
import com.synguyen.se114project.data.entity.Subtask;
import com.synguyen.se114project.data.repository.TaskRepository;

import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private TaskRepository mRepository;
    private final LiveData<List<Task>> mAllTasks;
    // 1. Tạo một biến LiveData để lưu ngày đang chọn
    private final MutableLiveData<Long> selectedDate = new MutableLiveData<>();

    // 2. Tạo LiveData danh sách task phụ thuộc vào ngày chọn
    private final LiveData<List<Task>> tasksBySelectedDate;
    public MainViewModel(@NonNull Application application) {
        super(application);
        mRepository = new TaskRepository(application);
        mAllTasks = mRepository.getAllTasks();

        // 3. Thiết lập SwitchMap
        // Mỗi khi "selectedDate" thay đổi -> Gọi Repository lấy list mới
        tasksBySelectedDate = Transformations.switchMap(selectedDate, date ->
        mRepository.getTasksByDate(date));

        // Mặc định chọn ngày hôm nay khi mở app
        setSelectedDate(System.currentTimeMillis());
    }
    // 4. Hàm để UI gọi khi bấm chọn ngày
    public void setSelectedDate(long date) {
        selectedDate.setValue(date);
    }

    // 5. Hàm để UI quan sát danh sách đã lọc
    public LiveData<List<Task>> getTasksBySelectedDate() {
        return tasksBySelectedDate;
    }
    // --- TASK API ---
    public LiveData<List<Task>> getAllTasks() { return mAllTasks; }
    public void insertTask(Task task) { mRepository.insert(task); }
    public void updateTask(Task task) { mRepository.update(task); }
    public void deleteTask(Task task) { mRepository.delete(task); }
    public void saveTask(Task task, List<Subtask> subtasks) {
        mRepository.insertTaskWithSubtasks(task, subtasks);
    }
    // --- SUBTASK API ---
    // UI sẽ gọi hàm này khi bấm vào một Task để xem chi tiết
    public LiveData<List<Subtask>> getSubtasksOfTask(int taskId) {
        return mRepository.getSubtasksOfTask(taskId);
    }

    public void insertSubtask(Subtask subtask) { mRepository.insertSubtask(subtask); }
    public void updateSubtask(Subtask subtask) { mRepository.updateSubtask(subtask); }
    public void deleteSubtask(Subtask subtask) { mRepository.deleteSubtask(subtask); }
}
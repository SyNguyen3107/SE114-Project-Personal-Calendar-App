package com.synguyen.se114project.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.synguyen.se114project.data.entity.Task;
import com.synguyen.se114project.data.entity.Subtask;
import com.synguyen.se114project.data.repository.TaskRepository;

import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private TaskRepository mRepository;
    private final LiveData<List<Task>> mAllTasks;

    public MainViewModel(@NonNull Application application) {
        super(application);
        mRepository = new TaskRepository(application);
        mAllTasks = mRepository.getAllTasks();
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
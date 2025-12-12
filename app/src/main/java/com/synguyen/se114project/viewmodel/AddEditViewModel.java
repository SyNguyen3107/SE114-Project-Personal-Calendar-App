package com.synguyen.se114project.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.synguyen.se114project.data.entity.Subtask;
import com.synguyen.se114project.data.entity.Task;
import com.synguyen.se114project.data.repository.TaskRepository;

import java.util.List;

public class AddEditViewModel extends AndroidViewModel {

    private final TaskRepository mRepository;

    public AddEditViewModel(@NonNull Application application) {
        super(application);
        mRepository = new TaskRepository(application);
    }

    // Logic lưu Task mới (kèm Subtask)
    public void saveNewTask(Task task, List<Subtask> subtasks) {
        if (subtasks != null && !subtasks.isEmpty()) {
            mRepository.insertTaskWithSubtasks(task, subtasks);
        } else {
            mRepository.insert(task);
        }
    }

    // Logic cập nhật Task đã có
    public void updateTask(Task task) {
        mRepository.update(task);
    }
}
package com.synguyen.se114project.viewmodel.teacher;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.synguyen.se114project.BuildConfig;
import com.synguyen.se114project.data.entity.Subtask;
import com.synguyen.se114project.data.entity.Task;
import com.synguyen.se114project.data.remote.RetrofitClient;
import com.synguyen.se114project.data.remote.SupabaseService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherTaskViewModel extends AndroidViewModel {

    private final SupabaseService service;
    // LiveData để UI quan sát
    private final MutableLiveData<List<Subtask>> subtasksLiveData = new MutableLiveData<>();
    private final MutableLiveData<Task> taskLiveData = new MutableLiveData<>();
    private String token; // Cần set token từ Fragment

    public TeacherTaskViewModel(@NonNull Application application) {
        super(application);
        service = RetrofitClient.getRetrofitInstance().create(SupabaseService.class);
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LiveData<List<Subtask>> getSubtasks() {
        return subtasksLiveData;
    }

    public LiveData<Task> getTask() {
        return taskLiveData;
    }

    // 1. Lấy chi tiết Task
    public void fetchTaskDetail(String taskId) {
        if (token == null) return;
        // Gọi API lấy 1 task (dùng filter eq.id)
        service.getTasksByCourse(BuildConfig.SUPABASE_KEY, "Bearer " + token, null) // Tạm dùng API cũ hoặc viết API getTaskById
                .enqueue(new Callback<List<Task>>() {
                    @Override
                    public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                        // ... Xử lý lấy task đúng ID từ list ...
                        // Để tối ưu, bạn nên thêm API getTaskById vào SupabaseService
                    }
                    @Override
                    public void onFailure(Call<List<Task>> call, Throwable t) {}
                });
    }

    // 2. Lấy danh sách Subtask của Task này
    public void fetchSubtasks(String taskId) {
        if (token == null) return;
        // Giả sử bạn đã thêm API getSubtasks vào SupabaseService
        // service.getSubtasks(key, token, "eq." + taskId)...
        // Nếu chưa có, ta sẽ xử lý local list tạm thời
    }

    // 3. Thêm Subtask mới
    public void addSubtask(Subtask subtask) {
        // Gọi API createSubtask
        // service.createSubtask(...)

        // Cập nhật LiveData ngay lập tức để UI mượt
        List<Subtask> currentList = subtasksLiveData.getValue();
        if (currentList != null) {
            currentList.add(subtask);
            subtasksLiveData.postValue(currentList);
        }
    }

    // 4. Xóa Subtask
    public void deleteSubtask(Subtask subtask) {
        // Gọi API delete
    }

    // 5. Update trạng thái hoàn thành
    public void updateSubtask(Subtask subtask) {
        // Gọi API update
    }
}
package com.synguyen.se114project.viewmodel.student;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.synguyen.se114project.data.entity.Subtask;
import com.synguyen.se114project.data.entity.Task;
import com.synguyen.se114project.data.repository.TaskRepository;

import java.util.List;

public class TaskDetailViewModel extends AndroidViewModel {

    private final TaskRepository mRepository;

    public TaskDetailViewModel(@NonNull Application application) {
        super(application);
        mRepository = new TaskRepository(application);
    }

    // =============================================================================================
    // --- READ DATA ---
    // =============================================================================================

    // Lấy thông tin chi tiết Task (Dùng UUID String)
    public LiveData<Task> getTaskById(String taskId) {
        return mRepository.getTaskById(taskId);
    }

    // Lấy danh sách Subtask của Task này (Sắp xếp theo thứ tự thêm vào hoặc ID)
    public LiveData<List<Subtask>> getSubtasksOfTask(String taskId) {
        return mRepository.getSubtasksOfTask(taskId);
    }

    // =============================================================================================
    // --- WRITE DATA (TASK) ---
    // =============================================================================================

    // Cập nhật thông tin Task cha (Vd: Đánh dấu Completed, sửa Title, sửa Description...)
    public void updateTask(Task task) {
        mRepository.update(task);
    }

    // Xóa Task cha (Sẽ kích hoạt Soft Delete và xóa luôn Subtasks trong Repository nếu cần)
    public void deleteTask(Task task) {
        mRepository.delete(task);
    }

    // =============================================================================================
    // --- WRITE DATA (SUBTASK) ---
    // =============================================================================================

    // Thêm Subtask mới ngay tại màn hình detail
    public void addSubtask(Subtask subtask) {
        mRepository.insertSubtask(subtask);
    }

    // Check/Uncheck hoàn thành Subtask
    public void updateSubtaskStatus(Subtask subtask, boolean isCompleted) {
        // Cập nhật giá trị ngay trên object trước khi gửi xuống DB
        subtask.setCompleted(isCompleted);
        mRepository.updateSubtask(subtask);
    }

    /**
     * Cập nhật tiêu đề Subtask
     * Lưu ý: Đối tượng 'subtask' truyền vào phải đã được setText() tên mới từ UI (Dialog)
     */
    public void updateSubtaskTitle(Subtask subtask) {
        // Gọi Repository để update toàn bộ object (bao gồm title mới)
        mRepository.updateSubtask(subtask);
    }

    // Hàm update chung (nếu bạn muốn dùng cho các trường hợp khác)
    public void updateSubtask(Subtask subtask) {
        mRepository.updateSubtask(subtask);
    }

    // Xóa Subtask
    public void deleteSubtask(Subtask subtask) {
        mRepository.deleteSubtask(subtask);
    }
}
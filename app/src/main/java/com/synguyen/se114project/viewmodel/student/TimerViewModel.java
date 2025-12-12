package com.synguyen.se114project.viewmodel.student;

import android.os.CountDownTimer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.synguyen.se114project.data.entity.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimerViewModel extends ViewModel {

    // Danh sách các Task đang chạy Timer (để hiển thị lên UI Home)
    private final MutableLiveData<List<Task>> runningTasks = new MutableLiveData<>(new ArrayList<>());

    // Map lưu trữ thời gian còn lại của từng Task (Key: TaskID, Value: Millis còn lại)
    private final MutableLiveData<Map<String, Long>> tasksTimeRemaining = new MutableLiveData<>(new HashMap<>());

    // Map lưu trữ các object CountDownTimer thực tế (Key: TaskID)
    private final Map<String, CountDownTimer> timerMap = new HashMap<>();

    // Map lưu trữ trạng thái Pause (Key: TaskID, Value: Boolean)
    private final Map<String, Boolean> pausedStateMap = new HashMap<>();

    // --- Getters ---
    public LiveData<List<Task>> getRunningTasks() { return runningTasks; }
    public LiveData<Map<String, Long>> getTasksTimeRemaining() { return tasksTimeRemaining; }

    // Kiểm tra xem một Task cụ thể có đang chạy (hoặc pause) không
    public boolean isTaskRunningOrPaused(String taskId) {
        return timerMap.containsKey(taskId) || (pausedStateMap.containsKey(taskId) && Boolean.TRUE.equals(pausedStateMap.get(taskId)));
    }

    public boolean isTaskRunning(String taskId) {
        return timerMap.containsKey(taskId);
    }

    public long getTimeRemaining(String taskId) {
        Map<String, Long> currentMap = tasksTimeRemaining.getValue();
        if (currentMap != null && currentMap.containsKey(taskId)) {
            Long time = currentMap.get(taskId);
            return time != null ? time : 0;
        }
        return 0;
    }

    // --- Actions ---

    public void startTimer(Task task) {
        String taskId = task.getId();

        // Nếu đã có timer đang chạy cho task này -> Bỏ qua
        if (timerMap.containsKey(taskId)) return;

        // Thêm vào danh sách running tasks (nếu chưa có)
        List<Task> currentList = runningTasks.getValue();
        if (currentList == null) currentList = new ArrayList<>();

        // Kiểm tra xem task đã có trong list chưa để tránh duplicate
        boolean exists = false;
        for (Task t : currentList) {
            if (t.getId().equals(taskId)) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            currentList.add(task);
            runningTasks.setValue(currentList);
        }

        // Xác định thời gian bắt đầu
        long duration;
        long currentRemaining = getTimeRemaining(taskId);

        if (currentRemaining > 0) {
            // Resume
            duration = currentRemaining;
        } else {
            // Start mới
            duration = task.getDuration() > 0 ? task.getDuration() : 25 * 60 * 1000;
        }

        // QUAN TRỌNG: Xóa trạng thái pause để UI biết là đang chạy
        pausedStateMap.remove(taskId);
        // Cần trigger update LiveData để UI nhận biết thay đổi trạng thái ngay lập tức (nếu cần thiết)
        // Tuy nhiên, logic check UI chủ yếu dựa vào timerMap.containsKey, nên việc put vào timerMap bên dưới là đủ.

        createAndStartTimer(taskId, duration);
    }

    private void createAndStartTimer(String taskId, long duration) {
        CountDownTimer timer = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Cập nhật thời gian còn lại vào LiveData Map
                Map<String, Long> timeMap = tasksTimeRemaining.getValue();
                if (timeMap == null) timeMap = new HashMap<>();
                timeMap.put(taskId, millisUntilFinished);
                tasksTimeRemaining.setValue(timeMap);
            }

            @Override
            public void onFinish() {
                stopTimer(taskId); // Tự động stop khi hết giờ
            }
        }.start();

        timerMap.put(taskId, timer);
        // Việc put vào timerMap sẽ làm cho isTaskRunning trả về true

        // Trigger update runningTasks để UI refresh trạng thái nút
        // (Đây là trick để Observer bên Fragment nhận biết có sự thay đổi state)
        List<Task> currentList = runningTasks.getValue();
        runningTasks.setValue(currentList);
    }

    public void pauseTimer(String taskId) {
        if (timerMap.containsKey(taskId)) {
            timerMap.get(taskId).cancel();
            timerMap.remove(taskId);

            // Đánh dấu là đang pause
            pausedStateMap.put(taskId, true);

            // Vẫn giữ trong runningTasks để hiện trên UI Home (ở trạng thái pause)
            // Trigger update để UI refresh
            List<Task> currentList = runningTasks.getValue();
            runningTasks.setValue(currentList);
        }
    }

    public void stopTimer(String taskId) {
        // Hủy timer
        if (timerMap.containsKey(taskId)) {
            timerMap.get(taskId).cancel();
            timerMap.remove(taskId);
        }

        // Xóa trạng thái pause
        pausedStateMap.remove(taskId);

        // Xóa khỏi danh sách running tasks
        List<Task> currentList = runningTasks.getValue();
        if (currentList != null) {
            // Dùng removeIf (Java 8+) hoặc loop để xóa
            for (int i = 0; i < currentList.size(); i++) {
                if (currentList.get(i).getId().equals(taskId)) {
                    currentList.remove(i);
                    break;
                }
            }
            runningTasks.setValue(currentList);
        }

        // Reset thời gian còn lại về 0 (hoặc xóa khỏi map)
        Map<String, Long> timeMap = tasksTimeRemaining.getValue();
        if (timeMap != null) {
            timeMap.remove(taskId);
            tasksTimeRemaining.setValue(timeMap);
        }
    }
}
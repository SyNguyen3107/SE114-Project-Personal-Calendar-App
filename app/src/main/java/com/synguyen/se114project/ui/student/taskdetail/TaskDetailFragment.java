package com.synguyen.se114project.ui.student.taskdetail;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.synguyen.se114project.R;
import com.synguyen.se114project.data.entity.Subtask;
import com.synguyen.se114project.data.entity.Task;
import com.synguyen.se114project.ui.adapter.SubtaskAdapter;
import com.synguyen.se114project.viewmodel.student.TaskDetailViewModel;
import com.synguyen.se114project.viewmodel.student.TimerViewModel;
import com.synguyen.se114project.BuildConfig;
import com.synguyen.se114project.data.remote.RetrofitClient;
import com.synguyen.se114project.data.remote.SupabaseService;
import com.synguyen.se114project.utils.FileUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.io.File;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;



public class TaskDetailFragment extends Fragment {

    private TaskDetailViewModel mViewModel;
    private TimerViewModel timerViewModel;
    private SubtaskAdapter adapter;
    private String taskId = null;
    private Task currentTask;
    private List<Subtask> currentSubtasks = new ArrayList<>();

    // Views
    private TextView tvTitle, tvDeadline, tvDescription, tvTimerDisplay;
    private ProgressBar pbTimer, pbTaskProgress;
    private TextView tvProgressPercent;
    private RecyclerView rvSubtasks;
    private ImageView btnBack;
    private View btnMoreOptions;
    private Button btnStart, btnTakeBreak;
    // UI Upload
    private View btnUploadLayout;
    private TextView tvUploadStatus;
    private ProgressBar pbUpload;

    // Launcher chọn file
    private ActivityResultLauncher<Intent> filePickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            taskId = getArguments().getString("taskId");
        }
        // ĐĂNG KÝ LAUNCHER NHẬN FILE
                filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedUri = result.getData().getData();
                        if (selectedUri != null) {
                            uploadAssignment(selectedUri);
                        }
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ View
        tvTitle = view.findViewById(R.id.tvTaskTitle);
        tvDeadline = view.findViewById(R.id.tvTaskDeadline);
        tvDescription = view.findViewById(R.id.tvTaskDescription);

        // Timer Views
        tvTimerDisplay = view.findViewById(R.id.tv_timer_display);
        pbTimer = view.findViewById(R.id.pbTimer);
        btnStart = view.findViewById(R.id.btnStart);
        btnTakeBreak = view.findViewById(R.id.btnTakeBreak);

        // Subtask Views
        rvSubtasks = view.findViewById(R.id.rvSubtasks);
        pbTaskProgress = view.findViewById(R.id.pbTaskProgress);
        tvProgressPercent = view.findViewById(R.id.tvProgressPercent);

        // Navigation Views
        btnBack = view.findViewById(R.id.btnBack);
        btnMoreOptions = view.findViewById(R.id.btnEditSubtask);

        // Upload View
        btnUploadLayout = view.findViewById(R.id.layoutUpload);
        tvUploadStatus = view.findViewById(R.id.tvUploadStatus);
        pbUpload = view.findViewById(R.id.pbUpload);

        // 2. ViewModel
        mViewModel = new ViewModelProvider(this).get(TaskDetailViewModel.class);
        timerViewModel = new ViewModelProvider(requireActivity()).get(TimerViewModel.class);

        // 3. Setup RecyclerView
        setupRecyclerView();

        // 4. Load Data
        if (taskId != null) {
            loadTaskData();
        }

        // 5. Events
        btnBack.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());

        if (btnMoreOptions != null) {
            btnMoreOptions.setOnClickListener(v -> showEditSubtasksDialog());
        }
        if (btnUploadLayout != null) {
            btnUploadLayout.setOnClickListener(v -> openFilePicker());
        }

        // 6. Timer Logic
        setupTimerLogic();
    }

    // Helper: Lấy thời lượng của Task (mặc định 25p nếu chưa set)
    private long getTaskDuration() {
        if (currentTask != null && currentTask.getDuration() > 0) {
            return currentTask.getDuration();
        }
        return 25 * 60 * 1000; // Default 25 min
    }

    private void setupTimerLogic() {
        // Cấu hình Progress Bar Timer DỰA THEO DURATION CỦA TASK
        if (currentTask != null && currentTask.getDuration() > 0) {
            pbTimer.setMax((int) (currentTask.getDuration() / 1000));
        } else {
            pbTimer.setMax(25 * 60); // Fallback 25p
        }

        // --- OBSERVERS ---
        // Quan sát Active Task để biết Timer nào đang chạy
        timerViewModel.getTasksTimeRemaining().observe(getViewLifecycleOwner(), map -> {
            checkAndUpdateTimerUI(); // Cập nhật UI dựa trên ID của task hiện tại
        });

        // Quan sát List running tasks để biết trạng thái start/stop
        timerViewModel.getRunningTasks().observe(getViewLifecycleOwner(), list -> {
            checkAndUpdateTimerUI();
        });

        // --- BUTTONS ---
        // Nút START / STOP
        btnStart.setOnClickListener(v -> {
            if (currentTask == null) return;
            boolean isRunning = timerViewModel.isTaskRunning(currentTask.getId());
            boolean isPaused = timerViewModel.isTaskRunningOrPaused(currentTask.getId()) && !isRunning;

            if (isRunning || isPaused) {
                timerViewModel.stopTimer(currentTask.getId()); // Stop timer cụ thể
                Toast.makeText(getContext(), "Timer stopped", Toast.LENGTH_SHORT).show();
            } else {
                timerViewModel.startTimer(currentTask); // Start timer cụ thể
            }
        });

        // Nút TAKE BREAK / RESUME
        btnTakeBreak.setOnClickListener(v -> {
            if (currentTask == null) return;
            boolean isRunning = timerViewModel.isTaskRunning(currentTask.getId());
            if (isRunning) {
                timerViewModel.pauseTimer(currentTask.getId()); // Pause timer cụ thể
            } else {
                // Resume logic: Gọi startTimer sẽ tự resume nếu còn thời gian
                timerViewModel.startTimer(currentTask);
            }
        });
    }

    // Logic cốt lõi: Kiểm tra xem Timer toàn cục có khớp với Task hiện tại không
    private void checkAndUpdateTimerUI() {
        if (currentTask == null) return;

        // Kiểm tra xem task hiện tại có trong danh sách running/paused không
        boolean isRunning = timerViewModel.isTaskRunning(currentTask.getId());
        boolean isPaused = timerViewModel.isTaskRunningOrPaused(currentTask.getId()) && !isRunning;

        // Lấy thời gian còn lại từ Map
        long millis = timerViewModel.getTimeRemaining(currentTask.getId());

        if (millis > 0) {
            updateTimerUI(millis);
        } else {
            updateTimerUI(getTaskDuration());
        }

        updateButtonsState(isRunning, isPaused);
    }

    private void updateTimerUI(long millisLeft) {
        long minutes = (millisLeft / 1000) / 60;
        long seconds = (millisLeft / 1000) % 60;
        tvTimerDisplay.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));

        int secondsLeft = (int) (millisLeft / 1000);
        pbTimer.setProgress(secondsLeft);
    }

    private void updateButtonsState(boolean isRunning, boolean isPaused) {
        if (isRunning) {
            // --- TRẠNG THÁI: ĐANG CHẠY ---
            // Nút trái: Stop (Đỏ)
            btnStart.setText("Stop");
            btnStart.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));

            // Nút phải: Take Break (Trắng/Xanh - Enable) -> Để bấm Pause
            btnTakeBreak.setText("Take Break");
            btnTakeBreak.setEnabled(true);
            btnTakeBreak.setAlpha(1.0f);

        } else if (isPaused) {
            // --- TRẠNG THÁI: TẠM DỪNG (PAUSED) ---
            // Nút trái: Stop (Vẫn cho phép dừng hẳn nếu muốn)
            btnStart.setText("Stop");
            btnStart.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));

            // Nút phải: Resume (Enable) -> Để bấm Chạy tiếp
            btnTakeBreak.setText("Resume");
            btnTakeBreak.setEnabled(true);
            btnTakeBreak.setAlpha(1.0f);

        } else {
            // --- TRẠNG THÁI: CHƯA CHẠY / ĐÃ XONG (IDLE) ---
            // Nút trái: Start (Xanh)
            btnStart.setText("Start");
            btnStart.setBackgroundColor(0xFF304FFE); // Xanh #304FFE

            // Nút phải: Take Break (Disable) -> Chưa chạy thì ko break được
            btnTakeBreak.setText("Take Break");
            btnTakeBreak.setEnabled(false);
            btnTakeBreak.setAlpha(0.5f);
        }
    }

    private void resetButtonsToIdle() {
        btnStart.setText("Start");
        btnStart.setBackgroundColor(0xFF304FFE); // Xanh
        btnTakeBreak.setText("Take Break");
        btnTakeBreak.setEnabled(false);
        btnTakeBreak.setAlpha(0.5f);
    }

    private void setupRecyclerView() {
        rvSubtasks.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SubtaskAdapter();
        adapter.setOnSubtaskClickListener(new SubtaskAdapter.OnSubtaskActionListener() {
            @Override
            public void onCheck(Subtask subtask, boolean isChecked) {
                mViewModel.updateSubtaskStatus(subtask, isChecked);
            }
            @Override
            public void onDelete(Subtask subtask) {
                // Không xóa ở đây
            }
        });
        rvSubtasks.setAdapter(adapter);
    }

    private void loadTaskData() {
        mViewModel.getTaskById(taskId).observe(getViewLifecycleOwner(), task -> {
            if (task != null) {
                currentTask = task;
                tvTitle.setText(task.getTitle());
                tvDescription.setText(task.getSubTitle());

                if (task.getDate() > 0) {
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
                    tvDeadline.setText("Due: " + sdf.format(new Date(task.getDate())));
                }

                // Cập nhật Max cho Progress Bar
                long duration = getTaskDuration();
                pbTimer.setMax((int) (duration / 1000));

                // Khi mới load, kiểm tra ngay trạng thái Timer
                checkAndUpdateTimerUI();
            }
        });

        mViewModel.getSubtasksOfTask(taskId).observe(getViewLifecycleOwner(), subtasks -> {
            currentSubtasks = subtasks;
            adapter.submitList(subtasks);
            calculateProgress(subtasks);
        });
    }

    private void calculateProgress(List<Subtask> subtasks) {
        if (subtasks == null || subtasks.isEmpty()) {
            pbTaskProgress.setProgress(0);
            tvProgressPercent.setText("0%");
            return;
        }
        int completed = 0;
        for (Subtask s : subtasks) if (s.isCompleted()) completed++;

        int percent = (int) (((float) completed / subtasks.size()) * 100);
        pbTaskProgress.setProgress(percent);
        tvProgressPercent.setText(percent + "%");
    }

    private void showEditSubtasksDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_subtask, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        RecyclerView rvDialog = dialogView.findViewById(R.id.rv_dialog_subtasks);
        TextView btnAdd = dialogView.findViewById(R.id.btn_dialog_add_subtask);
        Button btnSave = dialogView.findViewById(R.id.btn_dialog_save);
        ImageView btnClose = dialogView.findViewById(R.id.btn_close_dialog);

        rvDialog.setLayoutManager(new LinearLayoutManager(getContext()));
        SubtaskAdapter dialogAdapter = new SubtaskAdapter();
        List<Subtask> tempSubtasks = new ArrayList<>(currentSubtasks);
        dialogAdapter.submitList(tempSubtasks);

        dialogAdapter.setOnSubtaskClickListener(new SubtaskAdapter.OnSubtaskActionListener() {
            @Override
            public void onCheck(Subtask subtask, boolean isChecked) {
                subtask.setCompleted(isChecked);
            }

            @Override
            public void onDelete(Subtask subtask) {
                List<Subtask> current = new ArrayList<>(dialogAdapter.getCurrentList());
                current.remove(subtask);
                dialogAdapter.submitList(current);
                mViewModel.deleteSubtask(subtask);
            }
        });
        rvDialog.setAdapter(dialogAdapter);

        btnAdd.setOnClickListener(v -> {
            if (currentTask != null) {
                Subtask newSub = new Subtask(currentTask.getId(), "New Subtask");
                mViewModel.addSubtask(newSub);
                List<Subtask> current = new ArrayList<>(dialogAdapter.getCurrentList());
                current.add(newSub);
                dialogAdapter.submitList(current);
            }
        });

        btnSave.setOnClickListener(v -> {
            for (Subtask s : dialogAdapter.getCurrentList()) {
                mViewModel.updateSubtaskStatus(s, s.isCompleted());
            }
            dialog.dismiss();
            Toast.makeText(getContext(), "Changes saved!", Toast.LENGTH_SHORT).show();
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        filePickerLauncher.launch(Intent.createChooser(intent, "Select Assignment PDF"));
    }

    // Hàm Upload lên Supabase
    private void uploadAssignment(Uri fileUri) {
        if (currentTask == null) return;

        pbUpload.setVisibility(View.VISIBLE);
        tvUploadStatus.setText("Uploading...");
        btnUploadLayout.setEnabled(false);

        try {
            Context context = requireContext();
            File file = FileUtils.getFileFromUri(context, fileUri);

            // Quy tắc đặt tên file: assign_{taskId}_{userId}.pdf
            SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
            String userId = prefs.getString("USER_ID", "unknown_user"); // Đảm bảo bạn đã lưu USER_ID khi login
            String token = prefs.getString("ACCESS_TOKEN", "");

            String fileNameOnServer = "assign_" + currentTask.getId() + "_" + userId + ".pdf";

            // Tạo RequestBody
            RequestBody requestFile = RequestBody.create(MediaType.parse("application/pdf"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", fileNameOnServer, requestFile);

            // Gọi API
            SupabaseService service = RetrofitClient.getRetrofitInstance().create(SupabaseService.class);
            service.uploadFile(BuildConfig.SUPABASE_KEY, "Bearer " + token, "assignments", fileNameOnServer, body)
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            pbUpload.setVisibility(View.GONE);
                            btnUploadLayout.setEnabled(true);

                            if (response.isSuccessful()) {
                                Toast.makeText(context, "Nộp bài thành công!", Toast.LENGTH_SHORT).show();
                                tvUploadStatus.setText("Đã nộp: " + file.getName());
                                tvUploadStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                            } else {
                                Toast.makeText(context, "Lỗi nộp bài: " + response.code(), Toast.LENGTH_SHORT).show();
                                tvUploadStatus.setText("Lỗi upload. Thử lại.");
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            pbUpload.setVisibility(View.GONE);
                            btnUploadLayout.setEnabled(true);
                            Toast.makeText(context, "Lỗi mạng", Toast.LENGTH_SHORT).show();
                        }
                    });

        } catch (Exception e) {
            pbUpload.setVisibility(View.GONE);
            btnUploadLayout.setEnabled(true);
            e.printStackTrace();
            Toast.makeText(getContext(), "Lỗi file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
package com.synguyen.se114project.ui.student.taskdetail;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.synguyen.se114project.BuildConfig;
import com.synguyen.se114project.R;
import com.synguyen.se114project.data.entity.Subtask;
import com.synguyen.se114project.data.entity.Task;
import com.synguyen.se114project.data.remote.RetrofitClient;
import com.synguyen.se114project.data.remote.SupabaseService;
import com.synguyen.se114project.ui.adapter.SubtaskAdapter;
import com.synguyen.se114project.utils.FileUtils;
import com.synguyen.se114project.viewmodel.student.TaskDetailViewModel;
import com.synguyen.se114project.viewmodel.student.TimerViewModel;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

    // UI Upload (Sử dụng layoutUpload có sẵn trong XML)
    private View btnUploadLayout;
    private TextView tvUploadStatus;
    private ProgressBar pbUpload;

    // Biến lưu file đã chọn
    private Uri selectedFileUri;

    // Launcher chọn file PDF
    private final ActivityResultLauncher<String> pickFileLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedFileUri = uri;
                    String fileName = FileUtils.getFileName(requireContext(), uri);

                    // Sau khi chọn file, hiện Dialog xác nhận nộp ngay
                    showConfirmUploadDialog(fileName);
                }
            }
    );

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            taskId = getArguments().getString("taskId");
        }
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

        // Upload Views (Mapping đúng với ID trong XML)
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

        // Sự kiện Upload: Bấm vào layout -> Mở trình chọn file
        if (btnUploadLayout != null) {
            btnUploadLayout.setOnClickListener(v -> openFilePicker());
        }

        // 6. Timer Logic
        setupTimerLogic();
    }

    // --- CÁC HÀM XỬ LÝ UPLOAD FILE ---

    private void openFilePicker() {
        // Chỉ chọn file PDF (hoặc thay đổi MIME type nếu cần)
        pickFileLauncher.launch("application/pdf");
    }

    private void showConfirmUploadDialog(String fileName) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận nộp bài")
                .setMessage("Bạn có muốn nộp file này không?\n\n" + fileName)
                .setPositiveButton("Nộp ngay", (dialog, which) -> {
                    uploadSubmission();
                })
                .setNegativeButton("Hủy", (dialog, which) -> {
                    selectedFileUri = null; // Reset chọn file
                })
                .show();
    }

    private void uploadSubmission() {
        if (selectedFileUri == null || currentTask == null) return;

        // UI Loading
        pbUpload.setVisibility(View.VISIBLE);
        tvUploadStatus.setText("Đang tải lên...");
        btnUploadLayout.setEnabled(false); // Khóa nút bấm

        try {
            // 1. Chuyển Uri thành File thật
            File file = FileUtils.getFileFromUri(requireContext(), selectedFileUri);

            // 2. Tạo RequestBody (PDF)
            RequestBody requestFile = RequestBody.create(MediaType.parse("application/pdf"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

            // 3. Lấy thông tin User & Token
            SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
            String token = prefs.getString("ACCESS_TOKEN", "");
            String userId = prefs.getString("USER_ID", "");

            // 4. Tạo tên file unique: assign_{taskId}_{userId}_{timestamp}.pdf
            String serverFileName = "assign_" + currentTask.getId() + "_" + userId + "_" + System.currentTimeMillis() + ".pdf";

            // 5. Gọi API
            SupabaseService service = RetrofitClient.getRetrofitInstance().create(SupabaseService.class);
            service.uploadFile(BuildConfig.SUPABASE_KEY, "Bearer " + token, "assignments", serverFileName, body)
                    .enqueue(new Callback<ResponseBody>() { // Dùng ResponseBody
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            // Reset UI state
                            pbUpload.setVisibility(View.GONE);
                            btnUploadLayout.setEnabled(true);

                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "Nộp bài thành công!", Toast.LENGTH_SHORT).show();
                                tvUploadStatus.setText("Đã nộp: " + file.getName());
                                tvUploadStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));

                                // Clear uri để tránh nộp lại file cũ
                                selectedFileUri = null;
                            } else {
                                Toast.makeText(getContext(), "Lỗi upload: " + response.code(), Toast.LENGTH_SHORT).show();
                                tvUploadStatus.setText("Lỗi tải lên. Nhấn để thử lại.");
                                tvUploadStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            pbUpload.setVisibility(View.GONE);
                            btnUploadLayout.setEnabled(true);
                            Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            tvUploadStatus.setText("Lỗi mạng. Nhấn để thử lại.");
                        }
                    });

        } catch (Exception e) {
            pbUpload.setVisibility(View.GONE);
            btnUploadLayout.setEnabled(true);
            e.printStackTrace();
            Toast.makeText(getContext(), "Lỗi file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // --- CÁC HÀM LOGIC CŨ (GIỮ NGUYÊN) ---

    private long getTaskDuration() {
        if (currentTask != null && currentTask.getDuration() > 0) {
            return currentTask.getDuration();
        }
        return 25 * 60 * 1000; // Default 25 min
    }

    private void setupTimerLogic() {
        if (currentTask != null && currentTask.getDuration() > 0) {
            pbTimer.setMax((int) (currentTask.getDuration() / 1000));
        } else {
            pbTimer.setMax(25 * 60);
        }

        timerViewModel.getTasksTimeRemaining().observe(getViewLifecycleOwner(), map -> checkAndUpdateTimerUI());
        timerViewModel.getRunningTasks().observe(getViewLifecycleOwner(), list -> checkAndUpdateTimerUI());

        btnStart.setOnClickListener(v -> {
            if (currentTask == null) return;
            boolean isRunning = timerViewModel.isTaskRunning(currentTask.getId());
            boolean isPaused = timerViewModel.isTaskRunningOrPaused(currentTask.getId()) && !isRunning;

            if (isRunning || isPaused) {
                timerViewModel.stopTimer(currentTask.getId());
                Toast.makeText(getContext(), "Timer stopped", Toast.LENGTH_SHORT).show();
            } else {
                timerViewModel.startTimer(currentTask);
            }
        });

        btnTakeBreak.setOnClickListener(v -> {
            if (currentTask == null) return;
            boolean isRunning = timerViewModel.isTaskRunning(currentTask.getId());
            if (isRunning) {
                timerViewModel.pauseTimer(currentTask.getId());
            } else {
                timerViewModel.startTimer(currentTask);
            }
        });
    }

    private void checkAndUpdateTimerUI() {
        if (currentTask == null) return;

        boolean isRunning = timerViewModel.isTaskRunning(currentTask.getId());
        boolean isPaused = timerViewModel.isTaskRunningOrPaused(currentTask.getId()) && !isRunning;
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
        pbTimer.setProgress((int) (millisLeft / 1000));
    }

    private void updateButtonsState(boolean isRunning, boolean isPaused) {
        if (isRunning) {
            btnStart.setText("Stop");
            btnStart.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
            btnTakeBreak.setText("Take Break");
            btnTakeBreak.setEnabled(true);
            btnTakeBreak.setAlpha(1.0f);
        } else if (isPaused) {
            btnStart.setText("Stop");
            btnStart.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
            btnTakeBreak.setText("Resume");
            btnTakeBreak.setEnabled(true);
            btnTakeBreak.setAlpha(1.0f);
        } else {
            btnStart.setText("Start");
            btnStart.setBackgroundColor(0xFF304FFE);
            btnTakeBreak.setText("Take Break");
            btnTakeBreak.setEnabled(false);
            btnTakeBreak.setAlpha(0.5f);
        }
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
            public void onDelete(Subtask subtask) {}
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

                // Update Max Timer
                long duration = getTaskDuration();
                pbTimer.setMax((int) (duration / 1000));

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
}
package com.synguyen.se114project.ui.student.taskdetail;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

import com.google.gson.JsonObject;
import com.synguyen.se114project.BuildConfig;
import com.synguyen.se114project.R;
import com.synguyen.se114project.data.entity.Subtask;
import com.synguyen.se114project.data.entity.Task;
import com.synguyen.se114project.data.remote.RetrofitClient;
import com.synguyen.se114project.data.remote.SupabaseService;
import com.synguyen.se114project.data.remote.response.FileObject;
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

public class StudentTaskDetailFragment extends Fragment {

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
    private View btnEditSubtask;
    private Button btnStart, btnTakeBreak;
    private View btnUploadLayout;
    private TextView tvUploadStatus;
    private ProgressBar pbUpload;
    private TextView btnReadMore;
    private String currentDescription = ""; // Stores current description content
    private String taskTitle = "";
    private Uri selectedFileUri;
    private Button btnDeleteTask;

    // File Selection Launcher
    private final ActivityResultLauncher<String> pickFileLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedFileUri = uri;
                    String fileName = FileUtils.getFileName(requireContext(), uri);

                    // Show confirmation dialog after selecting file
                    showConfirmUploadDialog(fileName);
                }
            }
    );

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            taskId = getArguments().getString("taskId");
            taskTitle = getArguments().getString("taskTitle");
            // Optionally load initial description from arguments if passed
            // currentDescription = getArguments().getString("taskDescription");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_student_task_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Map Views
        tvTitle = view.findViewById(R.id.tvTaskTitle);
        tvDeadline = view.findViewById(R.id.tvTaskDeadline);
        tvDescription = view.findViewById(R.id.tvTaskDescription);
        btnReadMore = view.findViewById(R.id.btnReadMore);

        btnReadMore.setOnClickListener(v -> showEditDescriptionDialog());

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
        btnEditSubtask = view.findViewById(R.id.btnEditSubtask);

        // Upload Views (Mapping matches XML IDs)
        btnUploadLayout = view.findViewById(R.id.layoutUpload);
        tvUploadStatus = view.findViewById(R.id.tvUploadStatus);
        pbUpload = view.findViewById(R.id.pbUpload);
        btnDeleteTask = view.findViewById(R.id.btnDeleteTask);

        // 3. Gán sự kiện
        btnDeleteTask.setOnClickListener(v -> showConfirmDeleteDialog());
        // 2. ViewModel
        mViewModel = new ViewModelProvider(this).get(TaskDetailViewModel.class);
        timerViewModel = new ViewModelProvider(requireActivity()).get(TimerViewModel.class);

        // 3. Setup RecyclerView
        setupRecyclerView();

        // 4. Load Data
        if (taskId != null) {
            loadTaskData();
            checkExistingSubmission();
        }

        // 5. Events
        btnBack.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());

        if (btnEditSubtask != null) {
            btnEditSubtask.setOnClickListener(v -> showEditSubtasksDialog());
        }

        // Upload Event: Click layout -> Open file picker
        if (btnUploadLayout != null) {
            btnUploadLayout.setOnClickListener(v -> openFilePicker());
        }

        // 6. Timer Logic
        setupTimerLogic();
    }

    // --- FILE UPLOAD LOGIC ---

    private void openFilePicker() {
        // Only select PDF files
        pickFileLauncher.launch("application/pdf");
    }

    private void showConfirmUploadDialog(String fileName) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm Submission")
                .setMessage("Do you want to submit this file?\n\n" + fileName)
                .setPositiveButton("Submit Now", (dialog, which) -> {
                    uploadSubmission();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    selectedFileUri = null; // Reset selection
                })
                .show();
    }

    private void deleteTaskAPI() {
        if (taskId == null) return;

        // Loading UI (Optional: Bạn có thể hiện ProgressBar nếu muốn)
        btnDeleteTask.setEnabled(false);
        btnDeleteTask.setText("Deleting...");

        SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("ACCESS_TOKEN", "");

        SupabaseService service = RetrofitClient.getRetrofitInstance().create(SupabaseService.class);

        // Gọi API DELETE: tasks?id=eq.{taskId}
        service.deleteTask(BuildConfig.SUPABASE_KEY, "Bearer " + token, "eq." + taskId)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (!isAdded()) return;

                        // Supabase trả về 204 No Content khi xoá thành công
                        if (response.isSuccessful() || response.code() == 204) {

                            // A. Xoá khỏi Local Database (Room) thông qua ViewModel
                            if (currentTask != null) {
                                mViewModel.deleteTask(currentTask);
                            }

                            Toast.makeText(getContext(), "Task deleted successfully", Toast.LENGTH_SHORT).show();

                            // B. Thoát màn hình Detail, quay về Home
                            // Vì Home dùng LiveData quan sát Room, danh sách sẽ tự cập nhật
                            Navigation.findNavController(requireView()).navigateUp();

                        } else {
                            btnDeleteTask.setEnabled(true);
                            btnDeleteTask.setText("Delete this task");
                            Toast.makeText(getContext(), "Failed to delete: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        if (isAdded()) {
                            btnDeleteTask.setEnabled(true);
                            btnDeleteTask.setText("Delete this task");
                            Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void showEditDescriptionDialog() {
        // Kiểm tra an toàn: Nếu Task chưa load xong thì không hiện dialog
        if (currentTask == null) {
            Toast.makeText(getContext(), "Đang tải dữ liệu...", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Tạo View cho Dialog
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_description, null);

        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        EditText edtDescription = dialogView.findViewById(R.id.edtDescription);
        Button btnSave = dialogView.findViewById(R.id.btnSaveDescription);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelDescription); // Ánh xạ nút Cancel mới

        // 2. Thiết lập dữ liệu (Dùng currentTask.getTitle() thay vì biến taskTitle cũ có thể bị null)
        String safeTitle = (currentTask.getTitle() != null) ? currentTask.getTitle() : "Task";
        tvDialogTitle.setText(safeTitle + "'s Description");

        // Hiển thị description hiện tại
        edtDescription.setText(currentDescription);

        // 3. Khởi tạo AlertDialog
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        // Làm nền dialog trong suốt để bo góc đẹp hơn (Optional)
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // 4. Xử lý sự kiện nút Save
        btnSave.setOnClickListener(v -> {
            String newDesc = edtDescription.getText().toString().trim();
            updateDescriptionAPI(newDesc, dialog);
        });

        // 5. Xử lý sự kiện nút Cancel
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void checkExistingSubmission() {
        if (taskId == null) return;

        SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("ACCESS_TOKEN", "");
        String userId = prefs.getString("USER_ID", "");

        JsonObject body = new JsonObject();
        body.addProperty("prefix", "assign_" + taskId + "_" + userId);

        SupabaseService service = RetrofitClient.getRetrofitInstance().create(SupabaseService.class);
        service.listFiles(BuildConfig.SUPABASE_KEY, "Bearer " + token, "assignments", body)
                .enqueue(new Callback<List<FileObject>>() {
                    @Override
                    public void onResponse(Call<List<FileObject>> call, Response<List<FileObject>> response) {
                        if (isAdded() && response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            FileObject existingFile = response.body().get(0);
                            tvUploadStatus.setText("Submitted: " + existingFile.name);
                            tvUploadStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                        }
                    }

                    @Override
                    public void onFailure(Call<List<FileObject>> call, Throwable t) {
                    }
                });
    }

    private void updateDescriptionAPI(String newDesc, AlertDialog dialog) {
        // 1. Lấy token
        SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("ACCESS_TOKEN", "");

        SupabaseService service = RetrofitClient.getRetrofitInstance().create(SupabaseService.class);

        // 2. Tạo object chỉ chứa field cần update để gửi lên server
        Task updateData = new Task();
        updateData.setDescription(newDesc);

        // 3. Gọi API
        service.updateTask(BuildConfig.SUPABASE_KEY, "Bearer " + token, "eq." + taskId, updateData)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (!isAdded()) return;

                        if (response.isSuccessful()) {
                            // --- CẬP NHẬT UI ---
                            currentDescription = newDesc;
                            tvDescription.setText(newDesc);

                            // --- [QUAN TRỌNG] CẬP NHẬT LOCAL DATABASE ---
                            if (currentTask != null) {
                                currentTask.setDescription(newDesc);
                                // Gọi ViewModel để lưu vào Room.
                                // Việc này sẽ tự động làm mới HomeFragment và cache lại dữ liệu mới.
                                mViewModel.updateTask(currentTask);
                            }

                            dialog.dismiss();
                            Toast.makeText(getContext(), "Đã lưu thay đổi!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        if (isAdded()) {
                            Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void uploadSubmission() {
        if (selectedFileUri == null || currentTask == null) return;

        // UI Loading
        pbUpload.setVisibility(View.VISIBLE);
        tvUploadStatus.setText("Uploading...");
        btnUploadLayout.setEnabled(false); // Lock button

        try {
            // 1. Convert Uri to real File
            File file = FileUtils.getFileFromUri(requireContext(), selectedFileUri);

            // 2. Create RequestBody (PDF)
            RequestBody requestFile = RequestBody.create(MediaType.parse("application/pdf"), file);

            // 3. Get User Info & Token
            SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
            String token = prefs.getString("ACCESS_TOKEN", "");
            String userId = prefs.getString("USER_ID", "");

            // 4. Create unique file name using userId to support overwriting
            String serverFileName = "assign_" + currentTask.getId() + "_" + userId + ".pdf";

            // 5. Call API with x-upsert: true
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", serverFileName, requestFile);

            SupabaseService service = RetrofitClient.getRetrofitInstance().create(SupabaseService.class);
            service.uploadFile(BuildConfig.SUPABASE_KEY, "Bearer " + token, "true", "assignments", serverFileName, body)
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            // Reset UI state
                            pbUpload.setVisibility(View.GONE);
                            btnUploadLayout.setEnabled(true);

                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "Submission successful!", Toast.LENGTH_SHORT).show();
                                tvUploadStatus.setText("Submitted: " + file.getName());
                                tvUploadStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));

                                // Clear uri to avoid re-uploading old file
                                selectedFileUri = null;
                            } else {
                                Toast.makeText(getContext(), "Upload error: " + response.code(), Toast.LENGTH_SHORT).show();
                                tvUploadStatus.setText("Upload failed. Tap to retry.");
                                tvUploadStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            pbUpload.setVisibility(View.GONE);
                            btnUploadLayout.setEnabled(true);
                            Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            tvUploadStatus.setText("Network error. Tap to retry.");
                        }
                    });

        } catch (Exception e) {
            pbUpload.setVisibility(View.GONE);
            btnUploadLayout.setEnabled(true);
            e.printStackTrace();
            Toast.makeText(getContext(), "File error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // --- TIMER & OTHER LOGIC (UNCHANGED) ---

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
            public void onDelete(Subtask subtask) {
                // Not implemented on main screen
            }

            @Override
            public void onEdit(Subtask subtask) {
                // Optional: Implement edit logic here if desired
            }
        });
        rvSubtasks.setAdapter(adapter);
    }

    private void loadTaskData() {
        mViewModel.getTaskById(taskId).observe(getViewLifecycleOwner(), task -> {
            if (task != null) {
                currentTask = task;
                tvTitle.setText(task.getTitle());
                // Update description from DB in case it changed
                if (task.getDescription() != null) {
                    currentDescription = task.getDescription();
                    tvDescription.setText(currentDescription);
                }

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

        // Create copy of list to modify in dialog
        List<Subtask> tempSubtasks = new ArrayList<>();
        if (currentSubtasks != null) {
            for (Subtask original : currentSubtasks) {
                Subtask copy = new Subtask();
                copy.setId(original.getId());
                copy.setTaskId(original.getTaskId());
                copy.setTitle(original.getTitle());
                copy.setCompleted(original.isCompleted());
                tempSubtasks.add(copy);
            }
        }

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
                mViewModel.deleteSubtask(subtask); // Delete immediately from DB
            }

            @Override
            public void onEdit(Subtask subtask) {
                showRenameSubtaskDialog(subtask, dialogAdapter);
            }
        });
        rvDialog.setAdapter(dialogAdapter);

        btnAdd.setOnClickListener(v -> {
            if (currentTask != null) {
                Subtask newSub = new Subtask(currentTask.getId(), "New Subtask");
                mViewModel.addSubtask(newSub); // Add immediately to DB
                List<Subtask> current = new ArrayList<>(dialogAdapter.getCurrentList());
                current.add(newSub);
                dialogAdapter.submitList(current);
            }
        });

        btnSave.setOnClickListener(v -> {
            // Save completion status
            for (Subtask s : dialogAdapter.getCurrentList()) {
                mViewModel.updateSubtaskStatus(s, s.isCompleted());
            }
            dialog.dismiss();
            Toast.makeText(getContext(), "Changes saved!", Toast.LENGTH_SHORT).show();
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showConfirmDeleteDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Thực hiện xoá
                    deleteTaskAPI();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void showRenameSubtaskDialog(Subtask subtask, SubtaskAdapter adapter) {
        android.widget.EditText editText = new android.widget.EditText(requireContext());
        editText.setText(subtask.getTitle());
        editText.setPadding(50, 30, 50, 30);

        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Rename Subtask")
                .setView(editText)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newTitle = editText.getText().toString().trim();
                    if (!newTitle.isEmpty()) {
                        subtask.setTitle(newTitle);
                        mViewModel.updateSubtaskTitle(subtask); // Ensure this method exists in ViewModel
                        adapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
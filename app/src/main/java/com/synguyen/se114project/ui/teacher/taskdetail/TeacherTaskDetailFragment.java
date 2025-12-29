package com.synguyen.se114project.ui.teacher.taskdetail;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.synguyen.se114project.R;
import com.synguyen.se114project.data.entity.Subtask;
import com.synguyen.se114project.data.remote.RetrofitClient;
import com.synguyen.se114project.data.remote.SupabaseService;
import com.synguyen.se114project.ui.adapter.SubtaskAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TeacherTaskDetailFragment extends Fragment {

    private String taskId;
    private String taskTitle;
    private String token;

    // Views
    private TextView tvTimerDisplay, tvTitle, tvDeadline, tvDescription;
    private ProgressBar pbTimer, pbTaskProgress;
    private Button btnStart, btnPause;
    private RecyclerView rcvSubtasks;
    private ImageView btnBack;
    private TextView btnEditSubtask;

    // Timer Logic
    private CountDownTimer timer;
    private long totalTime = 25 * 60 * 1000; // 25 phút mặc định
    private long timeLeft = totalTime;
    private boolean isTimerRunning = false;

    // Data
    private SubtaskAdapter adapter;
    private List<Subtask> subtaskList = new ArrayList<>();

    public static TeacherTaskDetailFragment newInstance(String taskId, String title) {
        TeacherTaskDetailFragment fragment = new TeacherTaskDetailFragment();
        Bundle args = new Bundle();
        // Use lowercase keys to match nav graph and onCreateView reading
        args.putString("taskId", taskId);
        args.putString("taskTitle", title);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teacher_task_detail, container, false);

        if (getArguments() != null) {
            taskId = getArguments().getString("taskId");
            taskTitle = getArguments().getString("taskTitle");
        }

        SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        token = prefs.getString("ACCESS_TOKEN", null);

        tvTitle = view.findViewById(R.id.tvTaskTitle);
        tvTimerDisplay = view.findViewById(R.id.tv_timer_display);
        pbTimer = view.findViewById(R.id.pbTimer);
        btnStart = view.findViewById(R.id.btnStart);
        btnPause = view.findViewById(R.id.btnTakeBreak); // Ánh xạ nút "Take break" làm nút Pause
        rcvSubtasks = view.findViewById(R.id.rvSubtasks);
        btnBack = view.findViewById(R.id.btnBack);
        btnEditSubtask = view.findViewById(R.id.btnEditSubtask); // Dùng nút Edit làm chức năng thêm/sửa

        btnEditSubtask.setOnClickListener(v -> showAddSubtaskDialog());

        // Setup UI
        tvTitle.setText(taskTitle);
        setupTimerUI();
        setupRecyclerView();
        loadSubtasks();

        // Events
        btnBack.setOnClickListener(v -> {
            androidx.navigation.Navigation.findNavController(v).navigateUp();
        });

        btnStart.setOnClickListener(v -> startTimer());
        btnPause.setOnClickListener(v -> pauseTimer());
        btnEditSubtask.setOnClickListener(v -> showAddSubtaskDialog());

        return view;
    }

    // --- TIMER LOGIC ---
    private void setupTimerUI() {
        updateCountDownText();
        pbTimer.setMax((int) totalTime / 1000);
        pbTimer.setProgress((int) timeLeft / 1000);
    }

    private void startTimer() {
        if (isTimerRunning) return;

        timer = new CountDownTimer(timeLeft, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft = millisUntilFinished;
                updateCountDownText();
                pbTimer.setProgress((int) timeLeft / 1000);
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                btnStart.setText("Start");
                Toast.makeText(getContext(), "Hết giờ!", Toast.LENGTH_SHORT).show();
            }
        }.start();

        isTimerRunning = true;
        btnStart.setEnabled(false);
        btnPause.setEnabled(true);
    }

    private void pauseTimer() {
        if (!isTimerRunning) return;
        timer.cancel();
        isTimerRunning = false;
        btnStart.setEnabled(true);
        btnPause.setEnabled(false);
    }

    private void updateCountDownText() {
        int minutes = (int) (timeLeft / 1000) / 60;
        int seconds = (int) (timeLeft / 1000) % 60;
        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        tvTimerDisplay.setText(timeFormatted);
    }

    // --- SUBTASK LOGIC ---
    private void setupRecyclerView() {
        rcvSubtasks.setLayoutManager(new LinearLayoutManager(getContext()));
        // Tái sử dụng SubtaskAdapter của bạn
        adapter = new SubtaskAdapter();
        adapter.setOnSubtaskClickListener(new SubtaskAdapter.OnSubtaskActionListener() {
            @Override
            public void onCheck(Subtask subtask, boolean isChecked) {
                // Cập nhật lên Server
                updateSubtaskAPI(subtask, isChecked);
            }
            @Override
            public void onDelete(Subtask subtask) {
                // Xóa (nếu adapter có nút xóa)
                deleteSubtaskAPI(subtask);
            }

            @Override
            public void onEdit(Subtask subtask) {

            }
        });
        rcvSubtasks.setAdapter(adapter);
    }

    private void loadSubtasks() {
        SupabaseService service = RetrofitClient.getRetrofitInstance().create(SupabaseService.class);
//        service.getSubtasks(BuildConfig.SUPABASE_KEY, "Bearer " + token, "eq." + taskId)
//                .enqueue(new Callback<List<Subtask>>() {
//                    @Override
//                    public void onResponse(Call<List<Subtask>> call, Response<List<Subtask>> response) {
//                        if (response.isSuccessful() && response.body() != null) {
//                            subtaskList = response.body();
//                            adapter.submitList(subtaskList);
//                        }
//                    }
//                    @Override
//                    public void onFailure(Call<List<Subtask>> call, Throwable t) {}
//                });
    }

    private void showAddSubtaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Thêm công việc con");

        final EditText input = new EditText(getContext());
        input.setHint("Nhập tên công việc...");
        builder.setView(input);

        builder.setPositiveButton("Thêm", (dialog, which) -> {
            String title = input.getText().toString();
            if (!title.isEmpty()) {
                createSubtaskAPI(title);
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // --- API CALLS ---
    private void createSubtaskAPI(String title) {
        Subtask newSub = new Subtask(taskId, title);

        SupabaseService service = RetrofitClient.getRetrofitInstance().create(SupabaseService.class);
//        service.createSubtask(BuildConfig.SUPABASE_KEY, "Bearer " + token, newSub)
//                .enqueue(new Callback<Void>() {
//                    @Override
//                    public void onResponse(Call<Void> call, Response<Void> response) {
//                        if (response.isSuccessful()) {
//                            loadSubtasks(); // Load lại list
//                        }
//                    }
//                    @Override
//                    public void onFailure(Call<Void> call, Throwable t) {}
//                });
    }

    private void updateSubtaskAPI(Subtask subtask, boolean isChecked) {
        subtask.setCompleted(isChecked);
        // Gọi API PATCH (updateSubtask)
        // Lưu ý: Cần thêm API PATCH vào SupabaseService như hướng dẫn ở Bước 2
    }

    private void deleteSubtaskAPI(Subtask subtask) {
        // Gọi API DELETE
    }
}
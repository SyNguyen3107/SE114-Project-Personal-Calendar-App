package com.synguyen.se114project.ui.taskdetail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.synguyen.se114project.R;
import com.synguyen.se114project.data.entity.Task;
import com.synguyen.se114project.ui.adapter.SubtaskAdapter;
import com.synguyen.se114project.viewmodel.MainViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class TaskDetailFragment extends Fragment {

    private MainViewModel mainViewModel;
    private SubtaskAdapter adapter;
    private long taskId = -1;

    // Views (Cập nhật theo layout mới)
    private TextView tvTitle, tvDeadline, tvDescription;
    private RecyclerView rvSubtasks;
    private ImageView btnBack;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            taskId = getArguments().getLong("taskId", -1);
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

        // 1. Ánh xạ View (Updated IDs)
        tvTitle = view.findViewById(R.id.tvTaskTitle);
        tvDeadline = view.findViewById(R.id.tvTaskDeadline); // Thay cho tvDate/tvTime
        tvDescription = view.findViewById(R.id.tvTaskDescription); // Thay cho tvSubtitle

        rvSubtasks = view.findViewById(R.id.rvSubtasks);
        btnBack = view.findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());

        // 2. Setup RecyclerView Subtasks
        rvSubtasks.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SubtaskAdapter(new ArrayList<>(), (subtask, isChecked) -> {
            mainViewModel.updateSubtask(subtask);
        });
        rvSubtasks.setAdapter(adapter);

        // 3. ViewModel
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        // 4. Lấy dữ liệu Task cha
        mainViewModel.getAllTasks().observe(getViewLifecycleOwner(), tasks -> {
            for (Task t : tasks) {
                if (t.getId() == taskId) {
                    displayTaskInfo(t);
                    break;
                }
            }
        });

        // 5. Lấy danh sách Subtask con
        mainViewModel.getSubtasksOfTask((int) taskId).observe(getViewLifecycleOwner(), subtasks -> {
            adapter.setSubtasks(subtasks);
        });
    }

    private void displayTaskInfo(Task task) {
        if (task == null) return;

        // Set Title
        tvTitle.setText(task.getTitle());

        // Set Description (Lấy từ Subtitle)
        tvDescription.setText(task.getSubTitle());

        // Set Deadline (Gộp Date và Time lại)
        // Format: "Due: Nov 30th 2019"
        long dateTimestamp = task.getDate();
        if (dateTimestamp > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy", Locale.ENGLISH);
            String dateString = sdf.format(new Date(dateTimestamp));
            tvDeadline.setText("Due: " + dateString);
        } else {
            tvDeadline.setText("");
        }
    }
}
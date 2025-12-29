package com.synguyen.se114project.ui.teacher;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.synguyen.se114project.BuildConfig;
import com.synguyen.se114project.R;
import com.synguyen.se114project.data.entity.Task;
import com.synguyen.se114project.data.remote.RetrofitClient;
import com.synguyen.se114project.data.remote.SupabaseService;
import com.synguyen.se114project.ui.adapter.TeacherTaskAdapter;
import com.synguyen.se114project.ui.teacher.taskdetail.TeacherTaskDetailFragment;
import android.util.Log;
import androidx.navigation.Navigation;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherTasksFragment extends Fragment {

    private String courseId;
    private RecyclerView rcvTasks;
    private TeacherTaskAdapter adapter;
    private View fabAdd;
    private String token;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teacher_tasks, container, false);

        if (getArguments() != null) {
            courseId = getArguments().getString("COURSE_ID");
        }

        SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        token = prefs.getString("ACCESS_TOKEN", null);
        userId = prefs.getString("USER_ID", null);

        rcvTasks = view.findViewById(R.id.rcvTasks);
        fabAdd = view.findViewById(R.id.fabAddTask);

        rcvTasks.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TeacherTaskAdapter(new ArrayList<>(), task -> {
            try {
                Bundle bundle = new Bundle();
                bundle.putString("taskId", task.getId());
                bundle.putString("taskTitle", task.getTitle());
                Navigation.findNavController(requireView()).navigate(R.id.teacherTaskDetailFragment, bundle);
            } catch (Exception e) {
                Log.e("TeacherTasksFragment", "Error navigating to task detail", e);
                Toast.makeText(getContext(), "Cannot open task details", Toast.LENGTH_SHORT).show();
            }
        });
        rcvTasks.setAdapter(adapter);

        loadTasks();

        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> showAddTaskDialog());
        }

        return view;
    }

    private void loadTasks() {
        if (token == null || courseId == null) return;
        SupabaseService service = RetrofitClient.getRetrofitInstance().create(SupabaseService.class);
        service.getTasksByCourse( "Bearer " + token, "eq." + courseId)
                .enqueue(new Callback<List<Task>>() {
                    @Override
                    public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            adapter.updateData(response.body());
                        }
                    }
                    @Override
                    public void onFailure(Call<List<Task>> call, Throwable t) {
                        if (getContext() != null) Toast.makeText(getContext(), "Connection error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showAddTaskDialog() {
        if (getContext() == null) return;

        View v = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_task, null);
        final EditText edtTitle = v.findViewById(R.id.edtTaskTitle);
        final EditText edtDeadline = v.findViewById(R.id.edtTaskDeadline);
        final EditText edtDesc = v.findViewById(R.id.edtTaskDesc);
        Button btnCancel = v.findViewById(R.id.btnCancel);
        Button btnAssign = v.findViewById(R.id.btnAssign);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(v)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        btnCancel.setOnClickListener(view -> dialog.dismiss());

        btnAssign.setOnClickListener(view -> {
            String title = edtTitle.getText().toString().trim();
            String deadline = edtDeadline.getText().toString().trim();
            String desc = edtDesc.getText().toString().trim();

            if (!title.isEmpty()) {
                createTaskAPI(title, deadline, desc);
                dialog.dismiss();
            } else {
                edtTitle.setError("Title is required");
            }
        });

        dialog.show();
    }

    private void createTaskAPI(String title, String deadline, String desc) {
        SupabaseService service = RetrofitClient.getRetrofitInstance().create(SupabaseService.class);

        Task task = new Task();
        task.setTitle(title);
        task.setTime(deadline);
        task.setDescription(desc);
        task.setCourseId(courseId);
        task.setOwnerId(userId);
        task.setPriority(1);

        service.createTask( "Bearer " + token, task).enqueue(new Callback<List<Task>>() {
            @Override
            public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Task assigned successfully!", Toast.LENGTH_SHORT).show();
                    loadTasks();
                }
            }
            @Override
            public void onFailure(Call<List<Task>> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to assign task", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
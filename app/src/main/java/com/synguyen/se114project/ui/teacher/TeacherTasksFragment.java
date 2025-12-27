package com.synguyen.se114project.ui.teacher;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherTasksFragment extends Fragment {

    private String courseId;
    private RecyclerView rcvTasks;
    private TeacherTaskAdapter adapter;
    private FloatingActionButton fabAdd;
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

        // Khởi tạo Adapter
        adapter = new TeacherTaskAdapter(new ArrayList<>(), task -> {
            // Mở màn hình chi tiết (Đảm bảo bạn đã tạo Activity này)
            Intent intent = new Intent(getContext(), TeacherTaskDetailFragment.class);
            intent.putExtra("TASK_ID", task.getId());
            intent.putExtra("TASK_TITLE", task.getTitle());
            intent.putExtra("TASK_DESC", task.getDescription());
            intent.putExtra("TASK_DEADLINE", task.getTime());
            startActivity(intent);
        });
        rcvTasks.setAdapter(adapter);

        loadTasks();

        fabAdd.setOnClickListener(v -> showAddTaskDialog());

        return view;
    }

    private void loadTasks() {
        if (token == null || courseId == null) return;

        SupabaseService service = RetrofitClient.getRetrofitInstance().create(SupabaseService.class);

        // SỬA: Thêm "Bearer " trước token và "eq." trước courseId
        service.getTasksByCourse(BuildConfig.SUPABASE_KEY, "Bearer " + token, "eq." + courseId)
                .enqueue(new Callback<List<Task>>() {
                    @Override
                    public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Task> tasks = response.body();
                            // QUAN TRỌNG: Phải gọi hàm này để danh sách hiện lên màn hình
                            adapter.updateData(tasks);
                        } else {
                            Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Task>> call, Throwable t) {
                        Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Giao Bài Tập Mới");

        // Inflate layout dialog_add_task.xml vừa tạo ở trên
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_task, null);
        final EditText edtTitle = view.findViewById(R.id.edtTaskTitle);
        final EditText edtDeadline = view.findViewById(R.id.edtTaskDeadline);
        final EditText edtDesc = view.findViewById(R.id.edtTaskDesc);

        builder.setView(view);

        builder.setPositiveButton("Giao Bài", (dialog, which) -> {
            String title = edtTitle.getText().toString();
            String deadline = edtDeadline.getText().toString();
            String desc = edtDesc.getText().toString();

            if (!title.isEmpty()) {
                createTaskAPI(title, deadline, desc);
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void createTaskAPI(String title, String deadline, String desc) {
        SupabaseService service = RetrofitClient.getRetrofitInstance().create(SupabaseService.class);

        Task task = new Task();
        task.setTitle(title);
        task.setTime(deadline);
        task.setDescription(desc);
        task.setcourseId(courseId); // Chú ý: Entity dùng setcourseId (chữ c thường)
        task.setOwnerId(userId);
        task.setPriority(1);

        // SỬA: Thêm "Bearer " trước token
        service.createTask(BuildConfig.SUPABASE_KEY, "Bearer " + token, task).enqueue(new Callback<List<Task>>() {
            @Override
            public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Đã giao bài tập!", Toast.LENGTH_SHORT).show();
                    loadTasks(); // Load lại để thấy bài mới
                } else {
                    Toast.makeText(getContext(), "Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Task>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
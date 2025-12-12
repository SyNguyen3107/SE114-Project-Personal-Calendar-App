package com.synguyen.se114project.ui.teacher;

import android.content.Context;
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
import com.google.gson.JsonObject;
import com.synguyen.se114project.R;
import com.synguyen.se114project.data.remote.RetrofitClient;
import com.synguyen.se114project.data.remote.SupabaseService;
import com.synguyen.se114project.ui.adapter.TeacherTaskAdapter;

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

        // 1. Nhận CourseID từ Activity cha
        if (getArguments() != null) {
            courseId = getArguments().getString("COURSE_ID");
        }

        // 2. Lấy Token
        SharedPreferences prefs = getActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        token = prefs.getString("ACCESS_TOKEN", null);
        userId = prefs.getString("USER_ID", null);

        // 3. Setup UI
        rcvTasks = view.findViewById(R.id.rcvTasks);
        fabAdd = view.findViewById(R.id.fabAddTask);

        rcvTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TeacherTaskAdapter(new ArrayList<>());
        rcvTasks.setAdapter(adapter);

        // 4. Load dữ liệu & Sự kiện
        loadTasks();
        fabAdd.setOnClickListener(v -> showAddTaskDialog());

        return view;
    }

    private void loadTasks() {
        if (courseId == null || token == null) return;

        SupabaseService service = RetrofitClient.getClient().create(SupabaseService.class);
        // Lọc task theo course_id = eq.ID_CUA_MON_HOC
        service.getTasksByCourse(RetrofitClient.SUPABASE_KEY, token, "eq." + courseId)
                .enqueue(new Callback<List<JsonObject>>() {
                    @Override
                    public void onResponse(Call<List<JsonObject>> call, Response<List<JsonObject>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            adapter.updateData(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<JsonObject>> call, Throwable t) {
                        Toast.makeText(getContext(), "Lỗi tải bài tập", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Giao Bài Tập Mới");

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
        SupabaseService service = RetrofitClient.getClient().create(SupabaseService.class);

        JsonObject json = new JsonObject();
        json.addProperty("title", title);
        json.addProperty("time", deadline); // Lưu deadline vào cột 'time' (string) cho đơn giản
        json.addProperty("subtitle", desc);
        json.addProperty("course_id", courseId); // QUAN TRỌNG: Gắn task này vào môn học hiện tại
        json.addProperty("owner_id", userId);

        // Cần truyền mảng JSON hoặc 1 object, tuỳ API setup, thường Retrofit body nhận object OK
        service.createTask(RetrofitClient.SUPABASE_KEY, token, json).enqueue(new Callback<List<JsonObject>>() {
            @Override
            public void onResponse(Call<List<JsonObject>> call, Response<List<JsonObject>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Đã giao bài tập!", Toast.LENGTH_SHORT).show();
                    loadTasks(); // Load lại danh sách
                } else {
                    Toast.makeText(getContext(), "Lỗi: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<JsonObject>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
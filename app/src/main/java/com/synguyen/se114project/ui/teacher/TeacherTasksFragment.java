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
import com.synguyen.se114project.BuildConfig;
import com.synguyen.se114project.R;
import com.synguyen.se114project.data.entity.Task;
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
        SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("ACCESS_TOKEN", null);

        if (token == null || courseId == null) return;

        SupabaseService service = RetrofitClient.getRetrofitInstance().create(SupabaseService.class);

        // SỬA LẠI CÁCH GỌI HÀM:
        // 1. Dùng getTasksByCourse vừa tạo ở Bước 1
        // 2. Kiểu dữ liệu trong Call là List<Task> (không phải JsonObject)
        service.getTasksByCourse(BuildConfig.SUPABASE_KEY, token, "eq." + courseId)
                .enqueue(new Callback<List<Task>>() { // <--- Quan trọng: List<Task>
                    @Override
                    public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Task> tasks = response.body();

                            // Lúc này 'tasks' đã là List<Task>, không cần ép kiểu từ JsonObject nữa
                            // adapter.setData(tasks); // Giả sử adapter của bạn là TeacherTaskAdapter

                            // Log kiểm tra
                            android.util.Log.d("DEBUG_TASK", "Đã lấy được " + tasks.size() + " bài tập.");
                        } else {
                            Toast.makeText(getContext(), "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Task>> call, Throwable t) {
                        android.util.Log.e("DEBUG_TASK", "Lỗi: " + t.getMessage());
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
        SupabaseService service = RetrofitClient.getRetrofitInstance().create(SupabaseService.class);

        // 1. TẠO ĐỐI TƯỢNG TASK (Thay vì JsonObject)
        // Class Task đã có sẵn constructor tạo ID và các field mặc định
        Task task = new Task();
        task.setTitle(title);
        task.setTime(deadline); // Lưu deadline (dạng chuỗi giờ)
        task.setSubTitle(desc);
        task.setcourseId(courseId); // Lưu ý: Kiểm tra lại tên hàm setter trong Task.java (setCourseId hay setcourseId)
        task.setOwnerId(userId);

        // Gán các giá trị mặc định khác nếu cần
        task.setPriority(1);
        task.setSynced(true); // Vì đang đẩy thẳng lên server nên set là true

        // 2. GỌI API
        // - Dùng BuildConfig.SUPABASE_KEY
        // - Truyền biến 'task' vào body
        // - Callback nhận về List<Task>
        service.createTask(BuildConfig.SUPABASE_KEY, token, task).enqueue(new Callback<List<Task>>() {
            @Override
            public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Đã giao bài tập!", Toast.LENGTH_SHORT).show();
                    // loadTasks(); // Load lại danh sách nếu cần
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
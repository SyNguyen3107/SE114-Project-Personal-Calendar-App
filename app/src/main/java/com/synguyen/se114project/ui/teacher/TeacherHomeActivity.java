package com.synguyen.se114project.ui.teacher;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.JsonObject;
import com.synguyen.se114project.R;
import com.synguyen.se114project.data.remote.RetrofitClient;
import com.synguyen.se114project.data.remote.SupabaseService;
import com.synguyen.se114project.ui.adapter.TeacherCourseAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherHomeActivity extends AppCompatActivity {

    private RecyclerView rcvCourses;
    private FloatingActionButton fabAdd;
    private TeacherCourseAdapter adapter;
    private String token;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_home); // Đảm bảo bạn đã tạo layout này

        // 1. Lấy Token & ID từ bộ nhớ
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        token = prefs.getString("ACCESS_TOKEN", null);
        userId = prefs.getString("USER_ID", null);

        // 2. Ánh xạ UI
        rcvCourses = findViewById(R.id.rcvCourses);
        fabAdd = findViewById(R.id.fabAddCourse);

        // 3. Setup RecyclerView
        rcvCourses.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TeacherCourseAdapter(new ArrayList<>(), course -> {
            String id = course.get("id").getAsString();
            String name = course.get("name").getAsString();

            Intent intent = new Intent(TeacherHomeActivity.this, TeacherCourseDetailActivity.class);
            intent.putExtra("COURSE_ID", id);
            intent.putExtra("COURSE_NAME", name);
            startActivity(intent);
        });
        rcvCourses.setAdapter(adapter);

        // 4. Load dữ liệu
        loadCourses();

        // 5. Sự kiện thêm lớp mới
        fabAdd.setOnClickListener(v -> showAddCourseDialog());
    }

    private void loadCourses() {
        if (token == null) return;

        SupabaseService service = RetrofitClient.getClient().create(SupabaseService.class);
        // Lọc: eq.USER_ID
        service.getCourses(RetrofitClient.SUPABASE_KEY, token, "eq." + userId).enqueue(new Callback<List<JsonObject>>() {
            @Override
            public void onResponse(Call<List<JsonObject>> call, Response<List<JsonObject>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.updateData(response.body());
                } else {
                    Toast.makeText(TeacherHomeActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<JsonObject>> call, Throwable t) {
                Toast.makeText(TeacherHomeActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddCourseDialog() {
        // Tạo dialog nhập nhanh
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tạo Môn Học Mới");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_course, null);
        final EditText edtName = view.findViewById(R.id.edtCourseName);
        final EditText edtDesc = view.findViewById(R.id.edtCourseDesc);

        builder.setView(view);

        builder.setPositiveButton("Tạo", (dialog, which) -> {
            String name = edtName.getText().toString();
            String desc = edtDesc.getText().toString();
            if (!name.isEmpty()) {
                createCourseAPI(name, desc);
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void createCourseAPI(String name, String description) {
        SupabaseService service = RetrofitClient.getClient().create(SupabaseService.class);

        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("description", description);
        json.addProperty("owner_id", userId); // Rất quan trọng: Gán người tạo
        json.addProperty("teacher_name", "Giảng viên");

        service.createCourse(RetrofitClient.SUPABASE_KEY, token, json).enqueue(new Callback<List<JsonObject>>() {
            @Override
            public void onResponse(Call<List<JsonObject>> call, Response<List<JsonObject>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(TeacherHomeActivity.this, "Tạo thành công!", Toast.LENGTH_SHORT).show();
                    loadCourses(); // Tải lại danh sách
                } else {
                    Toast.makeText(TeacherHomeActivity.this, "Lỗi tạo lớp", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<JsonObject>> call, Throwable t) {
                Toast.makeText(TeacherHomeActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
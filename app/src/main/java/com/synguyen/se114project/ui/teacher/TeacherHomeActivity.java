package com.synguyen.se114project.ui.teacher;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.synguyen.se114project.data.entity.Course; // Đảm bảo import đúng entity này
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

    // SỬA 1: Đổi List<JsonObject> thành List<Course>
    private List<Course> courseList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_home);

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        token = prefs.getString("ACCESS_TOKEN", null);
        userId = prefs.getString("USER_ID", null);

        rcvCourses = findViewById(R.id.rcvCourses);
        fabAdd = findViewById(R.id.fabAddCourse);

        rcvCourses.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TeacherCourseAdapter(courseList, course -> {
            // SỬA 3: Dùng Getter của Class Course thay vì .get("id")
            String id = String.valueOf(course.getId()); // Giả sử ID là int/long
            String name = course.getName();

            Intent intent = new Intent(TeacherHomeActivity.this, TeacherCourseDetailActivity.class);
            intent.putExtra("COURSE_ID", id);
            intent.putExtra("COURSE_NAME", name);
            startActivity(intent);
        });
        rcvCourses.setAdapter(adapter);

        loadCourses();

        fabAdd.setOnClickListener(v -> showAddCourseDialog());
    }

    private void loadCourses() {
        if (token == null) return;

        SupabaseService service = RetrofitClient.getRetrofitInstance().create(SupabaseService.class);

        // SỬA 4: Callback phải là List<Course> để khớp với Interface
        service.getCourses(RetrofitClient.SUPABASE_KEY, token, "eq." + userId).enqueue(new Callback<List<Course>>() {
            @Override
            public void onResponse(Call<List<Course>> call, Response<List<Course>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    courseList.clear();
                    courseList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(TeacherHomeActivity.this, "Không tải được danh sách", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Course>> call, Throwable t) {
                Toast.makeText(TeacherHomeActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddCourseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tạo Môn Học Mới");

        View view = getLayoutInflater().inflate(R.layout.dialog_add_course, null);
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
        SupabaseService service = RetrofitClient.getRetrofitInstance().create(SupabaseService.class);

        // Khi gửi đi (POST), ta vẫn có thể dùng JsonObject để đóng gói dữ liệu gửi lên
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("description", description);
        json.addProperty("owner_id", userId);
        json.addProperty("teacher_name", "Giảng viên");
        service.createCourse(RetrofitClient.SUPABASE_KEY, token, json).enqueue(new Callback<List<Course>>() {
            @Override
            public void onResponse(Call<List<Course>> call, Response<List<Course>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(TeacherHomeActivity.this, "Tạo thành công!", Toast.LENGTH_SHORT).show();
                    loadCourses();
                } else {
                    Toast.makeText(TeacherHomeActivity.this, "Lỗi tạo lớp: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Course>> call, Throwable t) {
                Toast.makeText(TeacherHomeActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
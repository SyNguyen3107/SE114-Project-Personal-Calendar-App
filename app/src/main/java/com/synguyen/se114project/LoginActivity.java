package com.synguyen.se114project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.SharedPreferences;

import com.google.gson.JsonObject;
import com.synguyen.se114project.data.remote.RetrofitClient;
import com.synguyen.se114project.data.remote.SupabaseService;
import com.synguyen.se114project.data.remote.response.AuthResponse;
import com.synguyen.se114project.ui.student.MainActivity;
import com.synguyen.se114project.ui.teacher.TeacherHomeActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    // Trong LoginActivity.java

    private void handleLogin(String email, String password) {
        // 1. Chuẩn bị Body JSON
        JsonObject body = new JsonObject();
        body.addProperty("email", email);
        body.addProperty("password", password);

        // 2. Gọi API
        SupabaseService service = RetrofitClient.getClient().create(SupabaseService.class);
        Call<AuthResponse> call = service.loginUser(RetrofitClient.SUPABASE_KEY, body);

        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Đăng nhập thành công!
                    String accessToken = response.body().accessToken;
                    String userId = response.body().user.id;
                    String bearerToken = "Bearer " + accessToken;

                    // TODO 1: Lưu accessToken và userId vào SharedPreferences ngay lập tức!
                    saveToPreferences(accessToken, userId);

                    // TODO 2: Gọi tiếp API lấy Profile để xem là Teacher hay Student
                    checkUserRole(userId, bearerToken);

                } else {
                    Toast.makeText(LoginActivity.this, "Đăng nhập thất bại!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm check role
    private void checkUserRole(String userId, String token) {
        SupabaseService service = RetrofitClient.getClient().create(SupabaseService.class);
        // Cú pháp Supabase query: eq.GIÁ_TRỊ
        String queryId = "eq." + userId;

        service.getUserProfile(RetrofitClient.SUPABASE_KEY, token, queryId).enqueue(new Callback<List<JsonObject>>() {
            @Override
            public void onResponse(Call<List<JsonObject>> call, Response<List<JsonObject>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    JsonObject profile = response.body().get(0);
                    String role = profile.get("role").getAsString(); // "student" hoặc "teacher"

                    // CHUYỂN MÀN HÌNH DỰA VÀO ROLE
                    navigateToHome(role);
                }
            }
            @Override
            public void onFailure(Call<List<JsonObject>> call, Throwable t) {}
        });
    }
    private void navigateToHome(String role) {
        if ("teacher".equals(role)) {
            System.out.println("-----> CHÀO MỪNG GIẢNG VIÊN <-----");
            Intent intent = new Intent(LoginActivity.this, TeacherHomeActivity.class);
            startActivity(intent);
            finish();
        } else {
            System.out.println("-----> CHÀO MỪNG SINH VIÊN <-----");

            // QUAN TRỌNG: Vì chúng ta ĐANG ở MainActivity (Home của sinh viên)
            // Nên không cần startActivity(MainActivity) nữa để tránh vòng lặp vô tận.
            // Chỉ cần thông báo hoặc cập nhật UI nếu cần.
            Toast.makeText(this, "Bạn đang ở giao diện Sinh Viên", Toast.LENGTH_SHORT).show();
        }
    }
    // TODO 3: Lưu accessToken và userId vào SharedPreferences
    private void saveToPreferences(String accessToken, String userId) {
        SharedPreferences prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE);
        prefs.edit()
                .putString("access_token", accessToken)
                .putString("user_id", userId)
                .apply();
    }

}
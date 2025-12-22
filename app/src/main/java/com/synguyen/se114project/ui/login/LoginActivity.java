package com.synguyen.se114project.ui.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.JsonObject;
import com.synguyen.se114project.BuildConfig;
import com.synguyen.se114project.R;
import com.synguyen.se114project.data.entity.Profile;
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

    private EditText edtEmail, edtPassword;
    private Button btnLogin;

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

        // 1. Ánh xạ View (Bạn cần đảm bảo ID trong XML khớp với dòng này)
        edtEmail = findViewById(R.id.edtEmail);     // Ví dụ ID
        edtPassword = findViewById(R.id.edtPassword); // Ví dụ ID
        btnLogin = findViewById(R.id.btnLogin);     // Ví dụ ID

        // 2. Xử lý sự kiện đăng nhập
        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> {
                String email = edtEmail.getText().toString().trim();
                String pass = edtPassword.getText().toString().trim();

                if (email.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
                } else {
                    handleLogin(email, pass);
                }
            });
        }
    }

    private void handleLogin(String email, String password) {
        // Chuẩn bị Body JSON
        JsonObject body = new JsonObject();
        body.addProperty("email", email);
        body.addProperty("password", password);

        SupabaseService service = RetrofitClient.getRetrofitInstance().create(SupabaseService.class);

        // Dùng BuildConfig.SUPABASE_KEY để bảo mật
        Call<AuthResponse> call = service.loginUser(BuildConfig.SUPABASE_KEY, body);

        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Lấy thông tin từ Response
                    String accessToken = response.body().getValidAccessToken(); // Hoặc .accessToken tùy model của bạn
                    String userId = response.body().user.id;

                    // 1. Lưu Token ngay lập tức
                    saveToPreferences(accessToken, userId);

                    // 2. Gọi tiếp API lấy Profile để check Role
                    checkRoleAndNavigate(userId, accessToken);

                } else {
                    Toast.makeText(LoginActivity.this, "Đăng nhập thất bại! Kiểm tra lại email/pass.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm check role mới (Dùng List<Profile>)
    private void checkRoleAndNavigate(String userId, String token) {
        SupabaseService service = RetrofitClient.getRetrofitInstance().create(SupabaseService.class);

        // Gọi API lấy profile (đã sửa trong SupabaseService để trả về List<Profile>)
        // Query: "eq." + userId để lọc đúng user này
        service.getProfile(BuildConfig.SUPABASE_KEY, "Bearer " + token, "eq." + userId)
                .enqueue(new Callback<List<Profile>>() {
                    @Override
                    public void onResponse(Call<List<Profile>> call, Response<List<Profile>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            // Lấy profile đầu tiên
                            Profile profile = response.body().get(0);
                            String role = profile.getRole(); // Lấy role từ Object Profile

                            // Lưu Role vào Prefs luôn để dùng về sau (ví dụ ẩn hiện UI)
                            SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                            prefs.edit().putString("USER_ROLE", role).apply();

                            // Điều hướng
                            navigateToHome(role);
                        } else {
                            // Trường hợp có User nhưng chưa có Profile -> Mặc định cho là Student hoặc báo lỗi
                            Log.e("Login", "Không tìm thấy Profile cho User này");
                            navigateToHome("student");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Profile>> call, Throwable t) {
                        Toast.makeText(LoginActivity.this, "Lỗi khi lấy thông tin Profile", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToHome(String role) {
        Intent intent;
        if ("teacher".equalsIgnoreCase(role)) {
            // Role là Teacher -> Vào màn hình giáo viên
            Toast.makeText(this, "Xin chào Giảng viên!", Toast.LENGTH_SHORT).show();
            intent = new Intent(LoginActivity.this, TeacherHomeActivity.class);
        } else {
            // Role là Student (hoặc khác) -> Vào màn hình sinh viên
            Toast.makeText(this, "Xin chào Sinh viên!", Toast.LENGTH_SHORT).show();
            intent = new Intent(LoginActivity.this, MainActivity.class);
        }
        startActivity(intent);
        finish(); // Đóng LoginActivity để user không back lại được
    }

    private void saveToPreferences(String accessToken, String userId) {
        // QUAN TRỌNG: Tên file Prefs phải là "AppPrefs" để khớp với SyncWorker và các Fragment khác
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        prefs.edit()
                .putString("ACCESS_TOKEN", accessToken) // Key phải viết hoa nếu các file khác dùng viết hoa
                .putString("USER_ID", userId)
                .apply();
    }
}
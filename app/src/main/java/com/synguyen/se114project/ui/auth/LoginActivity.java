package com.synguyen.se114project.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.synguyen.se114project.BuildConfig;
import com.synguyen.se114project.R;
import com.synguyen.se114project.data.entity.Profile;
import com.synguyen.se114project.data.remote.RetrofitClient;
import com.synguyen.se114project.data.remote.SupabaseService;
import com.synguyen.se114project.data.remote.response.AuthResponse;
import com.synguyen.se114project.data.repository.AuthRepository;
import com.synguyen.se114project.ui.student.MainActivity;
import com.synguyen.se114project.ui.teacher.main.TeacherMainActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText edtEmail, edtPassword; // Dùng TextInputEditText cho khớp với XML material
    private Button btnLogin;
    private TextView tvRegister;
    private ProgressBar progressBar;

    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 0. Kiểm tra Auto Login (Nếu đã đăng nhập thì vào luôn)
        if (checkAutoLogin()) {
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Khởi tạo Repository
        authRepository = new AuthRepository();

        // 2. Ánh xạ View (Khớp với ID trong activity_login.xml)
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBarLogin); // ID của ProgressBar
        tvRegister = findViewById(R.id.tvRegister);       // ID của nút Đăng ký

        // 3. Sự kiện Đăng nhập
        btnLogin.setOnClickListener(v -> {
            String email = edtEmail.getText() != null ? edtEmail.getText().toString().trim() : "";
            String pass = edtPassword.getText() != null ? edtPassword.getText().toString().trim() : "";

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            } else {
                handleLogin(email, pass);
            }
        });

        // 4. Sự kiện chuyển sang màn hình Đăng ký
        if (tvRegister != null) {
            tvRegister.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            });
        }
    }

    private void handleLogin(String email, String password) {
        setLoading(true);

        // Sử dụng AuthRepository để gọi API Login
        authRepository.login(email, password, new AuthRepository.ResultCallback<AuthResponse>() {
            @Override
            public void onSuccess(AuthResponse data) {
                // Lấy Token và ID từ AuthResponse (Đã có helper method getAccessToken)
                String accessToken = data.getAccessToken();
                String userId = data.getUserId();

                if (accessToken != null && userId != null) {
                    // Gọi tiếp API lấy Profile để check Role
                    checkRoleAndNavigate(userId, accessToken);
                } else {
                    setLoading(false);
                    Toast.makeText(LoginActivity.this, "Lỗi dữ liệu đăng nhập", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm lấy Role từ bảng 'profiles'
    private void checkRoleAndNavigate(String userId, String token) {
        SupabaseService service = RetrofitClient.getRetrofitInstance().create(SupabaseService.class);

        // Gọi API: select * from profiles where id = userId
        service.getProfile(BuildConfig.SUPABASE_KEY, "Bearer " + token, "eq." + userId)
                .enqueue(new Callback<List<Profile>>() {
                    @Override
                    public void onResponse(Call<List<Profile>> call, Response<List<Profile>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            // Lấy profile
                            Profile profile = response.body().get(0);
                            String role = profile.getRole(); // Lấy role (student/teacher)

                            // Lưu toàn bộ session vào SharedPreferences
                            saveToPreferences(token, userId, role);

                            // Điều hướng
                            navigateToHome(role);
                        } else {
                            // Trường hợp hiếm: Có User Auth nhưng chưa có Profile
                            // Mặc định cho là student để tránh crash
                            saveToPreferences(token, userId, "student");
                            navigateToHome("student");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Profile>> call, Throwable t) {
                        setLoading(false);
                        Toast.makeText(LoginActivity.this, "Không lấy được thông tin người dùng", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToHome(String role) {
        setLoading(false); // Ẩn loading trước khi chuyển
        Intent intent;
        if ("teacher".equalsIgnoreCase(role)) {
            Toast.makeText(this, "Xin chào Giảng viên!", Toast.LENGTH_SHORT).show();
            intent = new Intent(LoginActivity.this, TeacherMainActivity.class);
        } else {
            Toast.makeText(this, "Xin chào Sinh viên!", Toast.LENGTH_SHORT).show();
            intent = new Intent(LoginActivity.this, MainActivity.class);
        }

        // Cờ này giúp xóa LoginActivity khỏi stack, bấm Back sẽ thoát app chứ ko về login
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void saveToPreferences(String accessToken, String userId, String role) {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        prefs.edit()
                .putString("ACCESS_TOKEN", accessToken)
                .putString("USER_ID", userId)
                .putString("USER_ROLE", role) // Lưu thêm Role
                .apply();
    }

    // Check xem user đã đăng nhập trước đó chưa
    private boolean checkAutoLogin() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String token = prefs.getString("ACCESS_TOKEN", null);
        String role = prefs.getString("USER_ROLE", null);

        if (token != null && role != null) {
            navigateToHome(role);
            return true;
        }
        return false;
    }

    // Hàm quản lý UI Loading
    private void setLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }

        // Thêm kiểm tra null cho các View này
        if (btnLogin != null) {
            btnLogin.setEnabled(!isLoading);
        }
        if (edtEmail != null) {
            edtEmail.setEnabled(!isLoading);
        }
        if (edtPassword != null) {
            edtPassword.setEnabled(!isLoading);
        }
    }
}
package com.synguyen.se114project.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
import com.synguyen.se114project.ui.student.main.StudentMainActivity;
import com.synguyen.se114project.ui.teacher.main.TeacherMainActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText edtEmail, edtPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private ProgressBar progressBar;

    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        authRepository = new AuthRepository();

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBarLogin);
        tvRegister = findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(v -> {
            String email = edtEmail.getText() != null ? edtEmail.getText().toString().trim() : "";
            String pass = edtPassword.getText() != null ? edtPassword.getText().toString().trim() : "";

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            } else {
                handleLogin(email, pass);
            }
        });

        if (tvRegister != null) {
            tvRegister.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            });
        }
    }

    private void handleLogin(String email, String password) {
        setLoading(true);

        authRepository.login(email, password, new AuthRepository.ResultCallback<AuthResponse>() {
            @Override
            public void onSuccess(AuthResponse data) {
                String accessToken = data.getAccessToken();
                String userId = data.getUserId();
                String refreshToken = data.getRefreshToken();

                if (refreshToken != null) {
                    getSharedPreferences("AppPrefs", MODE_PRIVATE).edit().putString("REFRESH_TOKEN", refreshToken).apply();
                }

                if (accessToken != null && userId != null) {
                    checkRoleAndNavigate(userId, accessToken);
                } else {
                    setLoading(false);
                    Toast.makeText(LoginActivity.this, "Login data error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkRoleAndNavigate(String userId, String token) {
        SupabaseService service = RetrofitClient.getRetrofitInstance().create(SupabaseService.class);

        service.getProfile("Bearer " + token, "eq." + userId)
                .enqueue(new Callback<List<Profile>>() {
                    @Override
                    public void onResponse(Call<List<Profile>> call, Response<List<Profile>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            Profile profile = response.body().get(0);
                            String role = profile.getRole();
                            String name = profile.getFullName();

                            Log.d("DEBUG_LOGIN", "UserId: " + userId + " | Name from DB: " + name + " | Role: " + role);

                            saveToPreferences(token, userId, role, name);
                            navigateToHome(role, name);
                        } else {
                            Log.e("DEBUG_LOGIN", "Profile not found or empty response");
                            saveToPreferences(token, userId, "student", "");
                            navigateToHome("student", "");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Profile>> call, Throwable t) {
                        setLoading(false);
                        Log.e("DEBUG_LOGIN", "Network failure: " + t.getMessage());
                        Toast.makeText(LoginActivity.this, "Failed to fetch profile", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToHome(String role, String fullName) {
        setLoading(false);
        Intent intent;
        String welcomeMsg = "Welcome, " + (fullName != null && !fullName.isEmpty() ? fullName : "User") + "!";
        
        if ("teacher".equalsIgnoreCase(role)) {
            Toast.makeText(this, welcomeMsg + " (Teacher)", Toast.LENGTH_SHORT).show();
            intent = new Intent(LoginActivity.this, TeacherMainActivity.class);
        } else {
            Toast.makeText(this, welcomeMsg + " (Student)", Toast.LENGTH_SHORT).show();
            intent = new Intent(LoginActivity.this, StudentMainActivity.class);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void saveToPreferences(String accessToken, String userId, String role, String fullName) {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        prefs.edit()
                .putString("ACCESS_TOKEN", accessToken)
                .putString("USER_ID", userId)
                .putString("USER_ROLE", role)
                .putString("USER_NAME", fullName)
                .apply();
        Log.d("DEBUG_LOGIN", "Saved to Prefs - Name: " + fullName);
    }

    private boolean checkAutoLogin() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String token = prefs.getString("ACCESS_TOKEN", null);
        String role = prefs.getString("USER_ROLE", null);
        String name = prefs.getString("USER_NAME", "");

        if (token != null && role != null) {
            Log.d("DEBUG_LOGIN", "AutoLogin detected - Name: " + name);
            navigateToHome(role, name);
            return true;
        }
        return false;
    }

    private void setLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (btnLogin != null) btnLogin.setEnabled(!isLoading);
        if (edtEmail != null) edtEmail.setEnabled(!isLoading);
        if (edtPassword != null) edtPassword.setEnabled(!isLoading);
    }
}
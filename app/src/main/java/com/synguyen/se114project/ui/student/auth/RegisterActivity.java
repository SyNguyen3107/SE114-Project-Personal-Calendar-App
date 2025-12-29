package com.synguyen.se114project.ui.student.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.synguyen.se114project.R;
import com.synguyen.se114project.data.repository.AuthRepository;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText edtFullName, edtEmail, edtPassword;
    private Button btnRegister;// btnBack;
    private ProgressBar progressBar;
    private RadioGroup rgRole;

    private AuthRepository authRepository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnRegister = findViewById(R.id.btnRegister);
        //btnBack = findViewById(R.id.btnBackLogin);
        progressBar = findViewById(R.id.progressBarRegister);
        rgRole = findViewById(R.id.rgRole);

        authRepository = new AuthRepository();

        //btnBack.setOnClickListener(v -> finish());

        btnRegister.setOnClickListener(v -> {
            String fullName = safe(edtFullName);
            String email = safe(edtEmail);
            String password = safe(edtPassword);

            if (!validate(fullName, email, password)) return;

            setLoading(true);

            // Determine selected role
            String role = "student";
            if (rgRole != null) {
                int sel = rgRole.getCheckedRadioButtonId();
                if (sel == R.id.rbTeacher) role = "teacher";
            }

            authRepository.signUpAndCreateProfile(email, password, fullName, role,
                    new AuthRepository.ResultCallback<AuthRepository.SignUpResult>() {
                        @Override
                        public void onSuccess(AuthRepository.SignUpResult data) {
                            runOnUiThread(() -> {
                                setLoading(false);
                                Toast.makeText(RegisterActivity.this, "Đăng kí thành công! Logging in...", Toast.LENGTH_SHORT).show();
                            });

                            // After signup we immediately login to persist tokens (refresh_token may be present)
                            authRepository.login(email, password, new AuthRepository.ResultCallback<com.synguyen.se114project.data.remote.response.AuthResponse>() {
                                @Override
                                public void onSuccess(com.synguyen.se114project.data.remote.response.AuthResponse resp) {
                                    // Save tokens and user id
                                    String access = resp.getAccessToken();
                                    String userId = resp.getUserId();
                                    String refresh = resp.getRefreshToken();
                                    android.content.SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                                    prefs.edit().putString("ACCESS_TOKEN", access).putString("USER_ID", userId).apply();
                                    if (refresh != null) prefs.edit().putString("REFRESH_TOKEN", refresh).apply();
                                    runOnUiThread(() -> {
                                        Toast.makeText(RegisterActivity.this, "Đăng nhập tự động thành công", Toast.LENGTH_SHORT).show();
                                        finish();
                                    });
                                }

                                @Override
                                public void onError(String message) {
                                    runOnUiThread(() -> {
                                        Toast.makeText(RegisterActivity.this, "Đăng kí xong nhưng tự login thất bại", Toast.LENGTH_LONG).show();
                                        finish();
                                    });
                                }
                            });
                        }

                        @Override
                        public void onError(String message) {
                            runOnUiThread(() -> {
                                setLoading(false);
                                Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
                            });
                        }
                    });
        });
    }

    private String safe(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private boolean validate(String fullName, String email, String password) {
        if (TextUtils.isEmpty(fullName)) { edtFullName.setError("Full name required"); return false; }
        if (TextUtils.isEmpty(email)) { edtEmail.setError("Email required"); return false; }
        if (TextUtils.isEmpty(password) || password.length() < 6) { edtPassword.setError("Password >= 6"); return false; }
        return true;
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!loading);
        //btnBack.setEnabled(!loading);
    }
}

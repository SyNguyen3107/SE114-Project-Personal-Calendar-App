package com.synguyen.se114project.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.synguyen.se114project.R;
import com.synguyen.se114project.data.repository.AuthRepository;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText edtFullName, edtEmail, edtPassword, edtConfirmPassword;
    private Button btnRegister;
    private TextView tvLoginLink;
    private ProgressBar progressBar;
    private RadioGroup rgRole;

    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register); // Đảm bảo tên file xml đúng

        // 1. Ánh xạ Views (IDs phải khớp với file XML của bạn)
        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginLink = findViewById(R.id.tvLoginLink); // Nút "Đã có tài khoản? Đăng nhập"
        progressBar = findViewById(R.id.progressBarRegister);
        rgRole = findViewById(R.id.rgRole);

        // 2. Khởi tạo Repository
        authRepository = new AuthRepository();

        // 3. Xử lý sự kiện
        btnRegister.setOnClickListener(v -> handleRegister());

        if (tvLoginLink != null) {
            tvLoginLink.setOnClickListener(v -> finish()); // Quay lại màn hình Login
        }
    }

    private void handleRegister() {
        // Lấy dữ liệu từ ô nhập
        String fullName = edtFullName.getText() != null ? edtFullName.getText().toString().trim() : "";
        String email = edtEmail.getText() != null ? edtEmail.getText().toString().trim() : "";
        String password = edtPassword.getText() != null ? edtPassword.getText().toString().trim() : "";

        // (Tùy chọn) Kiểm tra Confirm Password nếu có ô nhập này
        // String confirmPass = edtConfirmPassword.getText().toString().trim();

        // --- VALIDATION (Kiểm tra dữ liệu đầu vào) ---
        if (TextUtils.isEmpty(fullName)) {
            edtFullName.setError("Vui lòng nhập họ tên");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Vui lòng nhập email");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            edtPassword.setError("Vui lòng nhập mật khẩu");
            return;
        }
        if (password.length() < 6) {
            edtPassword.setError("Mật khẩu phải từ 6 ký tự trở lên");
            return;
        }
        // if (!password.equals(confirmPass)) { ... }

        // --- GỌI API ---
        setLoading(true);

        // Determine role selection (default: student)
        String role = "student";
        if (rgRole != null) {
            int sel = rgRole.getCheckedRadioButtonId();
            if (sel == R.id.rbTeacher) role = "teacher";
        }

        // Gọi hàm signUpAndCreateProfile từ AuthRepository
        authRepository.signUpAndCreateProfile(email, password, fullName, role, new AuthRepository.ResultCallback<AuthRepository.SignUpResult>() {
            @Override
            public void onSuccess(AuthRepository.SignUpResult data) {
                setLoading(false);
                Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();

                // Kết thúc màn hình đăng ký, quay về Login để người dùng đăng nhập
                finish();
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                Toast.makeText(RegisterActivity.this, "Lỗi: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }

    // Hàm hiển thị/ẩn trạng thái Loading
    private void setLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        btnRegister.setEnabled(!isLoading);
        edtFullName.setEnabled(!isLoading);
        edtEmail.setEnabled(!isLoading);
        edtPassword.setEnabled(!isLoading);
    }
}
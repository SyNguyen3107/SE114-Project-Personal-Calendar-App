package com.synguyen.se114project.ui.student.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
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
        progressBar = findViewById(R.id.progressBar);

        authRepository = new AuthRepository();

        //btnBack.setOnClickListener(v -> finish());

        btnRegister.setOnClickListener(v -> {
            String fullName = safe(edtFullName);
            String email = safe(edtEmail);
            String password = safe(edtPassword);

            if (!validate(fullName, email, password)) return;

            setLoading(true);

            authRepository.signUpAndCreateProfile(email, password, fullName,
                    new AuthRepository.ResultCallback<AuthRepository.SignUpResult>() {
                        @Override
                        public void onSuccess(AuthRepository.SignUpResult data) {
                            runOnUiThread(() -> {
                                setLoading(false);
                                Toast.makeText(RegisterActivity.this, "Đăng kí thành công!", Toast.LENGTH_SHORT).show();
                                finish();
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

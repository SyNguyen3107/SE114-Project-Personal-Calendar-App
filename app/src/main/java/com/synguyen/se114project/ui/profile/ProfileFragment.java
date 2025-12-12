package com.synguyen.se114project.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.synguyen.se114project.R;

public class ProfileFragment extends Fragment {

    private TextInputEditText etName, etEmail;
    private ImageView imgAvatar, btnChangeAvatar;
    private Button btnSaveProfile;

    // Biến để lưu URI ảnh đã chọn
    private Uri selectedImageUri = null;

    // Bộ khởi chạy để mở thư viện ảnh (Thay thế cho startActivityForResult cũ)
    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    // 1. Lưu URI ảnh vừa chọn vào biến tạm
                    selectedImageUri = uri;

                    // 2. Hiển thị lên ImageView
                    imgAvatar.setImageURI(uri);

                    // 3. (Quan trọng) Cấp quyền bền vững để app có thể đọc ảnh này sau khi khởi động lại
                    try {
                        requireContext().getContentResolver().takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ View
        etName = view.findViewById(R.id.etName);
        etEmail = view.findViewById(R.id.etEmail);
        imgAvatar = view.findViewById(R.id.imgAvatar);
        btnChangeAvatar = view.findViewById(R.id.btnChangeAvatar);
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);

        // 2. Load dữ liệu đã lưu (nếu có)
        loadUserProfile();

        // 3. Sự kiện chọn ảnh
        btnChangeAvatar.setOnClickListener(v -> {
            // Mở thư viện ảnh, chỉ lọc file ảnh ("image/*")
            pickImageLauncher.launch("image/*");
        });

        // 4. Sự kiện Lưu
        btnSaveProfile.setOnClickListener(v -> saveUserProfile());
    }

    private void saveUserProfile() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Name is required");
            return;
        }

        // Khởi tạo SharedPreferences
        SharedPreferences sharedPref = requireActivity().getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        // Lưu thông tin
        editor.putString("KEY_NAME", name);
        editor.putString("KEY_EMAIL", email);

        // Lưu đường dẫn ảnh (chuyển URI thành String)
        if (selectedImageUri != null) {
            editor.putString("KEY_AVATAR_URI", selectedImageUri.toString());
        }

        editor.apply(); // Lưu bất đồng bộ

        Toast.makeText(getContext(), "Profile Saved!", Toast.LENGTH_SHORT).show();
    }

    private void loadUserProfile() {
        SharedPreferences sharedPref = requireActivity().getSharedPreferences("UserProfile", Context.MODE_PRIVATE);

        String savedName = sharedPref.getString("KEY_NAME", "");
        String savedEmail = sharedPref.getString("KEY_EMAIL", "");
        String savedUriString = sharedPref.getString("KEY_AVATAR_URI", null);

        etName.setText(savedName);
        etEmail.setText(savedEmail);

        if (savedUriString != null) {
            selectedImageUri = Uri.parse(savedUriString);
            try {
                imgAvatar.setImageURI(selectedImageUri);
            } catch (Exception e) {
                // Trường hợp ảnh bị xóa khỏi máy hoặc mất quyền truy cập, hiển thị ảnh mặc định
                imgAvatar.setImageResource(R.drawable.ic_launcher_background);
            }
        }
    }
}
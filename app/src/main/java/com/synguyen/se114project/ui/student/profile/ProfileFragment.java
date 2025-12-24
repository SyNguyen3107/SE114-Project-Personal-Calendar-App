package com.synguyen.se114project.ui.student.profile;

import android.app.AlertDialog;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.synguyen.se114project.R;
import com.synguyen.se114project.data.entity.Profile; // Import Entity Profile
import com.synguyen.se114project.data.repository.ProfileRepository;
import com.synguyen.se114project.ui.auth.LoginActivity;

public class ProfileFragment extends Fragment {

    private TextInputEditText etName, etEmail;
    private ImageView imgAvatar, btnChangeAvatar;
    private TextView btnLogout;
    private Button btnSaveProfile;

    private Uri selectedImageUri = null;

    private ProfileRepository profileRepository;

    // ====== Auth Prefs (Đã sửa cho khớp với LoginActivity & Worker) ======
    private static final String APP_PREFS = "AppPrefs"; // Tên file thống nhất
    private static final String KEY_ACCESS_TOKEN = "ACCESS_TOKEN"; // Key viết hoa
    private static final String KEY_USER_ID = "USER_ID"; // Key viết hoa

    // ====== Local Prefs (Lưu avatar tạm dưới máy) ======
    private static final String LOCAL_PREFS = "UserProfile";
    private static final String KEY_AVATAR_URI = "KEY_AVATAR_URI";

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    imgAvatar.setImageURI(uri);

                    try {
                        requireContext().getContentResolver().takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
                    } catch (SecurityException ignored) {}
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

        etName = view.findViewById(R.id.etName);
        etEmail = view.findViewById(R.id.etEmail);
        imgAvatar = view.findViewById(R.id.imgAvatar);
        btnChangeAvatar = view.findViewById(R.id.btnChangeAvatar);
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        btnLogout = view.findViewById(R.id.btnLogout);

        profileRepository = new ProfileRepository();

        // 1) Avatar local (demo đẹp)
        loadAvatarLocal();

        // 2) Load dữ liệu từ Supabase public.profiles
        loadProfileFromSupabase();

        // 3) Chọn ảnh
        btnChangeAvatar.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        // 4) Save: update tên lên Supabase + lưu avatar local
        btnSaveProfile.setOnClickListener(v -> {
            saveAvatarLocal();
            updateNameToSupabase();
        });

        // 5) Logout
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> showLogoutConfirmationDialog());
        }
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Log out")
                .setMessage("Do you really want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> handleLogout())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void handleLogout() {
        // Xóa Token và thông tin User
        SharedPreferences authPref = requireActivity().getSharedPreferences(APP_PREFS, Context.MODE_PRIVATE);
        authPref.edit().clear().apply();

        // Điều hướng về LoginActivity
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
        
        Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    // ===================== SUPABASE =====================
    private void loadProfileFromSupabase() {
        // Dùng tên file Prefs chuẩn "AppPrefs"
        SharedPreferences authPref = requireActivity().getSharedPreferences(APP_PREFS, Context.MODE_PRIVATE);
        String token = authPref.getString(KEY_ACCESS_TOKEN, "");
        String userId = authPref.getString(KEY_USER_ID, "");

        if (token.isEmpty() || userId.isEmpty()) {
            Toast.makeText(getContext(), "Bạn chưa đăng nhập (thiếu token/userId)", Toast.LENGTH_SHORT).show();
            return;
        }

        // SỬA ĐỔI: Callback nhận vào Profile (không phải JsonObject)
        profileRepository.getProfile(token, userId, new ProfileRepository.ResultCallback<Profile>() {
            @Override
            public void onSuccess(Profile profile) {
                if (isAdded()) { // Kiểm tra Fragment còn tồn tại không
                    requireActivity().runOnUiThread(() -> {
                        // Dùng Getter của Object Profile
                        String fullName = profile.getFullName();
                        String email = profile.getEmail();

                        etName.setText(fullName != null ? fullName : "");
                        etEmail.setText(email != null ? email : "");

                        // Avatar: nếu chưa chọn ảnh local thì dùng ảnh tĩnh
                        if (selectedImageUri == null) {
                            imgAvatar.setImageResource(R.drawable.ic_launcher_background);
                        }
                    });
                }
            }

            @Override
            public void onError(String message) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show()
                    );
                }
            }
        });
    }

    private void updateNameToSupabase() {
        String newName = etName.getText() == null ? "" : etName.getText().toString().trim();
        if (newName.isEmpty()) {
            etName.setError("Name is required");
            return;
        }

        SharedPreferences authPref = requireActivity().getSharedPreferences(APP_PREFS, Context.MODE_PRIVATE);
        String token = authPref.getString(KEY_ACCESS_TOKEN, "");
        String userId = authPref.getString(KEY_USER_ID, "");

        if (token.isEmpty() || userId.isEmpty()) {
            Toast.makeText(getContext(), "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        profileRepository.updateFullName(token, userId, newName, new ProfileRepository.ResultCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                    );
                }
            }

            @Override
            public void onError(String message) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show()
                    );
                }
            }
        });
    }

    // ===================== LOCAL AVATAR =====================
    private void loadAvatarLocal() {
        SharedPreferences sp = requireActivity().getSharedPreferences(LOCAL_PREFS, Context.MODE_PRIVATE);
        String savedUriString = sp.getString(KEY_AVATAR_URI, null);

        if (savedUriString != null) {
            selectedImageUri = Uri.parse(savedUriString);
            try {
                imgAvatar.setImageURI(selectedImageUri);
            } catch (Exception e) {
                imgAvatar.setImageResource(R.drawable.ic_launcher_background);
            }
        } else {
            imgAvatar.setImageResource(R.drawable.ic_launcher_background);
        }
    }

    private void saveAvatarLocal() {
        SharedPreferences sp = requireActivity().getSharedPreferences(LOCAL_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        if (selectedImageUri != null) {
            editor.putString(KEY_AVATAR_URI, selectedImageUri.toString());
        }
        editor.apply();
    }
}

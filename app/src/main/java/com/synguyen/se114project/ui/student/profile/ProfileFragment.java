package com.synguyen.se114project.ui.student.profile;

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
import com.synguyen.se114project.data.repository.ProfileRepository;

public class ProfileFragment extends Fragment {

    private TextInputEditText etName, etEmail;
    private ImageView imgAvatar, btnChangeAvatar;
    private Button btnSaveProfile;

    private Uri selectedImageUri = null;

    private ProfileRepository profileRepository;

    // ====== Auth Prefs  ======
    // Trong LoginActivity.java
    private static final String AUTH_PREFS = "auth_prefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_USER_ID = "user_id";

    // ====== Local Prefs  ======
    // Trong ProfileFragment.java
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
    }

    // ===================== SUPABASE =====================
    private void loadProfileFromSupabase() {
        SharedPreferences authPref = requireActivity().getSharedPreferences(AUTH_PREFS, Context.MODE_PRIVATE);
        String token = authPref.getString(KEY_ACCESS_TOKEN, "");
        String userId = authPref.getString(KEY_USER_ID, "");

        if (token.isEmpty() || userId.isEmpty()) {
            Toast.makeText(getContext(), "Bạn chưa đăng nhập (thiếu token/userId)", Toast.LENGTH_SHORT).show();
            return;
        }

        profileRepository.getProfile(token, userId, new ProfileRepository.ResultCallback<com.google.gson.JsonObject>() {
            @Override
            public void onSuccess(com.google.gson.JsonObject data) {
                requireActivity().runOnUiThread(() -> {
                    String fullName = data.has("full_name") && !data.get("full_name").isJsonNull()
                            ? data.get("full_name").getAsString() : "";
                    String email = data.has("email") && !data.get("email").isJsonNull()
                            ? data.get("email").getAsString() : "";

                    etName.setText(fullName);
                    etEmail.setText(email);

                    // Avatar: nếu chưa chọn ảnh local thì dùng ảnh tĩnh
                    if (selectedImageUri == null) {
                        imgAvatar.setImageResource(R.drawable.ic_launcher_background);
                    }
                });
            }

            @Override
            public void onError(String message) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show()
                );
            }
        });
    }

    private void updateNameToSupabase() {
        String newName = etName.getText() == null ? "" : etName.getText().toString().trim();
        if (newName.isEmpty()) {
            etName.setError("Name is required");
            return;
        }

        SharedPreferences authPref = requireActivity().getSharedPreferences(AUTH_PREFS, Context.MODE_PRIVATE);
        String token = authPref.getString(KEY_ACCESS_TOKEN, "");
        String userId = authPref.getString(KEY_USER_ID, "");

        if (token.isEmpty() || userId.isEmpty()) {
            Toast.makeText(getContext(), "Bạn chưa đăng nhập (thiếu token/userId)", Toast.LENGTH_SHORT).show();
            return;
        }

        profileRepository.updateFullName(token, userId, newName, new ProfileRepository.ResultCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Profile updated!", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onError(String message) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show()
                );
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

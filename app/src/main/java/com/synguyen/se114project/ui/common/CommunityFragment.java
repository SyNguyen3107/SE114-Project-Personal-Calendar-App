package com.synguyen.se114project.ui.common;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.synguyen.se114project.R;

public class CommunityFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_community, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ nút Avatar (Lưu ý: ID này phải khớp với file xml bạn sửa ở Bước 1)
        // Nếu trong xml bạn đặt là @id/btnProfile thì sửa dòng dưới thành R.id.btnProfile
        View btnAvatar = view.findViewById(R.id.imgUserAvatar);

        if (btnAvatar != null) {
            btnAvatar.setOnClickListener(v -> {
                try {
                    // Dùng dòng này thay cho dòng Navigation.findNavController(...) cũ
                    NavHostFragment.findNavController(CommunityFragment.this)
                            .navigate(R.id.profileFragment);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
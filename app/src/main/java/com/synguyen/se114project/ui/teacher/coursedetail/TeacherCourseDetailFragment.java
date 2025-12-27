package com.synguyen.se114project.ui.teacher.coursedetail;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.synguyen.se114project.BuildConfig;
import com.synguyen.se114project.R;
import com.synguyen.se114project.data.entity.Enrollment;
import com.synguyen.se114project.data.entity.Profile;
import com.synguyen.se114project.data.remote.RetrofitClient;
import com.synguyen.se114project.data.remote.SupabaseService;
import com.synguyen.se114project.ui.teacher.materials.TeacherMaterialsFragment;
import com.synguyen.se114project.ui.teacher.students.TeacherStudentsFragment;
import com.synguyen.se114project.ui.teacher.TeacherTasksFragment;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherCourseDetailFragment extends Fragment {

    private String courseId;
    private String courseName;
    private FloatingActionButton fabAddStudent;
    private String token;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Sử dụng layout cũ, đảm bảo ID bên trong không đổi
        return inflater.inflate(R.layout.fragment_teacher_course_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Lấy Token từ SharedPreferences
        SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        token = prefs.getString("ACCESS_TOKEN", null);

        // 1. Nhận dữ liệu từ Arguments (thay vì Intent)
        if (getArguments() != null) {
            courseId = getArguments().getString("COURSE_ID");
            courseName = getArguments().getString("COURSE_NAME");
        }

        // 2. Setup Toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbarDetail);
        toolbar.setTitle(courseName != null ? courseName : "Chi tiết môn học");

        // Navigation: Quay lại màn hình trước đó dùng Navigation Component
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        // 3. Setup UI Components
        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        ViewPager2 viewPager = view.findViewById(R.id.viewPager);
        fabAddStudent = view.findViewById(R.id.fabAddStudent);

        // Adapter cho Fragment phải truyền 'this' (Fragment) thay vì 'this' (Activity)
        ViewPagerAdapter adapter = new ViewPagerAdapter(this, courseId);
        viewPager.setAdapter(adapter);

        // Gắn tên Tab
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Bài tập"); break;
                case 1: tab.setText("Sinh viên"); break;
                case 2: tab.setText("Tài liệu"); break;
            }
        }).attach();

        // 4. Logic Ẩn/Hiện nút Thêm Sinh Viên
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 1) {
                    fabAddStudent.show();
                } else {
                    fabAddStudent.hide();
                }
            }
        });

        // 5. Sự kiện bấm nút Thêm
        fabAddStudent.setOnClickListener(v -> showInputCodeDialog());
    }

    // --- LOGIC THÊM SINH VIÊN ---

    private void showInputCodeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Thêm sinh viên vào lớp");
        builder.setMessage("Nhập Mã Sinh Viên (User Code):");

        final EditText input = new EditText(requireContext());
        input.setHint("Ví dụ: SV12345678");
        builder.setView(input);

        builder.setPositiveButton("Tìm kiếm", (dialog, which) -> {
            String code = input.getText().toString().trim();
            if (!code.isEmpty()) {
                findStudentByCode(code);
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void findStudentByCode(String code) {
        SupabaseService service = RetrofitClient.getRetrofitInstance().create(SupabaseService.class);

        service.getProfileByCode(BuildConfig.SUPABASE_KEY, "Bearer " + token, "eq." + code)
                .enqueue(new Callback<List<Profile>>() {
                    @Override
                    public void onResponse(Call<List<Profile>> call, Response<List<Profile>> response) {
                        if (isAdded() && response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            Profile student = response.body().get(0);
                            showConfirmDialog(student);
                        } else {
                            if (isAdded()) Toast.makeText(requireContext(), "Không tìm thấy sinh viên: " + code, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Profile>> call, Throwable t) {
                        if (isAdded()) Toast.makeText(requireContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showConfirmDialog(Profile student) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận thêm")
                .setMessage("Bạn muốn thêm sinh viên này?\n\nTên: " + student.getFullName() + "\nEmail: " + student.getEmail())
                .setPositiveButton("Thêm ngay", (dialog, which) -> {
                    addStudentToCourse(student.getId());
                })
                .setNegativeButton("Quay lại", null)
                .show();
    }

    private void addStudentToCourse(String studentId) {
        SupabaseService service = RetrofitClient.getRetrofitInstance().create(SupabaseService.class);
        Enrollment enrollment = new Enrollment(courseId, studentId);

        service.enrollStudent(BuildConfig.SUPABASE_KEY, "Bearer " + token, enrollment)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(), "Đã thêm sinh viên thành công!", Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 409) {
                            Toast.makeText(requireContext(), "Sinh viên này đã có trong lớp rồi!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        if (isAdded()) Toast.makeText(requireContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // --- ADAPTER CHO VIEWPAGER ---
    private static class ViewPagerAdapter extends FragmentStateAdapter {
        private final String courseId;

        public ViewPagerAdapter(@NonNull Fragment fragment, String courseId) {
            super(fragment);
            this.courseId = courseId;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Bundle args = new Bundle();
            args.putString("COURSE_ID", courseId);

            Fragment fragment;
            switch (position) {
                case 0: fragment = new TeacherTasksFragment(); break;
                case 1: fragment = new TeacherStudentsFragment(); break;
                default: fragment = new TeacherMaterialsFragment(); break;
            }
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
}
package com.synguyen.se114project.ui.teacher.coursedetail;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
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

public class TeacherCourseDetailActivity extends AppCompatActivity {

    private String courseId;
    private String courseName;
    private FloatingActionButton fabAddStudent;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_course_detail);

        // Lấy Token để dùng cho API
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        token = prefs.getString("ACCESS_TOKEN", null);

        // 1. Nhận dữ liệu
        if (getIntent() != null) {
            courseId = getIntent().getStringExtra("COURSE_ID");
            courseName = getIntent().getStringExtra("COURSE_NAME");
        }

        // 2. Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarDetail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(courseName != null ? courseName : "Chi tiết môn học");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // 3. Setup UI Components
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        fabAddStudent = findViewById(R.id.fabAddStudent); // Đã thêm trong XML

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
        // Chỉ hiện nút khi đang ở Tab "Sinh viên" (index 1)
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 1) { // Tab Sinh viên
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thêm sinh viên vào lớp");
        builder.setMessage("Nhập Mã Sinh Viên (User Code):");

        final EditText input = new EditText(this);
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

        // Gọi API tìm profile theo user_code
        service.getProfileByCode(BuildConfig.SUPABASE_KEY, "Bearer " + token, "eq." + code)
                .enqueue(new Callback<List<Profile>>() {
                    @Override
                    public void onResponse(Call<List<Profile>> call, Response<List<Profile>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            // Lấy kết quả đầu tiên tìm thấy
                            Profile student = response.body().get(0);
                            showConfirmDialog(student);
                        } else {
                            Toast.makeText(TeacherCourseDetailActivity.this, "Không tìm thấy sinh viên có mã: " + code, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Profile>> call, Throwable t) {
                        Toast.makeText(TeacherCourseDetailActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showConfirmDialog(Profile student) {
        new AlertDialog.Builder(this)
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

        // Tạo đối tượng Enrollment (courseId lấy từ Intent, studentId lấy từ Profile tìm được)
        Enrollment enrollment = new Enrollment(courseId, studentId);

        service.enrollStudent(BuildConfig.SUPABASE_KEY, "Bearer " + token, enrollment)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(TeacherCourseDetailActivity.this, "Đã thêm sinh viên thành công!", Toast.LENGTH_SHORT).show();

                            // Mẹo: Gửi sự kiện để Fragment Sinh viên cập nhật lại danh sách (nếu cần)
                            // Hoặc đơn giản là nhắc GV vuốt xuống để refresh bên Fragment kia
                        } else if (response.code() == 409) {
                            // Lỗi 409 Conflict: Do ràng buộc UNIQUE(course_id, user_id)
                            Toast.makeText(TeacherCourseDetailActivity.this, "Sinh viên này đã có trong lớp rồi!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(TeacherCourseDetailActivity.this, "Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(TeacherCourseDetailActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // --- ADAPTER CHO VIEWPAGER ---
    private static class ViewPagerAdapter extends FragmentStateAdapter {
        private final String courseId;

        public ViewPagerAdapter(@NonNull AppCompatActivity fragmentActivity, String courseId) {
            super(fragmentActivity);
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
                case 1: fragment = new TeacherStudentsFragment(); break; // Fragment hiển thị danh sách SV
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
package com.synguyen.se114project.ui.teacher;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.synguyen.se114project.R;

public class TeacherCourseDetailActivity extends AppCompatActivity {

    private String courseId;
    private String courseName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_course_detail);

        // 1. Nhận dữ liệu từ màn hình danh sách gửi sang
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
        toolbar.setNavigationOnClickListener(v -> finish()); // Nút back

        // 3. Setup ViewPager & Tabs
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);

        ViewPagerAdapter adapter = new ViewPagerAdapter(this, courseId);
        viewPager.setAdapter(adapter);

        // Gắn tên cho các Tab
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Bài tập"); break;
                case 1: tab.setText("Sinh viên"); break;
                case 2: tab.setText("Tài liệu"); break;
            }
        }).attach();
    }

    // Class Adapter nội bộ để quản lý 3 Fragment
    private static class ViewPagerAdapter extends FragmentStateAdapter {
        private final String courseId;

        public ViewPagerAdapter(@NonNull AppCompatActivity fragmentActivity, String courseId) {
            super(fragmentActivity);
            this.courseId = courseId;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            // Truyền courseId vào cho các Fragment con để chúng biết cần load dữ liệu của lớp nào
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
            return 3; // Tổng cộng 3 tab
        }
    }
}
package com.synguyen.se114project.ui.teacher.main;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.synguyen.se114project.R;

public class TeacherMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_main);

        // 1. Ánh xạ Bottom Navigation từ file XML
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav_teacher);

        // 2. Lấy NavController từ NavHostFragment
        // (Lưu ý: Phải dùng getSupportFragmentManager để tìm FragmentContainerView)
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_teacher);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();

            // 3. KẾT NỐI THẦN THÁNH: Setup BottomNav với NavController
            // Lệnh này thay thế hoàn toàn Adapter và setOnItemSelectedListener thủ công
            NavigationUI.setupWithNavController(bottomNav, navController);
        }

        // Fab: mở danh sách Community
//        com.google.android.material.floatingactionbutton.FloatingActionButton fab = findViewById(R.id.fabCommunity);
//        if (fab != null) {
//            fab.setOnClickListener(v -> {
//                startActivity(new android.content.Intent(TeacherMainActivity.this, com.synguyen.se114project.ui.community.CommunityListActivity.class));
//            });
//        }
    }
}
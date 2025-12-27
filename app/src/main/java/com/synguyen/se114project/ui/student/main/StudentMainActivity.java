package com.synguyen.se114project.ui.student.main;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import java.util.concurrent.TimeUnit;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.synguyen.se114project.R;
import com.synguyen.se114project.worker.SyncWorker;

public class StudentMainActivity extends AppCompatActivity {

    private NavController navController;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_main);

        // 1. Tìm BottomNav
        bottomNavigationView = findViewById(R.id.bottom_nav_view);

        // 2. Tìm NavController từ NavHostFragment
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            // 3. KẾT NỐI BottomNav với NavController
            NavigationUI.setupWithNavController(bottomNavigationView, navController);

            // 4. Logic ẩn/hiện BottomNav thông minh
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int destId = destination.getId();
                // LƯU Ý: Đảm bảo ID này khớp với ID trong nav_graph.xml
                if (destId == R.id.studentTaskAddEditFragment || destId == R.id.studentTaskDetailFragment) {
                    bottomNavigationView.setVisibility(View.GONE);
                } else {
                    bottomNavigationView.setVisibility(View.VISIBLE);
                }
            });
        }
        setupAutoSync();
    }
    private void setupAutoSync() {
        // 1. Tạo điều kiện: Chỉ chạy khi có mạng (CONNECTED)
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        // 2. Tạo yêu cầu chạy định kỳ (mỗi 15 phút - thời gian tối thiểu của Android)
        PeriodicWorkRequest syncRequest =
                new PeriodicWorkRequest.Builder(SyncWorker.class, 15, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .build();

        // 3. Đưa vào hàng đợi (EnqueueUnique để không bị trùng lặp)
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "SyncTasksWork",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP, // Nếu đang có rồi thì giữ nguyên
                syncRequest
        );
    }
}
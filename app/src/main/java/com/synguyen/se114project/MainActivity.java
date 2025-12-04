package com.synguyen.se114project;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.synguyen.se114project.data.entity.Subtask;
import com.synguyen.se114project.data.entity.Task;
import com.synguyen.se114project.viewmodel.MainViewModel;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private NavController navController;
    private BottomNavigationView bottomNavigationView;
    private MainViewModel mainViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // 1. Tìm BottomNav
        bottomNavigationView = findViewById(R.id.bottom_nav_view);

        // 2. Tìm NavController từ NavHostFragment
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            // 3. KẾT NỐI BottomNav với NavController
            // Dòng này làm mọi phép thuật: Nó tự động bắt sự kiện click item menu
            // và chuyển đến fragment có ID tương ứng trong nav_graph
            NavigationUI.setupWithNavController(bottomNavigationView, navController);

            // 4. (Tùy chọn) Ẩn BottomNav khi vào màn hình Thêm/Sửa Task
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (destination.getId() == R.id.action_homeFragment_to_addEditFragment) {
                    bottomNavigationView.setVisibility(View.GONE);
                } else {
                    bottomNavigationView.setVisibility(View.VISIBLE);
                }
            });
        }
//        // ==================================================================
//        // CODE TẠM THỜI: THÊM DỮ LIỆU MẪU (TEST DATA)
//        // ==================================================================
//
//        // 2. Khởi tạo ViewModel
//        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
//
//        // 3. Tạo Task cha (Hôm nay, lúc 14:00)
//        long today = System.currentTimeMillis();
//        Task dummyTask = new Task(
//                "Test Detail UI",       // Title
//                "Check checkbox and calendar", // Subtitle
//                today,                  // Date (Long)
//                "14:00",                // Time
//                "Testing",              // Tag
//                2
//        );
//
//        // 4. Tạo danh sách Subtask
//        List<Subtask> dummySubtasks = new ArrayList<>();
//        // taskId để tạm là 0, Repository sẽ tự cập nhật sau khi lưu Task cha
//        dummySubtasks.add(new Subtask(0, "Kiểm tra hiển thị Subtask 1"));
//        dummySubtasks.add(new Subtask(0, "Thử check vào ô vuông"));
//        dummySubtasks.add(new Subtask(0, "Kiểm tra nút Edit"));
//
//        // 5. Gọi hàm Lưu (Chỉ chạy 1 lần rồi comment lại để tránh trùng lặp)
//        mainViewModel.saveTask(dummyTask, dummySubtasks);
//        Toast.makeText(this, "Đã thêm Task mẫu!", Toast.LENGTH_SHORT).show();
//
//        // ==================================================================// Priority (High)
    }
}
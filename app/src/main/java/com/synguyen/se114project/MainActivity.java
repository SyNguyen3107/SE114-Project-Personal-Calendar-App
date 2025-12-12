package com.synguyen.se114project;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private BottomNavigationView bottomNavigationView;

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
            // Nó tự động highlight icon tương ứng với Fragment đang hiển thị
            NavigationUI.setupWithNavController(bottomNavigationView, navController);

            // 4. Logic ẩn/hiện BottomNav thông minh
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int destId = destination.getId();

                // Ẩn thanh menu dưới khi vào các màn hình chi tiết hoặc nhập liệu
                // LƯU Ý: Hãy đảm bảo ID này khớp với ID trong file nav_graph.xml của bạn
                if (destId == R.id.addEditFragment || destId == R.id.taskDetailFragment) {
                    bottomNavigationView.setVisibility(View.GONE);
                } else {
                    bottomNavigationView.setVisibility(View.VISIBLE);
                }
            });
        }
    }
}
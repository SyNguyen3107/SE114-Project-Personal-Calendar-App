package com.synguyen.se114project.ui.student;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.JsonObject;
import com.synguyen.se114project.R;
import com.synguyen.se114project.data.remote.RetrofitClient;
import com.synguyen.se114project.data.remote.SupabaseService;
import com.synguyen.se114project.ui.teacher.TeacherHomeActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
            NavigationUI.setupWithNavController(bottomNavigationView, navController);

            // 4. Logic ẩn/hiện BottomNav thông minh
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int destId = destination.getId();
                // LƯU Ý: Đảm bảo ID này khớp với ID trong nav_graph.xml
                if (destId == R.id.addEditFragment || destId == R.id.taskDetailFragment) {
                    bottomNavigationView.setVisibility(View.GONE);
                } else {
                    bottomNavigationView.setVisibility(View.VISIBLE);
                }
            });
        }

        // Gọi hàm login để test (Lưu ý: Sau này nên chuyển sang màn hình Login riêng)
//        login("synguyen3107@gmail.com", "12345678");
    }

    // --- HÀM 1: LOGIN ---
    private void login(String email, String password) {
        SupabaseService service = RetrofitClient.getClient().create(SupabaseService.class);

        JsonObject body = new JsonObject();
        body.addProperty("email", email);
        body.addProperty("password", password);

        Call<JsonObject> call = service.loginUser(RetrofitClient.SUPABASE_KEY, body);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject responseBody = response.body();

                    // 1. LẤY DỮ LIỆU
                    String accessToken = responseBody.get("access_token").getAsString();
                    String userId = responseBody.get("user").getAsJsonObject().get("id").getAsString();
                    String tokenHeader = "Bearer " + accessToken;

                    // 2. LƯU VÀO SHAREDPREFERENCES
                    SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("ACCESS_TOKEN", tokenHeader);
                    editor.putString("USER_ID", userId);
                    editor.apply();

                    Log.d("LOGIN", "Login thành công, đang check role...");

                    // 3. GỌI TIẾP API ĐỂ CHECK ROLE (Gọi hàm tách riêng bên dưới)
                    checkUserRole(userId, tokenHeader);
                } else {
                    Log.e("LOGIN", "Login thất bại: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("LOGIN", "Lỗi mạng: " + t.getMessage());
            }
        });
    }

    // --- HÀM 2: CHECK ROLE (Đã đưa ra ngoài hàm login) ---
    private void checkUserRole(String userId, String token) {
        SupabaseService service = RetrofitClient.getClient().create(SupabaseService.class);
        String queryId = "eq." + userId;

        service.getUserProfile(RetrofitClient.SUPABASE_KEY, token, queryId).enqueue(new Callback<List<JsonObject>>() {
            @Override
            public void onResponse(Call<List<JsonObject>> call, Response<List<JsonObject>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    JsonObject profile = response.body().get(0);
                    String role = profile.has("role") ? profile.get("role").getAsString() : "student";

                    // 4. CHUYỂN MÀN HÌNH
                    navigateToHome(role);
                } else {
                    // Nếu chưa có profile -> Mặc định là Student
                    navigateToHome("student");
                }
            }

            @Override
            public void onFailure(Call<List<JsonObject>> call, Throwable t) {
                Log.e("CHECK_ROLE", "Lỗi check role: " + t.getMessage());
                // Tạm thời cho vào Student nếu lỗi mạng
                navigateToHome("student");
            }
        });
    }

    // --- HÀM 3: ĐIỀU HƯỚNG ---
    private void navigateToHome(String role) {
        if ("teacher".equals(role)) {
            System.out.println("-----> CHÀO MỪNG GIẢNG VIÊN <-----");
            Intent intent = new Intent(MainActivity.this, TeacherHomeActivity.class);
            startActivity(intent);
            finish();
        } else {
            System.out.println("-----> CHÀO MỪNG SINH VIÊN <-----");

            // QUAN TRỌNG: Vì chúng ta ĐANG ở MainActivity (Home của sinh viên)
            // Nên không cần startActivity(MainActivity) nữa để tránh vòng lặp vô tận.
            // Chỉ cần thông báo hoặc cập nhật UI nếu cần.
            Toast.makeText(this, "Bạn đang ở giao diện Sinh Viên", Toast.LENGTH_SHORT).show();
        }
    }
}
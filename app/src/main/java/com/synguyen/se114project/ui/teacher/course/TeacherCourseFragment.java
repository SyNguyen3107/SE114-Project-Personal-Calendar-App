package com.synguyen.se114project.ui.teacher.course;

import android.content.Context;
import android.content.Intent;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.synguyen.se114project.BuildConfig;
import com.synguyen.se114project.R;
import com.synguyen.se114project.data.entity.Course;
import com.synguyen.se114project.data.remote.RetrofitClient;
import com.synguyen.se114project.data.remote.SupabaseService;
import com.synguyen.se114project.ui.adapter.TeacherCourseAdapter;
import com.synguyen.se114project.ui.teacher.coursedetail.TeacherCourseDetailActivity;
import com.synguyen.se114project.worker.SyncWorker;

import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// 1. Kế thừa từ Fragment
public class TeacherCourseFragment extends Fragment {

    private RecyclerView rcvCourses;
    private FloatingActionButton fabAdd;
    private TeacherCourseAdapter adapter;
    private String token;
    private String userId;

    // 2. Chuyển logic khởi tạo giao diện vào onCreateView
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate layout XML (đổi tên file layout nếu cần)
        View view = inflater.inflate(R.layout.fragment_teacher_course, container, false);

        // Lấy Context an toàn từ requireActivity()
        SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        token = prefs.getString("ACCESS_TOKEN", null);
        userId = prefs.getString("USER_ID", null);

        // 3. Ánh xạ View từ 'view'
        rcvCourses = view.findViewById(R.id.rcvCourses);
        fabAdd = view.findViewById(R.id.fabAddCourse);

        // Context dùng getContext()
        rcvCourses.setLayoutManager(new LinearLayoutManager(getContext()));

        // Khởi tạo Adapter
        adapter = new TeacherCourseAdapter(course -> {
            String id = (course.getId() != null) ? course.getId() : "";
            String name = (course.getName() != null) ? course.getName() : "Chi tiết";

            // Intent: Dùng getContext() thay cho TeacherHomeActivity.this
            Intent intent = new Intent(getContext(), TeacherCourseDetailActivity.class);
            intent.putExtra("COURSE_ID", id);
            intent.putExtra("COURSE_NAME", name);
            startActivity(intent);
        });
        rcvCourses.setAdapter(adapter);

        // Setup sự kiện
        fabAdd.setOnClickListener(v -> showAddCourseDialog());
        setupAutoSync();

        return view;
    }

    // 4. Sử dụng onResume để load lại dữ liệu khi quay lại tab này
    @Override
    public void onResume() {
        super.onResume();
        loadCourses();
    }

    private void setupAutoSync() {
        // WorkManager cần Context
        if (getContext() == null) return;

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest syncRequest =
                new PeriodicWorkRequest.Builder(SyncWorker.class, 15, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .build();

        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
                "SyncTasksWork",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
        );
    }

    private void loadCourses() {
        if (token == null) return;

        SupabaseService service = RetrofitClient.getRetrofitInstance().create(SupabaseService.class);

        // API cũ: "courses_with_stats"
        service.getCourses(BuildConfig.SUPABASE_KEY, "Bearer " + token, "eq." + userId)
                .enqueue(new Callback<List<Course>>() {
                    @Override
                    public void onResponse(Call<List<Course>> call, Response<List<Course>> response) {
                        // 5. Kiểm tra an toàn: Nếu Fragment đã bị đóng thì không update UI nữa
                        if (!isAdded() || getContext() == null) return;

                        if (response.isSuccessful() && response.body() != null) {
                            adapter.submitList(response.body());
                        } else {
                            if (response.code() == 401) {
                                Toast.makeText(getContext(), "Hết phiên đăng nhập!", Toast.LENGTH_LONG).show();
                                forceLogout();
                            } else {
                                // Xử lý lỗi
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Course>> call, Throwable t) {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Lỗi mạng", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void forceLogout() {
        if (getActivity() == null) return;

        SharedPreferences prefs = getActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(getContext(), com.synguyen.se114project.ui.auth.LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish(); // Đóng Activity cha (TeacherMainActivity)
    }

    private void showAddCourseDialog() {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Tạo Môn Học Mới");

        // Inflate layout cho Dialog
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_course, null);
        final EditText edtName = view.findViewById(R.id.edtCourseName);
        final EditText edtDesc = view.findViewById(R.id.edtCourseDesc);

        builder.setView(view);

        builder.setPositiveButton("Tạo", (dialog, which) -> {
            String name = edtName.getText().toString();
            String desc = edtDesc.getText().toString();
            if (!name.isEmpty()) {
                createCourseAPI(name, desc);
            } else {
                Toast.makeText(getContext(), "Tên không được để trống", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void createCourseAPI(String name, String description) {
        SupabaseService service = RetrofitClient.getRetrofitInstance().create(SupabaseService.class);

        Course course = new Course();
        course.setName(name);
        course.setDescription(description);
        course.setTeacherId(userId);

        service.createCourse(BuildConfig.SUPABASE_KEY, "Bearer " + token, course)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (!isAdded()) return; // Check fragment alive

                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Tạo lớp thành công!", Toast.LENGTH_SHORT).show();
                            loadCourses();
                        } else {
                            Toast.makeText(getContext(), "Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
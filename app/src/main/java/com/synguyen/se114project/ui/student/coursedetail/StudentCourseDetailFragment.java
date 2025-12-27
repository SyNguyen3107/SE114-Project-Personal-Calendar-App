package com.synguyen.se114project.ui.student.coursedetail;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;
import com.synguyen.se114project.BuildConfig;
import com.synguyen.se114project.R;
import com.synguyen.se114project.data.remote.RetrofitClient;
import com.synguyen.se114project.data.remote.SupabaseService;
import com.synguyen.se114project.data.remote.response.FileObject;
import com.synguyen.se114project.ui.adapter.MaterialAdapter;
import com.synguyen.se114project.ui.adapter.TaskAdapter;
import com.synguyen.se114project.viewmodel.student.CourseDetailViewModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentCourseDetailFragment extends Fragment {

    private String courseId;
    private CourseDetailViewModel mViewModel;

    // Views
    private TextView tvCourseName, tvTeacher, tvTime, tvNoMaterials;
    private ConstraintLayout layoutHeader;
    private RecyclerView rvTasks, rvMaterials;
    private ProgressBar pbMaterials;
    private ImageView btnBack;

    // Adapters
    private TaskAdapter taskAdapter;
    private MaterialAdapter materialAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // Lấy ID môn học được truyền từ màn hình danh sách (CourseFragment)
            courseId = getArguments().getString("classId");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_student_course_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ Views (Theo ID mới trong file XML gộp)
        tvCourseName = view.findViewById(R.id.tv_detail_course_name);
        tvTeacher = view.findViewById(R.id.tv_detail_teacher);
        tvTime = view.findViewById(R.id.tv_detail_time);
        layoutHeader = view.findViewById(R.id.layout_detail_header);

        rvTasks = view.findViewById(R.id.rv_class_tasks);
        rvMaterials = view.findViewById(R.id.rv_materials);
        pbMaterials = view.findViewById(R.id.pb_materials);
        tvNoMaterials = view.findViewById(R.id.tv_no_materials);
        btnBack = view.findViewById(R.id.btn_back);

        // 2. Khởi tạo ViewModel
        mViewModel = new ViewModelProvider(this).get(CourseDetailViewModel.class);

        // 3. Setup Adapters
        setupTaskAdapter();
        setupMaterialAdapter();

        // 4. Load Data
        if (courseId != null) {
            loadCourseInfo();
            loadTasks();
            loadMaterials();
        }

        // 5. Sự kiện Back
        btnBack.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());
    }

    private void setupTaskAdapter() {
        rvTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        taskAdapter = new TaskAdapter(task -> {
            Bundle bundle = new Bundle();
            bundle.putString("taskId", task.getId());
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_studentCourseDetailFragment_to_studentTaskDetailFragment, bundle);
        });
        rvTasks.setAdapter(taskAdapter);
    }

    private void setupMaterialAdapter() {
        rvMaterials.setLayoutManager(new LinearLayoutManager(getContext()));
        // Khởi tạo adapter, click vào file -> tải về
        materialAdapter = new MaterialAdapter(file -> downloadFile(file.name));
        rvMaterials.setAdapter(materialAdapter);
    }

    private void loadCourseInfo() {
        // Lấy thông tin chi tiết môn học từ Room
        mViewModel.getCourseById(courseId).observe(getViewLifecycleOwner(), course -> {
            if (course != null) {
                tvCourseName.setText(course.getName());
                tvTeacher.setText(course.getTeacherName());
                tvTime.setText(course.getTimeSlot());

                // Đổi màu nền Header theo màu của môn học
                try {
                    if (course.getColorHex() != null && !course.getColorHex().isEmpty()) {
                        layoutHeader.setBackgroundColor(Color.parseColor(course.getColorHex()));
                    }
                } catch (Exception e) {
                    // Màu lỗi thì giữ mặc định
                }
            }
        });
    }

    private void loadTasks() {
        // Lấy danh sách bài tập của môn này từ Room
        mViewModel.getTasksByClassId(courseId).observe(getViewLifecycleOwner(), tasks -> {
            if (tasks != null) {
                taskAdapter.submitList(tasks);
            }
        });
    }

    private void loadMaterials() {
        // Lấy danh sách tài liệu từ Supabase (Logic giống StudentMaterialsFragment)
        pbMaterials.setVisibility(View.VISIBLE);
        SharedPreferences prefs = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("ACCESS_TOKEN", "");

        SupabaseService service = RetrofitClient.getRetrofitInstance().create(SupabaseService.class);
        JsonObject body = new JsonObject();
        body.addProperty("prefix", "course_" + courseId); // Lọc file theo ID môn học
        body.addProperty("limit", 100);

        service.listFiles(BuildConfig.SUPABASE_KEY, "Bearer " + token, "materials", body)
                .enqueue(new Callback<List<FileObject>>() {
                    @Override
                    public void onResponse(Call<List<FileObject>> call, Response<List<FileObject>> response) {
                        pbMaterials.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            List<FileObject> files = response.body();
                            if (files.isEmpty()) {
                                tvNoMaterials.setVisibility(View.VISIBLE);
                                rvMaterials.setVisibility(View.GONE);
                            } else {
                                tvNoMaterials.setVisibility(View.GONE);
                                rvMaterials.setVisibility(View.VISIBLE);
                                materialAdapter.setFiles(files);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<FileObject>> call, Throwable t) {
                        pbMaterials.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Không tải được tài liệu", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void downloadFile(String fileName) {
        String url = BuildConfig.SUPABASE_URL + "/storage/v1/object/public/materials/" + fileName;
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }
}
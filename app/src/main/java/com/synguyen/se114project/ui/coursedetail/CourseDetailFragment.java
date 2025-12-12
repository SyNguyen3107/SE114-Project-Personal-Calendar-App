package com.synguyen.se114project.ui.coursedetail;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

import com.synguyen.se114project.R;
import com.synguyen.se114project.data.entity.Course;
import com.synguyen.se114project.ui.adapter.TaskAdapter;
import com.synguyen.se114project.viewmodel.CourseDetailViewModel;

public class CourseDetailFragment extends Fragment {

    private CourseDetailViewModel mViewModel;
    private TaskAdapter taskAdapter;
    private String classId;

    // Views
    private TextView tvCourseName, tvTeacherName, tvTimeSlot;
    private ConstraintLayout headerLayout;
    private RecyclerView rvClassTasks;
    private ImageView btnBack;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Nhận classId từ Bundle arguments
        if (getArguments() != null) {
            classId = getArguments().getString("classId");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Hãy chắc chắn bạn đã tạo layout fragment_course_detail.xml
        return inflater.inflate(R.layout.fragment_course_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ View
        tvCourseName = view.findViewById(R.id.tv_detail_course_name);
        tvTeacherName = view.findViewById(R.id.tv_detail_teacher);
        tvTimeSlot = view.findViewById(R.id.tv_detail_time);
        headerLayout = view.findViewById(R.id.layout_detail_header);
        rvClassTasks = view.findViewById(R.id.rv_class_tasks);
        btnBack = view.findViewById(R.id.btn_back);

        // 2. Setup RecyclerView cho danh sách bài tập
        rvClassTasks.setLayoutManager(new LinearLayoutManager(getContext()));

        // Sử dụng TaskAdapter hiện có
        taskAdapter = new TaskAdapter();
        taskAdapter.setOnItemClickListener(task -> {
            // Click vào task -> Mở chi tiết Task
            Bundle bundle = new Bundle();
            bundle.putString("taskId", task.getId());
            Navigation.findNavController(view).navigate(R.id.action_courseDetailFragment_to_taskDetailFragment, bundle);
        });
        rvClassTasks.setAdapter(taskAdapter);

        // 3. Setup ViewModel
        mViewModel = new ViewModelProvider(this).get(CourseDetailViewModel.class);

        // 4. Xử lý nút Back
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());
        }

        // 5. Load dữ liệu
        if (classId != null) {
            loadData();
        } else {
            Toast.makeText(getContext(), "Lỗi: Không tìm thấy ID lớp học", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadData() {
        // A. Load thông tin môn học (Header)
        mViewModel.getCourseById(classId).observe(getViewLifecycleOwner(), course -> {
            if (course != null) {
                updateHeaderUI(course);
            }
        });

        // B. Load danh sách Task của môn học này
        mViewModel.getTasksByClassId(classId).observe(getViewLifecycleOwner(), tasks -> {
            taskAdapter.submitList(tasks);
        });
    }

    private void updateHeaderUI(Course course) {
        tvCourseName.setText(course.getName());
        tvTeacherName.setText(course.getTeacherName());
        tvTimeSlot.setText(course.getTimeSlot());

        // Đổi màu header theo màu môn học
        try {
            if (course.getColorHex() != null && !course.getColorHex().isEmpty()) {
                headerLayout.setBackgroundColor(Color.parseColor(course.getColorHex()));
            }
        } catch (IllegalArgumentException e) {
            headerLayout.setBackgroundColor(Color.parseColor("#2196F3")); // Màu mặc định (Blue)
        }
    }
}
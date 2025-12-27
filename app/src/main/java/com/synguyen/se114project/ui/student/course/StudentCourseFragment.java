package com.synguyen.se114project.ui.student.course;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
// import android.widget.ProgressBar; // Gợi ý: Nên thêm ProgressBar

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.synguyen.se114project.R;
import com.synguyen.se114project.ui.adapter.CourseAdapter;
import com.synguyen.se114project.viewmodel.student.CourseViewModel;

public class StudentCourseFragment extends Fragment {

    private CourseViewModel mViewModel;
    private RecyclerView rvCourse;
    private CourseAdapter adapter;
    // private ProgressBar progressBar; // Nếu giao diện có ProgressBar

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_student_course, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ View
        rvCourse = view.findViewById(R.id.rv_courses);
        // progressBar = view.findViewById(R.id.progress_bar);

        rvCourse.setLayoutManager(new LinearLayoutManager(getContext()));

        // Setup Adapter
        adapter = new CourseAdapter(course -> {
            Bundle bundle = new Bundle();
            // Đảm bảo course.getId() trả về đúng UUID của lớp học trong Supabase
            bundle.putString("classId", course.getId());

            NavController navController = Navigation.findNavController(view);
            try {
                navController.navigate(R.id.action_studentCourseFragment_to_studentCourseDetailFragment, bundle);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Chưa tạo màn hình chi tiết!", Toast.LENGTH_SHORT).show();
            }
        });
        rvCourse.setAdapter(adapter);

        // Setup ViewModel
        mViewModel = new ViewModelProvider(this).get(CourseViewModel.class);

        // 1. Quan sát dữ liệu từ Room
        mViewModel.getAllCourses().observe(getViewLifecycleOwner(), courses -> {
            // Cập nhật list bất kể có rỗng hay không (để clear list nếu user đăng xuất/đổi tk)
            adapter.submitList(courses);

            // Nếu muốn hiện thông báo khi không có lớp:
            if (courses == null || courses.isEmpty()) {
                // binding.txtEmpty.setVisibility(View.VISIBLE);
            } else {
                // binding.txtEmpty.setVisibility(View.GONE);
            }
        });

        // 2. GỌI API LẤY DỮ LIỆU THẬT
        // Gọi ngay khi màn hình được tạo để đảm bảo dữ liệu mới nhất
        mViewModel.refreshStudentCourses();
    }
}
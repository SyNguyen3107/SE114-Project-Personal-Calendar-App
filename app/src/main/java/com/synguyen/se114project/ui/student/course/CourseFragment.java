package com.synguyen.se114project.ui.student.course;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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

public class CourseFragment extends Fragment {

    private CourseViewModel mViewModel;
    private RecyclerView rvCourse;
    private CourseAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_course, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvCourse = view.findViewById(R.id.rv_courses);
        rvCourse.setLayoutManager(new LinearLayoutManager(getContext()));

        // Setup Adapter
        adapter = new CourseAdapter(course -> {
            // Xử lý khi click vào môn học -> Chuyển sang màn hình chi tiết môn học
            Bundle bundle = new Bundle();
            bundle.putString("classId", course.getId()); // Truyền Class ID (UUID) sang màn hình chi tiết môn học


            NavController navController = Navigation.findNavController(view);
            try {
                // Điều hướng sang ClassDetailFragment
                navController.navigate(R.id.action_courseFragment_to_courseDetailFragment, bundle);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Chưa tạo màn hình chi tiết môn học!", Toast.LENGTH_SHORT).show();
            }
        });
        rvCourse.setAdapter(adapter);

        // Setup ViewModel
        mViewModel = new ViewModelProvider(this).get(CourseViewModel.class);

        // Quan sát dữ liệu
        mViewModel.getAllCourses().observe(getViewLifecycleOwner(), courses -> {
            if (courses == null || courses.isEmpty()) {
                // Nếu chưa có dữ liệu, tạo mẫu để test giao diện
                mViewModel.createSampleData();
            } else {
                adapter.submitList(courses);
            }
        });
    }
}
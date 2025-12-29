package com.synguyen.se114project.ui.student.course;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
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

public class StudentCourseFragment extends Fragment {

    private CourseViewModel mViewModel;
    private RecyclerView rvCourse;
    private CourseAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty; // Text hiển thị khi không có dữ liệu

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_student_course, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ View (Đảm bảo ID trong XML trùng khớp)
        rvCourse = view.findViewById(R.id.rv_courses);

        rvCourse.setLayoutManager(new LinearLayoutManager(getContext()));

        // 2. Setup Adapter
        adapter = new CourseAdapter(course -> {
            Bundle bundle = new Bundle();

            // Thay vì gửi từng trường lẻ tẻ, hãy gửi nguyên object Course
            // Key "course_data" PHẢI TRÙNG KHỚP với key bên StudentCourseDetailFragment
            bundle.putSerializable("course_data", course);
            // -----------------

            NavController navController = Navigation.findNavController(view);
            try {
                // Đảm bảo ID này đúng trong nav_graph.xml của bạn
                navController.navigate(R.id.action_studentCourseFragment_to_studentCourseDetailFragment, bundle);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Lỗi điều hướng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        rvCourse.setAdapter(adapter);

        // 3. Setup ViewModel
        mViewModel = new ViewModelProvider(this).get(CourseViewModel.class);

        // 4. Lấy USER_ID từ SharedPreferences (QUAN TRỌNG)
        // Vì SQL View trên Supabase cần student_id để lọc
        SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String userId = prefs.getString("USER_ID", null);

        // Ánh xạ views cho progress / empty
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        // 5. Quan sát dữ liệu từ Room (Offline-first)
        mViewModel.getAllCourses().observe(getViewLifecycleOwner(), courses -> {
            // Tắt loading khi có dữ liệu
            if (progressBar != null) progressBar.setVisibility(View.GONE);

            adapter.submitList(courses);

            // Xử lý hiển thị thông báo rỗng
            if (courses == null || courses.isEmpty()) {
                if (tvEmpty != null) tvEmpty.setVisibility(View.VISIBLE);
            } else {
                if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);
            }
        });

        // 6. Quan sát thông báo lỗi (Nếu ViewModel có LiveData error)
        // mViewModel.getError().observe(...) { ... }

        // 7. GỌI API LẤY DỮ LIỆU MỚI
        if (userId != null) {
            if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
            // Truyền userId vào để API gọi: getStudentCourses(..., "eq." + userId)
            mViewModel.refreshStudentCourses(userId);
        } else {
            Toast.makeText(getContext(), "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            if (progressBar != null) progressBar.setVisibility(View.GONE);
        }
    }
}
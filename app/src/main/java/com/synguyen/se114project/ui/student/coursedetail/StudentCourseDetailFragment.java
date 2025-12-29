package com.synguyen.se114project.ui.student.coursedetail; // Đảm bảo package đúng với project của bạn

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation; // Nếu dùng Navigation Component
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.synguyen.se114project.R;
import com.synguyen.se114project.data.entity.Course;
import com.synguyen.se114project.ui.adapter.CourseViewPagerAdapter;

public class StudentCourseDetailFragment extends Fragment {

    private static final String ARG_COURSE = "course_data"; // Key để nhận dữ liệu
    private Course mCourse;

    public StudentCourseDetailFragment() {
        // Required empty public constructor
    }

    // Hàm tạo instance nhanh (nếu dùng cách gọi truyền thống)
    public static StudentCourseDetailFragment newInstance(Course course) {
        StudentCourseDetailFragment fragment = new StudentCourseDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_COURSE, course);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Nhận dữ liệu Course được truyền sang
        if (getArguments() != null) {
            // Nếu dùng Navigation Component (SafeArgs) thì cách lấy sẽ khác một chút,
            // nhưng đây là cách lấy chuẩn Bundle.
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                mCourse = getArguments().getSerializable(ARG_COURSE, Course.class);
            } else {
                mCourse = (Course) getArguments().getSerializable(ARG_COURSE);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_student_course_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Setup Toolbar
        androidx.appcompat.widget.Toolbar toolbar = view.findViewById(R.id.toolbarCourseDetail);
        if (mCourse != null) {
            toolbar.setTitle(mCourse.getName());
            // Có thể set Subtitle là mã lớp nếu muốn
            // toolbar.setSubtitle(mCourse.getId());
        }

        // Xử lý nút Back trên Toolbar
        toolbar.setNavigationOnClickListener(v -> {
            // Cách quay lại màn hình trước
            Navigation.findNavController(view).navigateUp();
            // Hoặc nếu không dùng Navigation Component: getActivity().onBackPressed();
        });

        // 2. Setup ViewPager & Tabs
        TabLayout tabLayout = view.findViewById(R.id.tabLayoutCourse);
        ViewPager2 viewPager = view.findViewById(R.id.viewPagerCourse);

        if (mCourse != null) {
            // Khởi tạo Adapter (Code ở bước 3 bên dưới)
            // Truyền 'this' (Fragment hiện tại) và courseId
            CourseViewPagerAdapter adapter = new CourseViewPagerAdapter(this, mCourse.getId());
            viewPager.setAdapter(adapter);

            // Liên kết TabLayout với ViewPager
            new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                switch (position) {
                    case 0: tab.setText("Task"); break;
                    case 1: tab.setText("References"); break;
                    case 2: tab.setText("Members"); break;
                }
            }).attach();
        }
    }
}
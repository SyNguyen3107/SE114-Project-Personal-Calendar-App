package com.synguyen.se114project.ui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.synguyen.se114project.ui.student.coursedetail.CourseMaterialsFragment;
import com.synguyen.se114project.ui.student.coursedetail.CourseStudentsFragment;
import com.synguyen.se114project.ui.student.coursedetail.CourseTasksFragment;

public class CourseViewPagerAdapter extends FragmentStateAdapter {
    private final String courseId;

    // Constructor nhận vào Fragment cha để quản lý lifecycle
    public CourseViewPagerAdapter(@NonNull Fragment fragment, String courseId) {
        super(fragment);
        this.courseId = courseId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // TẠM THỜI: Chúng ta sẽ trả về các Fragment rỗng để test khung sườn trước.
        // Bạn CẦN tạo 3 file fragment java (Task, Material, Student) như bên dưới để code chạy được.

        switch (position) {
            case 0:
                return CourseTasksFragment.newInstance(courseId); // Tab 1
            case 1:
                return CourseMaterialsFragment.newInstance(courseId); // Tab 2
            case 2:
                return CourseStudentsFragment.newInstance(courseId);  // Tab 3
            default:
                return CourseTasksFragment.newInstance(courseId);
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Tổng cộng 3 tab
    }
}
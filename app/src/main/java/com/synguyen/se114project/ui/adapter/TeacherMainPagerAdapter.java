package com.synguyen.se114project.ui.adapter;
import com.synguyen.se114project.ui.teacher.course.TeacherCourseFragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.synguyen.se114project.ui.common.CommunityFragment;
import com.synguyen.se114project.ui.teacher.schedule.TeacherScheduleFragment;

public class TeacherMainPagerAdapter extends FragmentStateAdapter {

    public TeacherMainPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new TeacherScheduleFragment();
            case 1: return new TeacherCourseFragment(); // Màn hình Home cũ
            case 2: return new CommunityFragment();     // Màn hình Cộng đồng chung
            default: return new TeacherCourseFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
package com.synguyen.se114project.ui.student.coursedetail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class CourseStudentsFragment extends Fragment {
    private static final String ARG_COURSE_ID = "course_id";
    private String mCourseId;

    public CourseStudentsFragment() {} // Constructor rỗng bắt buộc

    public static CourseStudentsFragment newInstance(String courseId) {
        CourseStudentsFragment fragment = new CourseStudentsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_COURSE_ID, courseId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) mCourseId = getArguments().getString(ARG_COURSE_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Tạm thời trả về Text để test
        TextView tv = new TextView(getContext());
        tv.setText("Danh sách Task của khóa: " + mCourseId);
        tv.setGravity(android.view.Gravity.CENTER);
        return tv;
    }
}
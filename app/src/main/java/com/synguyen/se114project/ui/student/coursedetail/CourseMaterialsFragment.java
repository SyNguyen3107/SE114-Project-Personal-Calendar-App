package com.synguyen.se114project.ui.student.coursedetail;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.synguyen.se114project.R;
import com.synguyen.se114project.ui.adapter.MaterialAdapter;
import com.synguyen.se114project.viewmodel.student.CourseViewModel;

public class CourseMaterialsFragment extends Fragment {

    private static final String ARG_COURSE_ID = "course_id";
    private String mCourseId;
    private CourseViewModel mViewModel;
    private MaterialAdapter mAdapter;
    private TextView tvEmpty;

    public CourseMaterialsFragment() {}

    public static CourseMaterialsFragment newInstance(String courseId) {
        CourseMaterialsFragment fragment = new CourseMaterialsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_COURSE_ID, courseId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCourseId = getArguments().getString(ARG_COURSE_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Bạn cần tạo file xml layout cho fragment này: fragment_course_materials
        // Hoặc dùng tạm fragment_course_tasks nếu lười tạo mới (nhưng nên tạo mới cho chuẩn)
        return inflater.inflate(R.layout.fragment_course_materials, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Setup RecyclerView
        RecyclerView rv = view.findViewById(R.id.rvMaterials); // Nhớ tạo ID này trong XML
        tvEmpty = view.findViewById(R.id.tvEmptyMaterials);    // Nhớ tạo ID này trong XML

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new MaterialAdapter();
        rv.setAdapter(mAdapter);

        // 2. Setup ViewModel
        mViewModel = new ViewModelProvider(this).get(CourseViewModel.class);

        // 3. Observe Data
        mViewModel.getMaterials().observe(getViewLifecycleOwner(), files -> {
            if (files == null || files.isEmpty()) {
                tvEmpty.setVisibility(View.VISIBLE);
                rv.setVisibility(View.GONE);
            } else {
                tvEmpty.setVisibility(View.GONE);
                rv.setVisibility(View.VISIBLE);
                mAdapter.setList(files);
            }
        });

        // 4. Load Data
        if (mCourseId != null) {
            mViewModel.loadMaterials(mCourseId);
        }

        // 5. Click Download (Mở trình duyệt)
        mAdapter.setOnItemClickListener(file -> {
            // Logic mở link Supabase Storage
            // Cấu trúc link public: https://<project-id>.supabase.co/storage/v1/object/public/materials/<prefix>/<name>
            // Hoặc bạn có thể dùng hàm getPublicUrl trong Repository nếu muốn code xịn hơn.

            Toast.makeText(getContext(), "Đang mở: " + file.getName(), Toast.LENGTH_SHORT).show();
            // String url = ...;
            // Intent i = new Intent(Intent.ACTION_VIEW);
            // i.setData(Uri.parse(url));
            // startActivity(i);
        });
    }
}
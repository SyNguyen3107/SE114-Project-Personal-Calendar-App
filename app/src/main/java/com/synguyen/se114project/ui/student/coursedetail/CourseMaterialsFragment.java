package com.synguyen.se114project.ui.student.coursedetail;

import android.content.Context;
import android.content.SharedPreferences;
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

import com.synguyen.se114project.BuildConfig;
import com.synguyen.se114project.R;
import com.synguyen.se114project.ui.adapter.MaterialAdapter;
import com.synguyen.se114project.utils.FileUtils;
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
        // Đảm bảo bạn đã có file layout này
        return inflater.inflate(R.layout.fragment_course_materials, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Setup UI
        RecyclerView rv = view.findViewById(R.id.rvMaterials);
        tvEmpty = view.findViewById(R.id.tvEmptyMaterials);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new MaterialAdapter();
        rv.setAdapter(mAdapter);

        // 2. Setup ViewModel
        mViewModel = new ViewModelProvider(this).get(CourseViewModel.class);

        // 3. Quan sát dữ liệu (Observe)
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

        // 4. Quan sát lỗi (Optional - nếu ViewModel có biến error)
        // mViewModel.getError().observe(...);

        // 5. Load Data
        if (mCourseId != null) {
            // --- SỬA ĐỔI QUAN TRỌNG: Truyền Token vào hàm load ---
            String token = getUserToken();
            mViewModel.loadMaterials(token, mCourseId);
        }

        // 6. Click Download
        mAdapter.setOnItemClickListener(file -> {
            if (getContext() != null) {
                // Lưu ý: Nếu bucket là private, bạn cần token để tải.
                // Hàm downloadCourseMaterial cần được kiểm tra lại.
                FileUtils.downloadCourseMaterial(getContext(), mCourseId, file.getName());
            }
        });
    }

    // Hàm lấy Token
    private String getUserToken() {
        if (getContext() == null) return "Bearer " + BuildConfig.SUPABASE_KEY;

        SharedPreferences prefs = getContext().getSharedPreferences("MY_APP_PREFS", Context.MODE_PRIVATE);
        String token = prefs.getString("ACCESS_TOKEN", "");

        if (token.isEmpty()) {
            return "Bearer " + BuildConfig.SUPABASE_KEY; // Fallback dùng Key mặc định
        }
        return "Bearer " + token;
    }
}
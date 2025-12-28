package com.synguyen.se114project.ui.student.coursedetail;

import android.os.Bundle;
import android.util.Log;
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
import com.synguyen.se114project.data.entity.Task; // Import Entity Task
import com.synguyen.se114project.ui.adapter.TaskAdapter;
import com.synguyen.se114project.ui.student.taskdetail.StudentTaskDetailFragment;
import com.synguyen.se114project.viewmodel.student.TaskViewModel;

public class CourseTasksFragment extends Fragment {

    private static final String ARG_COURSE_ID = "course_id";
    private String mCourseId;

    private TaskViewModel mViewModel;
    private TaskAdapter mAdapter;
    private TextView tvEmpty;

    public CourseTasksFragment() {}

    public static CourseTasksFragment newInstance(String courseId) {
        CourseTasksFragment fragment = new CourseTasksFragment();
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
        return inflater.inflate(R.layout.fragment_course_tasks, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Debug: Kiểm tra xem Fragment có nhận được ID không
        Log.d("DEBUG_FRAGMENT", "onViewCreated chạy. CourseID: " + mCourseId);

        // 1. Ánh xạ View & Setup Adapter (Giữ nguyên)
        RecyclerView recyclerView = view.findViewById(R.id.rvCourseTasks);
        tvEmpty = view.findViewById(R.id.tvEmptyTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new TaskAdapter();
        recyclerView.setAdapter(mAdapter);

        // 2. Setup ViewModel (Giữ nguyên)
        mViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        // 3. Quan sát LiveData từ Room (Giữ nguyên)
        if (mCourseId != null) {
            mViewModel.getCourseTasks(mCourseId).observe(getViewLifecycleOwner(), tasks -> {
                Log.d("DEBUG_FRAGMENT", "LiveData thay đổi. Số lượng task: " + (tasks != null ? tasks.size() : "null"));
                if (tasks == null || tasks.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    mAdapter.submitList(tasks);
                }
            });

            Log.d("DEBUG_FRAGMENT", "Bắt đầu gọi lệnh refreshTasks từ ViewModel...");
            mViewModel.refreshTasks(mCourseId);
            // =================================================================

        } else {
            Log.e("DEBUG_FRAGMENT", "LỖI: mCourseId bị null!");
        }

        // 5. Xử lý sự kiện trong Adapter
        // Click vào item -> Xem chi tiết (Chưa làm, tạm thời Toast)
        mAdapter.setOnItemClickListener(task -> {
            StudentTaskDetailFragment detailFragment = new StudentTaskDetailFragment();
            Bundle args = new Bundle();
            args.putString("taskId", task.getId());
            args.putString("taskTitle", task.getTitle());

            // Đánh dấu là mở từ Course
            args.putBoolean("IS_FROM_COURSE_DETAIL", true);

            detailFragment.setArguments(args);

            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(
                                android.R.anim.fade_in, android.R.anim.fade_out,
                                android.R.anim.fade_in, android.R.anim.fade_out
                        )
                        .add(android.R.id.content, detailFragment)

                        // --- [SỬA DÒNG NÀY] ---
                        // Thay vì null, hãy đặt một cái tên định danh (Tag)
                        .addToBackStack("TASK_DETAIL_SESSION")
                        // ----------------------

                        .commit();
            }
        });

        // Click vào Checkbox -> Cập nhật trạng thái
        mAdapter.setOnTaskCheckListener((task, isChecked) -> {
            task.setCompleted(isChecked);
            mViewModel.update(task); // Cập nhật vào Database
        });
    }
}
package com.synguyen.se114project.ui.teacher.coursedetail;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.synguyen.se114project.R;
import com.synguyen.se114project.data.entity.Enrollment;
import com.synguyen.se114project.data.entity.Profile;
import com.synguyen.se114project.data.remote.RetrofitClient;
import com.synguyen.se114project.data.remote.SupabaseService;
import com.synguyen.se114project.ui.adapter.SelectStudentAdapter;
import com.synguyen.se114project.ui.teacher.materials.TeacherMaterialsFragment;
import com.synguyen.se114project.ui.teacher.students.TeacherStudentsFragment;
import com.synguyen.se114project.ui.teacher.TeacherTasksFragment;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherCourseDetailFragment extends Fragment {

    private String courseId;
    private String courseName;
    private FloatingActionButton fabAddStudent;
    private String token;
    private ViewPager2 viewPager; // Lưu biến toàn cục

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_teacher_course_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        token = prefs.getString("ACCESS_TOKEN", null);

        if (getArguments() != null) {
            courseId = getArguments().getString("COURSE_ID");
            courseName = getArguments().getString("COURSE_NAME");
        }

        Toolbar toolbar = view.findViewById(R.id.toolbarDetail);
        toolbar.setTitle(courseName != null ? courseName : "Course Detail");
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager); // Gán biến
        fabAddStudent = view.findViewById(R.id.fabAddStudent);

        if (fabAddStudent != null) {
            fabAddStudent.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.holo_blue_dark)));
        }

        ViewPagerAdapter adapter = new ViewPagerAdapter(this, courseId);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Tasks"); break;
                case 1: tab.setText("Students"); break;
                case 2: tab.setText("Materials"); break;
            }
        }).attach();

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 1) fabAddStudent.show();
                else fabAddStudent.hide();
            }
        });

        fabAddStudent.setOnClickListener(v -> showAddStudentDialog());
    }

    private void showAddStudentDialog() {
        if (getContext() == null) return;

        View v = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_student, null);
        EditText edtSearch = v.findViewById(R.id.edtSearchStudent);
        RecyclerView rvSelect = v.findViewById(R.id.rvSelectStudents);
        Button btnCancel = v.findViewById(R.id.btnCancel);
        Button btnEnroll = v.findViewById(R.id.btnEnroll);

        rvSelect.setLayoutManager(new LinearLayoutManager(getContext()));
        SelectStudentAdapter selectAdapter = new SelectStudentAdapter();
        rvSelect.setAdapter(selectAdapter);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(v)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        loadAllStudents(selectAdapter);

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                selectAdapter.filter(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnCancel.setOnClickListener(view -> dialog.dismiss());

        btnEnroll.setOnClickListener(view -> {
            List<String> selectedUserIds = selectAdapter.getSelectedUserIds();
            if (!selectedUserIds.isEmpty()) {
                enrollMultipleStudents(selectedUserIds, dialog);
            } else {
                Toast.makeText(getContext(), "Please select at least one student", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void loadAllStudents(SelectStudentAdapter adapter) {
        SupabaseService service = RetrofitClient.getRetrofitInstance().create(SupabaseService.class);
        service.getAllStudents("Bearer " + token).enqueue(new Callback<List<Profile>>() {
            @Override
            public void onResponse(Call<List<Profile>> call, Response<List<Profile>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.updateData(response.body());
                }
            }
            @Override
            public void onFailure(Call<List<Profile>> call, Throwable t) {
                if (getContext() != null) Toast.makeText(getContext(), "Failed to load students", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enrollMultipleStudents(List<String> userIds, AlertDialog dialog) {
        List<Enrollment> enrollments = new ArrayList<>();
        for (String uid : userIds) {
            enrollments.add(new Enrollment(courseId, uid));
        }

        SupabaseService service = RetrofitClient.getRetrofitInstance().create(SupabaseService.class);
        service.enrollMultipleStudents("Bearer " + token, enrollments).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (isAdded()) {
                    if (response.isSuccessful()) {
                        Toast.makeText(requireContext(), "Students enrolled successfully!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        
                        // TỰ ĐỘNG LÀM MỚI DANH SÁCH SINH VIÊN
                        refreshStudentsList();
                    } else {
                        Toast.makeText(requireContext(), "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (isAdded()) Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm gọi để làm mới danh sách sinh viên ở Fragment bên trong ViewPager
    private void refreshStudentsList() {
        if (viewPager == null) return;
        
        // Tìm Fragment TeacherStudentsFragment trong các Fragment con của Fragment này
        for (Fragment fragment : getChildFragmentManager().getFragments()) {
            if (fragment instanceof TeacherStudentsFragment) {
                ((TeacherStudentsFragment) fragment).loadStudents(); // Gọi hàm load lại dữ liệu
                break;
            }
        }
    }

    private static class ViewPagerAdapter extends FragmentStateAdapter {
        private final String courseId;
        public ViewPagerAdapter(@NonNull Fragment fragment, String courseId) {
            super(fragment);
            this.courseId = courseId;
        }
        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Bundle args = new Bundle();
            args.putString("COURSE_ID", courseId);
            Fragment fragment;
            switch (position) {
                case 0: fragment = new TeacherTasksFragment(); break;
                case 1: fragment = new TeacherStudentsFragment(); break;
                default: fragment = new TeacherMaterialsFragment(); break;
            }
            fragment.setArguments(args);
            return fragment;
        }
        @Override
        public int getItemCount() { return 3; }
    }
}
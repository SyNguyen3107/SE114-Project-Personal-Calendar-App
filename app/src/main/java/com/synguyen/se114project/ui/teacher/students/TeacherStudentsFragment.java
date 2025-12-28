package com.synguyen.se114project.ui.teacher.students;

import android.content.Context;
import android.content.SharedPreferences;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.synguyen.se114project.BuildConfig;
import com.synguyen.se114project.R;
import com.synguyen.se114project.data.entity.Profile;
import com.synguyen.se114project.data.remote.RetrofitClient;
import com.synguyen.se114project.data.remote.SupabaseService;
import com.synguyen.se114project.data.remote.response.EnrollmentResponse;
import com.synguyen.se114project.ui.adapter.StudentAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherStudentsFragment extends Fragment {

    private String courseId;
    private RecyclerView rcvStudents;
    private TextView tvStudentCount, tvEmpty;
    private StudentAdapter adapter;
    private List<Profile> mListProfile = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            courseId = getArguments().getString("COURSE_ID");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teacher_students, container, false);

        rcvStudents = view.findViewById(R.id.rcvStudents);
        tvStudentCount = view.findViewById(R.id.tvStudentCount);
        tvEmpty = view.findViewById(R.id.tvEmptyStudents);

        rcvStudents.setLayoutManager(new LinearLayoutManager(getContext()));

        // Khởi tạo Adapter
        adapter = new StudentAdapter(mListProfile);
        rcvStudents.setAdapter(adapter);

        // Gọi load dữ liệu ngay khi tạo view
        loadStudents();

        return view;
    }

    public void loadStudents() {
        if (getContext() == null) return;

        SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("ACCESS_TOKEN", null);

        if (token == null || courseId == null) {
            Log.e("TeacherStudents", "Token or CourseID is null");
            return;
        }

        SupabaseService service = RetrofitClient.getRetrofitInstance().create(SupabaseService.class);

        // Gọi API
        service.getStudentsInCourse("Bearer " + token, "eq." + courseId)
                .enqueue(new Callback<List<EnrollmentResponse>>() {
                    @Override
                    public void onResponse(Call<List<EnrollmentResponse>> call, Response<List<EnrollmentResponse>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<EnrollmentResponse> rawList = response.body();
                            Log.d("DEBUG_STUDENTS", "Raw list size: " + rawList.size());

                            mListProfile.clear();
                            for (EnrollmentResponse item : rawList) {
                                // Kiểm tra kỹ null ở đây. Nếu profile null -> RLS chưa mở
                                if (item.getProfile() != null) {
                                    mListProfile.add(item.getProfile());
                                } else {
                                    Log.w("DEBUG_STUDENTS", "Item có profile = NULL. Kiểm tra lại RLS Policy!");
                                }
                            }

                            // 1. Cập nhật số lượng
                            if (tvStudentCount != null) {
                                tvStudentCount.setText(mListProfile.size() + " sinh viên");
                            }

                            // 2. Cập nhật Adapter
                            adapter.updateData(mListProfile);

                            // 3. Ẩn hiện thông báo trống
                            if (mListProfile.isEmpty()) {
                                tvEmpty.setVisibility(View.VISIBLE);
                                rcvStudents.setVisibility(View.GONE);
                            } else {
                                tvEmpty.setVisibility(View.GONE);
                                rcvStudents.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Log.e("TeacherStudents", "Lỗi API: " + response.code());
                            Toast.makeText(getContext(), "Không tải được danh sách", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<EnrollmentResponse>> call, Throwable t) {
                        Log.e("TeacherStudents", "Lỗi mạng: " + t.getMessage());
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
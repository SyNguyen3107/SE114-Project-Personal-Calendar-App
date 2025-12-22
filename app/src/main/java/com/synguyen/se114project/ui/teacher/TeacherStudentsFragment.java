package com.synguyen.se114project.ui.teacher;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.synguyen.se114project.data.remote.response.EnrollmentResponse; // Import class này
import com.synguyen.se114project.ui.adapter.StudentAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherStudentsFragment extends Fragment {

    private String courseId;
    private RecyclerView rcvStudents;
    private StudentAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teacher_students, container, false);

        if (getArguments() != null) {
            courseId = getArguments().getString("COURSE_ID");
        }

        rcvStudents = view.findViewById(R.id.rcvStudents);
        rcvStudents.setLayoutManager(new LinearLayoutManager(getContext()));

        // Adapter giờ nhận List<Profile>
        adapter = new StudentAdapter(new ArrayList<>());
        rcvStudents.setAdapter(adapter);

        loadStudents();

        return view;
    }

    private void loadStudents() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("ACCESS_TOKEN", null);

        if (token == null || courseId == null) return;

        SupabaseService service = RetrofitClient.getRetrofitInstance().create(SupabaseService.class);

        // QUAN TRỌNG: Kiểu dữ liệu trong Callback phải khớp với SupabaseService
        // Call<List<EnrollmentResponse>>
        service.getStudentsInCourse(BuildConfig.SUPABASE_KEY, token, "eq." + courseId)
                .enqueue(new Callback<List<EnrollmentResponse>>() { // <--- SỬA Ở ĐÂY
                    @Override
                    public void onResponse(Call<List<EnrollmentResponse>> call, Response<List<EnrollmentResponse>> response) {
                        // Tham số 'call' và 'response' cũng phải là EnrollmentResponse
                        if (response.isSuccessful() && response.body() != null) {
                            List<EnrollmentResponse> wrapperList = response.body();

                            // Chuyển đổi từ Wrapper sang List<Profile> để đưa vào Adapter
                            List<Profile> profileList = new ArrayList<>();
                            for (EnrollmentResponse item : wrapperList) {
                                if (item.getProfile() != null) {
                                    profileList.add(item.getProfile());
                                }
                            }

                            // Cập nhật Adapter
                            adapter.updateData(profileList);

                            if (profileList.isEmpty()) {
                                Toast.makeText(getContext(), "Lớp chưa có sinh viên", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e("TeacherStudents", "Lỗi API: " + response.code());
                            Toast.makeText(getContext(), "Lỗi tải danh sách", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<EnrollmentResponse>> call, Throwable t) {
                        // Tham số 'call' cũng phải là EnrollmentResponse
                        Log.e("TeacherStudents", "Lỗi mạng: " + t.getMessage());
                        Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
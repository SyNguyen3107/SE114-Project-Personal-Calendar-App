package com.synguyen.se114project.ui.teacher;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;
import com.synguyen.se114project.R;
import com.synguyen.se114project.data.remote.RetrofitClient;
import com.synguyen.se114project.data.remote.SupabaseService;
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
        View view = inflater.inflate(R.layout.fragment_teacher_students, container, false); // Nhớ tạo layout này

        if (getArguments() != null) {
            courseId = getArguments().getString("COURSE_ID");
        }

        rcvStudents = view.findViewById(R.id.rcvStudents);
        rcvStudents.setLayoutManager(new LinearLayoutManager(getContext()));
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
        // Lọc sinh viên theo courseId
        service.getStudentsInCourse(RetrofitClient.SUPABASE_KEY, token, "eq." + courseId)
                .enqueue(new Callback<List<JsonObject>>() {
                    @Override
                    public void onResponse(Call<List<JsonObject>> call, Response<List<JsonObject>> response) {
                        // 1. IN LOG XEM ID LỚP ĐANG GỌI LÀ GÌ
                        android.util.Log.d("DEBUG_STUDENT", "Đang gọi API với Course ID: " + courseId);

                        if (response.isSuccessful() && response.body() != null) {
                            // 2. IN LOG XEM DỮ LIỆU SERVER TRẢ VỀ CÁI GÌ
                            android.util.Log.d("DEBUG_STUDENT", "Số lượng tìm thấy: " + response.body().size());
                            android.util.Log.d("DEBUG_STUDENT", "Nội dung JSON: " + response.body().toString());

                            adapter.updateData(response.body());

                            if (response.body().isEmpty()) {
                                Toast.makeText(getContext(), "Lớp chưa có sinh viên (List Empty)", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // 3. IN LOG LỖI NẾU CÓ
                            android.util.Log.e("DEBUG_STUDENT", "Lỗi API Code: " + response.code());
                            try {
                                android.util.Log.e("DEBUG_STUDENT", "Lỗi Body: " + response.errorBody().string());
                            } catch (Exception e) { e.printStackTrace(); }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<JsonObject>> call, Throwable t) {
                        android.util.Log.e("DEBUG_STUDENT", "Lỗi mạng/Crash: " + t.getMessage());
                        t.printStackTrace();
                    }
                });
    }
}
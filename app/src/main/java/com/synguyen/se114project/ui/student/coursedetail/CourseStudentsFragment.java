package com.synguyen.se114project.ui.student.coursedetail;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import com.synguyen.se114project.BuildConfig; // Import để lấy Key
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

public class CourseStudentsFragment extends Fragment {
    private static final String ARG_COURSE_ID = "course_id";
    private String mCourseId;

    private RecyclerView rvStudents;
    private StudentAdapter studentAdapter;
    private SearchView searchView;
    private List<Profile> studentList; // Dữ liệu gốc
    private ProgressBar progressBar;

    public CourseStudentsFragment() {}

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
        // Inflate layout mới tạo (fragment_course_students.xml)
        return inflater.inflate(R.layout.fragment_course_students, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ View
        rvStudents = view.findViewById(R.id.rvCourseStudents);
        searchView = view.findViewById(R.id.svStudents);

        // --- QUAN TRỌNG: Thêm dòng này để tránh lỗi crash ---
        progressBar = view.findViewById(R.id.progressBar);

        // 2. Setup RecyclerView
        setupRecyclerView();

        // 3. Setup Search
        setupSearchListener();

        // 4. Load Data (Chỉ gọi khi có CourseID)
        if (mCourseId != null) {
            loadStudentData();
        } else {
            Toast.makeText(getContext(), "Không tìm thấy ID khóa học", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupRecyclerView() {
        rvStudents.setLayoutManager(new LinearLayoutManager(getContext()));
        studentList = new ArrayList<>();
        // Khởi tạo Adapter với list rỗng trước
        studentAdapter = new StudentAdapter(studentList);
        rvStudents.setAdapter(studentAdapter);
    }

    private void setupSearchListener() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (studentAdapter != null) {
                    studentAdapter.searchStudents(query);
                }
                searchView.clearFocus(); // Ẩn bàn phím
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (studentAdapter != null) {
                    studentAdapter.searchStudents(newText);
                }
                return true;
            }
        });
    }

    // Trong hàm loadStudentData của CourseStudentsFragment.java

    private void loadStudentData() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        String query = "eq." + mCourseId;

        // Lấy token (Đảm bảo có chữ Bearer)
        String token = "Bearer " + BuildConfig.SUPABASE_KEY;
        // Lưu ý: Nếu bạn đã đăng nhập, hãy thử lấy Token thật từ SharedPreferences

        SupabaseService service = RetrofitClient.getRetrofitInstance().create(SupabaseService.class);

        service.getStudentsInCourse(token, query).enqueue(new Callback<List<EnrollmentResponse>>() {
            @Override
            public void onResponse(Call<List<EnrollmentResponse>> call, Response<List<EnrollmentResponse>> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);

                // --- DEBUG LOGGING ---
                if (response.isSuccessful()) {
                    List<EnrollmentResponse> data = response.body();
                    if (data == null || data.isEmpty()) {
                        Log.e("DEBUG_APP", "Server trả về thành công nhưng danh sách RỖNG. Kiểm tra lại CourseID: " + mCourseId);
                        // Có thể do CourseID không khớp hoặc RLS trên Supabase chặn Anon Key
                    } else {
                        Log.d("DEBUG_APP", "Lấy được " + data.size() + " bản ghi raw.");
                        // Xử lý trích xuất Profile như cũ
                        List<Profile> profiles = new ArrayList<>();
                        for (EnrollmentResponse e : data) {
                            if (e.getProfile() != null) profiles.add(e.getProfile());
                        }
                        studentAdapter.updateData(profiles);
                    }
                } else {
                    // Nếu code là 401: Lỗi Token/Quyền
                    // Nếu code là 400: Lỗi cú pháp query
                    Log.e("DEBUG_APP", "Lỗi API Student. Code: " + response.code() + ". Msg: " + response.message());
                    try {
                        // In ra nội dung lỗi chi tiết từ server
                        if (response.errorBody() != null) {
                            Log.e("DEBUG_APP", "Chi tiết lỗi: " + response.errorBody().string());
                        }
                    } catch (Exception e) {}
                }
            }

            @Override
            public void onFailure(Call<List<EnrollmentResponse>> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Log.e("DEBUG_APP", "Lỗi mạng: " + t.getMessage());
            }
        });
    }
    // Hàm hỗ trợ lấy Token từ bộ nhớ (Bạn check xem lúc Login bạn lưu key tên là gì nhé)
    private String getUserToken() {
        SharedPreferences prefs = getContext().getSharedPreferences("MY_APP_PREFS", Context.MODE_PRIVATE);
        String token = prefs.getString("ACCESS_TOKEN", ""); // "ACCESS_TOKEN" là key bạn dùng lúc lưu
        return "Bearer " + token;
    }
}
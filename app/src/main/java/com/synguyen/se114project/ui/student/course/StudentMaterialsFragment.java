package com.synguyen.se114project.ui.student.course;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;
import com.synguyen.se114project.BuildConfig;
import com.synguyen.se114project.R;
import com.synguyen.se114project.data.remote.RetrofitClient;
import com.synguyen.se114project.data.remote.SupabaseService;
import com.synguyen.se114project.data.remote.response.FileObject;
// Bạn cần tạo Adapter đơn giản để hiển thị tên file, tôi sẽ dùng code ngắn gọn ở đây
import com.synguyen.se114project.ui.adapter.SimpleFileAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentMaterialsFragment extends Fragment {

    private String courseId;
    private RecyclerView rcvMaterials;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private SimpleFileAdapter adapter; // Cần tạo class này

    public static StudentMaterialsFragment newInstance(String courseId) {
        StudentMaterialsFragment fragment = new StudentMaterialsFragment();
        Bundle args = new Bundle();
        args.putString("COURSE_ID", courseId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_student_materials, container, false);
        if (getArguments() != null) courseId = getArguments().getString("COURSE_ID");

        rcvMaterials = view.findViewById(R.id.rcvMaterials);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        rcvMaterials.setLayoutManager(new LinearLayoutManager(getContext()));

        // Setup Adapter với sự kiện click để Download
        adapter = new SimpleFileAdapter(new ArrayList<>(), file -> {
            downloadFile(file.name);
        });
        rcvMaterials.setAdapter(adapter);

        loadMaterials();
        return view;
    }

    private void loadMaterials() {
        progressBar.setVisibility(View.VISIBLE);
        SharedPreferences prefs = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("ACCESS_TOKEN", "");

        SupabaseService service = RetrofitClient.getRetrofitInstance().create(SupabaseService.class);

        // Tạo body để lọc file theo Course ID
        JsonObject body = new JsonObject();
        body.addProperty("prefix", "course_" + courseId); // QUAN TRỌNG: Chỉ lấy file của môn này
        body.addProperty("limit", 100);

        service.listFiles( "Bearer " + token, "materials", body)
                .enqueue(new Callback<List<FileObject>>() {
                    @Override
                    public void onResponse(Call<List<FileObject>> call, Response<List<FileObject>> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            List<FileObject> files = response.body();
                            if (files.isEmpty()) {
                                tvEmpty.setVisibility(View.VISIBLE);
                            } else {
                                tvEmpty.setVisibility(View.GONE);
                                adapter.updateData(files);
                            }
                        } else {
                            Toast.makeText(getContext(), "Lỗi tải tài liệu", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<FileObject>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void downloadFile(String fileName) {
        // Tạo Public URL để mở bằng trình duyệt
        String url = BuildConfig.SUPABASE_URL + "/storage/v1/object/public/materials/" + fileName;
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }
}
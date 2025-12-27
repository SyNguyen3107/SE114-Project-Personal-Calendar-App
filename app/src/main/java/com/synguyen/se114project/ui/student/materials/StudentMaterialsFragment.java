package com.synguyen.se114project.ui.student.materials;

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
import com.synguyen.se114project.ui.adapter.MaterialAdapter;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentMaterialsFragment extends Fragment {

    private RecyclerView rcvMaterials;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private MaterialAdapter adapter;
    private String courseId;
    private static final String BUCKET_NAME = "materials";
    private static final String SUPABASE_PROJECT_ID = "lazxmtosowirorbweoxh";

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
        return inflater.inflate(R.layout.fragment_student_materials, container, false); // Đảm bảo đã tạo layout này
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rcvMaterials = view.findViewById(R.id.rcvMaterials);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        setupRecyclerView();
        loadMaterials();
    }
    private void setupRecyclerView() {
//        rcvMaterials.setLayoutManager(new LinearLayoutManager(getContext()));
//        adapter = new MaterialAdapter(file -> {
//            openFileInBrowser(file.getName());
//        });
//        rcvMaterials.setAdapter(adapter);
    }

    private void loadMaterials() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("ACCESS_TOKEN", "");

        SupabaseService service = RetrofitClient.getRetrofitInstance().create(SupabaseService.class);

        // Body để list files: Cần chỉ định folder path (prefix)
        JsonObject body = new JsonObject();
        body.addProperty("prefix", ""); // "" nghĩa là lấy tất cả file trong bucket (hoặc "course_id/" nếu chia thư mục)

//        service.listFiles(BuildConfig.SUPABASE_KEY, "Bearer " + token, BUCKET_NAME, body)
//                .enqueue(new Callback<List<FileObject>>() {
//                    @Override
//                    public void onResponse(Call<List<FileObject>> call, Response<List<FileObject>> response) {
//                        progressBar.setVisibility(View.GONE);
//                        if (response.isSuccessful() && response.body() != null) {
//                            List<FileObject> files = response.body();
//
//                            // Lọc bỏ các object là Folder (thường size = 0 hoặc name kết thúc bằng /)
//                            // Ở đây hiển thị hết
//                            if (files.isEmpty()) {
//                                tvEmpty.setVisibility(View.VISIBLE);
//                            } else {
//                                adapter.setFiles(files);
//                            }
//                        } else {
//                            Toast.makeText(getContext(), "Không tải được tài liệu", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(Call<List<FileObject>> call, Throwable t) {
//                        progressBar.setVisibility(View.GONE);
//                        Toast.makeText(getContext(), "Lỗi mạng", Toast.LENGTH_SHORT).show();
//                    }
//                });
    }

    private void openFileInBrowser(String fileName) {

        // Cần lấy Base URL từ RetrofitClient hoặc hardcode Project ID
        String baseUrl = BuildConfig.SUPABASE_URL;
        String fullUrl = baseUrl + "/storage/v1/object/public/" + BUCKET_NAME + "/" + fileName;

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(fullUrl));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Không tìm thấy ứng dụng mở file này", Toast.LENGTH_SHORT).show();
        }
    }
}
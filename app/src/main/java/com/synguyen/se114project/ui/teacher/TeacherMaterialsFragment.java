package com.synguyen.se114project.ui.teacher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.synguyen.se114project.BuildConfig;
import com.synguyen.se114project.R;
import com.synguyen.se114project.data.remote.RetrofitClient;
import com.synguyen.se114project.data.remote.SupabaseService;
import com.synguyen.se114project.utils.FileUtils;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherMaterialsFragment extends Fragment {

    private static final String ARG_COURSE_ID = "COURSE_ID";
    private String courseId;

    // UI Components
    private Button btnUpload;
    private ProgressBar progressBar;
    private TextView tvStatus;

    // Launcher để nhận kết quả khi chọn file
    private ActivityResultLauncher<Intent> filePickerLauncher;

    public TeacherMaterialsFragment() {
        // Required empty public constructor
    }

    // Factory method nhận Course ID
    public static TeacherMaterialsFragment newInstance(String courseId) {
        TeacherMaterialsFragment fragment = new TeacherMaterialsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_COURSE_ID, courseId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            courseId = getArguments().getString(ARG_COURSE_ID);
        }

        // ĐĂNG KÝ LAUNCHER (Phải làm trong onCreate)
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedUri = result.getData().getData();
                        if (selectedUri != null) {
                            // Người dùng đã chọn file -> Bắt đầu Upload
                            uploadFileToSupabase(selectedUri);
                        }
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout đã cập nhật có nút Upload (fragment_teacher_materials.xml)
        View view = inflater.inflate(R.layout.fragment_teacher_materials, container, false);

        // Ánh xạ View
        btnUpload = view.findViewById(R.id.btnUploadMaterial);
        progressBar = view.findViewById(R.id.progressBarMaterial);
        tvStatus = view.findViewById(R.id.tvStatus);

        // Sự kiện nút Upload
        btnUpload.setOnClickListener(v -> openFilePicker());

        return view;
    }

    // Hàm mở trình chọn file của Android
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf"); // Chỉ hiện file PDF
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Tạo Intent Chooser để user chọn app quản lý file
        Intent chooser = Intent.createChooser(intent, "Chọn tài liệu PDF");
        filePickerLauncher.launch(chooser);
    }

    // --- LOGIC UPLOAD QUAN TRỌNG NHẤT ---
    private void uploadFileToSupabase(Uri fileUri) {
        // 1. Hiện loading
        setLoading(true);
        tvStatus.setText("Đang xử lý file...");

        try {
            Context context = requireContext();

            // 2. Dùng FileUtils để chuyển Uri -> File thật
            // (Đảm bảo bạn đã tạo file FileUtils.java trong package 'utils')
            File file = FileUtils.getFileFromUri(context, fileUri);

            // Log kiểm tra
            Log.d("Upload", "File path: " + file.getAbsolutePath());
            Log.d("Upload", "File size: " + file.length());

            // 3. Chuẩn bị RequestBody cho Retrofit
            // "application/pdf" là MIME Type
            RequestBody requestFile = RequestBody.create(MediaType.parse("application/pdf"), file);

            // "file" là key mà Supabase yêu cầu trong form-data
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

            // 4. Tạo tên file trên Server (Tránh trùng tên)
            // VD: course_123_17000000.pdf
            String fileNameOnServer = "course_" + courseId + "_" + System.currentTimeMillis() + ".pdf";

            // 5. Lấy Token
            SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
            String token = prefs.getString("ACCESS_TOKEN", "");

            // 6. Gọi API Upload
            SupabaseService service = RetrofitClient.getRetrofitInstance().create(SupabaseService.class);

            // bucket name = "materials" (Bạn phải tạo bucket này trên Supabase Dashboard trước)
            service.uploadFile(BuildConfig.SUPABASE_KEY, "Bearer " + token, "materials", fileNameOnServer, body)
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            setLoading(false);
                            if (response.isSuccessful()) {
                                Toast.makeText(context, "Upload thành công!", Toast.LENGTH_SHORT).show();
                                tvStatus.setText("Upload xong: " + fileNameOnServer);

                                // TODO: Bước tiếp theo (Optional)
                                // Gọi thêm 1 API nữa để lưu tên file này vào bảng 'materials' trong Database
                                // để hiển thị ra list cho sinh viên.
                            } else {
                                String err = "Lỗi " + response.code();
                                tvStatus.setText(err);
                                Toast.makeText(context, err, Toast.LENGTH_SHORT).show();
                                Log.e("Upload", "Fail: " + response.message());
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            setLoading(false);
                            tvStatus.setText("Lỗi mạng");
                            Log.e("Upload", "Error: " + t.getMessage());
                        }
                    });

        } catch (Exception e) {
            setLoading(false);
            e.printStackTrace();
            Toast.makeText(getContext(), "Lỗi đọc file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            tvStatus.setVisibility(View.VISIBLE);
            btnUpload.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            // tvStatus vẫn hiện để báo kết quả
            btnUpload.setEnabled(true);
        }
    }
}
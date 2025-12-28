package com.synguyen.se114project.viewmodel.student;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.JsonObject; // Cần thêm import này
import com.synguyen.se114project.data.entity.Course;
import com.synguyen.se114project.data.remote.RetrofitClient; // Cần thêm import này
import com.synguyen.se114project.data.remote.SupabaseService; // Cần thêm import này
import com.synguyen.se114project.data.remote.response.FileObject; // Import ngắn gọn hơn
import com.synguyen.se114project.data.repository.CourseRepository;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourseViewModel extends AndroidViewModel {

    // Biến Repository vẫn giữ để dùng cho các hàm khác (như getAllCourses)
    private final CourseRepository mRepository;
    private final LiveData<List<Course>> mAllCourses;

    // MutableLiveData để cập nhật UI
    private final MutableLiveData<List<FileObject>> mMaterials = new MutableLiveData<>();

    // Getter cho Fragment observe
    public LiveData<List<FileObject>> getMaterials() {
        return mMaterials;
    }

    public CourseViewModel(@NonNull Application application) {
        super(application);
        mRepository = new CourseRepository(application);
        mAllCourses = mRepository.getAllCourses();
    }

    public LiveData<List<Course>> getAllCourses() {
        return mAllCourses;
    }

    public void refreshStudentCourses(String studentId) {
        if (studentId == null || studentId.isEmpty()) return;
        mRepository.syncStudentCourses(studentId);
    }

    // --- CẬP NHẬT CHÍNH Ở ĐÂY ---

    /**
     * Hàm load tài liệu mới: Yêu cầu thêm tham số Token
     */
    public void loadMaterials(String token, String courseId) {
        JsonObject body = new JsonObject();

        // --- SỬA: Đổi thành chuỗi rỗng để test ---
        // Thay vì: body.addProperty("prefix", courseId + "/");
        body.addProperty("prefix", "");
        // ----------------------------------------

        String bucketName = "materials"; // Đảm bảo tên bucket đúng (documents/materials?)

        SupabaseService service = RetrofitClient.getRetrofitInstance().create(SupabaseService.class);

        // Log ra để debug
        Log.d("DEBUG_APP", "Đang tải file từ bucket: " + bucketName + " với token: " + token);

        service.listFiles(token, bucketName, body).enqueue(new Callback<List<FileObject>>() {
            @Override
            public void onResponse(Call<List<FileObject>> call, Response<List<FileObject>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("DEBUG_APP", "Tìm thấy " + response.body().size() + " file.");
                    mMaterials.postValue(response.body());
                } else {
                    // Log lỗi chi tiết từ server
                    Log.e("DEBUG_APP", "Lỗi tải file: Code " + response.code() + " - " + response.message());
                    mMaterials.postValue(new ArrayList<>());
                }
            }
            @Override
            public void onFailure(Call<List<FileObject>> call, Throwable t) {
                Log.e("DEBUG_APP", "Lỗi kết nối file: " + t.getMessage());
                mMaterials.postValue(new ArrayList<>());
            }
        });
    }
}
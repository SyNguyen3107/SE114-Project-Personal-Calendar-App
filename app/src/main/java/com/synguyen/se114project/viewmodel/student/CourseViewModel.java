package com.synguyen.se114project.viewmodel.student;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.synguyen.se114project.data.entity.Course;
import com.synguyen.se114project.data.repository.CourseRepository; // Đảm bảo bạn đã có class này

import java.util.ArrayList;
import java.util.List;

public class CourseViewModel extends AndroidViewModel {

    private final CourseRepository mRepository;
    private final LiveData<List<Course>> mAllCourses;
    private final androidx.lifecycle.MutableLiveData<List<com.synguyen.se114project.data.remote.response.FileObject>> mMaterials = new androidx.lifecycle.MutableLiveData<>();
    public LiveData<List<com.synguyen.se114project.data.remote.response.FileObject>> getMaterials() {
        return mMaterials;
    }

    public CourseViewModel(@NonNull Application application) {
        super(application);
        mRepository = new CourseRepository(application);
        // LiveData này tự động cập nhật khi Room Database thay đổi
        mAllCourses = mRepository.getAllCourses();
    }

    public LiveData<List<Course>> getAllCourses() {
        return mAllCourses;
    }
    public void refreshStudentCourses(String studentId) {
        if (studentId == null || studentId.isEmpty()) {
            return; // Kiểm tra an toàn
        }
        // Truyền studentId xuống Repository
        mRepository.syncStudentCourses(studentId);
    }
    // Hàm load materials
    public void loadMaterials(String courseId) {
        // Gọi hàm bên Repository (bạn đã có hàm này trong code cũ rồi)
        mRepository.getMaterials(courseId, new retrofit2.Callback<List<com.synguyen.se114project.data.remote.response.FileObject>>() {
            @Override
            public void onResponse(retrofit2.Call<List<com.synguyen.se114project.data.remote.response.FileObject>> call, retrofit2.Response<List<com.synguyen.se114project.data.remote.response.FileObject>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mMaterials.postValue(response.body());
                } else {
                    mMaterials.postValue(new ArrayList<>()); // Trả về rỗng nếu lỗi
                }
            }

            @Override
            public void onFailure(retrofit2.Call<List<com.synguyen.se114project.data.remote.response.FileObject>> call, Throwable t) {
                mMaterials.postValue(new ArrayList<>());
                t.printStackTrace();
            }
        });
    }
}
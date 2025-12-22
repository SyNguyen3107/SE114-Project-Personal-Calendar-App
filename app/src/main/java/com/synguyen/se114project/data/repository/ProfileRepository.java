package com.synguyen.se114project.data.repository;

import com.synguyen.se114project.BuildConfig;
import com.synguyen.se114project.data.entity.Profile; // Import Entity Profile
import com.synguyen.se114project.data.remote.RetrofitClient;
import com.synguyen.se114project.data.remote.SupabaseService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileRepository {

    private final SupabaseService service;

    public interface ResultCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    public ProfileRepository() {
        service = RetrofitClient.getRetrofitInstance().create(SupabaseService.class);
    }

    // 1. Sửa kiểu trả về của Callback từ JsonObject -> Profile
    public void getProfile(String accessToken, String userId, ResultCallback<Profile> cb) {
        service.getProfile(
                BuildConfig.SUPABASE_KEY, // Dùng BuildConfig
                "Bearer " + accessToken,
                "eq." + userId
        ).enqueue(new Callback<List<Profile>>() { // Call<List<Profile>>
            @Override
            public void onResponse(Call<List<Profile>> call, Response<List<Profile>> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) {
                    cb.onError("Load profile failed: " + response.code());
                    return;
                }
                // Trả về đối tượng Profile đầu tiên trong list
                cb.onSuccess(response.body().get(0));
            }

            @Override
            public void onFailure(Call<List<Profile>> call, Throwable t) {
                cb.onError(t.getMessage());
            }
        });
    }

    public void updateFullName(String accessToken, String userId, String newName, ResultCallback<Void> cb) {
        // 2. Tạo đối tượng Profile để update (Thay vì JsonObject)
        // Vì dùng PATCH, ta chỉ cần set những trường muốn sửa
        Profile updateBody = new Profile();
        updateBody.setFullName(newName);

        service.updateProfile(
                BuildConfig.SUPABASE_KEY,
                "Bearer " + accessToken,
                "eq." + userId,
                updateBody // Truyền đối tượng Profile vào
        ).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    cb.onError("Update profile failed: " + response.code());
                    return;
                }
                cb.onSuccess(null);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                cb.onError(t.getMessage());
            }
        });
    }
}
package com.synguyen.se114project.data.repository;

import com.google.gson.JsonObject; // 1. Import JsonObject
import com.synguyen.se114project.BuildConfig;
import com.synguyen.se114project.data.entity.Profile;
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

    // Lấy thông tin Profile
    public void getProfile(String accessToken, String userId, ResultCallback<Profile> cb) {
        service.getProfile(
                BuildConfig.SUPABASE_KEY,
                "Bearer " + accessToken,
                "eq." + userId
        ).enqueue(new Callback<List<Profile>>() {
            @Override
            public void onResponse(Call<List<Profile>> call, Response<List<Profile>> response) {
                if (!response.isSuccessful()) {
                    cb.onError("Lỗi tải profile: " + response.code());
                    return;
                }
                if (response.body() == null || response.body().isEmpty()) {
                    cb.onError("Không tìm thấy profile của user này.");
                    return;
                }
                // Trả về đối tượng Profile đầu tiên
                cb.onSuccess(response.body().get(0));
            }

            @Override
            public void onFailure(Call<List<Profile>> call, Throwable t) {
                cb.onError("Lỗi mạng: " + t.getMessage());
            }
        });
    }

    // Cập nhật tên (Dùng JsonObject để an toàn)
    public void updateFullName(String accessToken, String userId, String newName, ResultCallback<Void> cb) {
        // 2. SỬA ĐỔI QUAN TRỌNG: Dùng JsonObject thay vì Profile object
        // Để đảm bảo chỉ gửi trường full_name, không gửi đè null lên email/avatar
        JsonObject body = new JsonObject();
        body.addProperty("full_name", newName);
        // Nếu muốn update thêm user_code thì: body.addProperty("user_code", code);

        service.updateProfile(
                BuildConfig.SUPABASE_KEY,
                "Bearer " + accessToken,
                "eq." + userId,
                body // Truyền JsonObject vào
        ).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    cb.onSuccess(null);
                } else {
                    cb.onError("Lỗi cập nhật profile: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                cb.onError("Lỗi mạng: " + t.getMessage());
            }
        });
    }
}
package com.synguyen.se114project.data.repository;

import com.google.gson.JsonObject;
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

    public void getProfile(String accessToken, String userId, ResultCallback<JsonObject> cb) {
        service.getProfile(
                RetrofitClient.SUPABASE_KEY,
                "Bearer " + accessToken,
                "eq." + userId
        ).enqueue(new Callback<List<JsonObject>>() {
            @Override
            public void onResponse(Call<List<JsonObject>> call, Response<List<JsonObject>> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) {
                    cb.onError("Load profile failed: " + response.code());
                    return;
                }
                cb.onSuccess(response.body().get(0));
            }

            @Override
            public void onFailure(Call<List<JsonObject>> call, Throwable t) {
                cb.onError(t.getMessage());
            }
        });
    }

    public void updateFullName(String accessToken, String userId, String newName, ResultCallback<Void> cb) {
        JsonObject body = new JsonObject();
        body.addProperty("full_name", newName);

        service.updateProfile(
                RetrofitClient.SUPABASE_KEY,
                "Bearer " + accessToken,
                "eq." + userId,
                body
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

package com.synguyen.se114project.data.repository;

import com.google.gson.JsonObject;
import com.synguyen.se114project.data.remote.RetrofitClient;
import com.synguyen.se114project.data.remote.SupabaseService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommunityRepository {
    private final SupabaseService service;

    public interface ResultCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    public CommunityRepository() {
        service = RetrofitClient.getRetrofitInstance().create(SupabaseService.class);
    }

    public void getCommunities(android.content.Context ctx, String token, ResultCallback<List<JsonObject>> callback) {
        getCommunitiesInternal(ctx, token, callback, /*retry*/ true);
    }

    private void getCommunitiesInternal(android.content.Context ctx, String token, ResultCallback<List<JsonObject>> callback, boolean retryOn401) {
        service.getCommunities("Bearer " + token).enqueue(new Callback<List<JsonObject>>() {
            @Override
            public void onResponse(Call<List<JsonObject>> call, Response<List<JsonObject>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else if (response.code() == 401 && retryOn401) {
                    // Try refresh once
                    new AuthRepository().refreshAccessToken(ctx, new AuthRepository.ResultCallback<String>() {
                        @Override
                        public void onSuccess(String newToken) {
                            // retry with new token but don't retry again
                            getCommunitiesInternal(ctx, newToken, callback, false);
                        }

                        @Override
                        public void onError(String message) {
                            callback.onError("Failed to refresh token: " + message);
                        }
                    });
                } else {
                    callback.onError("Failed to load communities: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<JsonObject>> call, Throwable t) {
                callback.onError("Error: " + t.getMessage());
            }
        });
    }

    public void createCommunity(String token, String ownerId, String name, String description, boolean isPublic, ResultCallback<JsonObject> callback) {
        JsonObject body = new JsonObject();
        body.addProperty("name", name);
        body.addProperty("description", description);
        body.addProperty("owner_id", ownerId);
        body.addProperty("is_public", isPublic);

        service.createCommunity("Bearer " + token, body).enqueue(new Callback<List<JsonObject>>() {
            @Override
            public void onResponse(Call<List<JsonObject>> call, Response<List<JsonObject>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0));
                } else {
                    callback.onError("Failed to create community: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<JsonObject>> call, Throwable t) {
                callback.onError("Error: " + t.getMessage());
            }
        });
    }

    // Lấy messages của một community (có sắp xếp theo created_at)
    public void getMessagesByCommunity(android.content.Context ctx, String token, String communityId, ResultCallback<List<JsonObject>> callback) {
        getMessagesByCommunityInternal(ctx, token, communityId, callback, /*retry*/ true);
    }

    private void getMessagesByCommunityInternal(android.content.Context ctx, String token, String communityId, ResultCallback<List<JsonObject>> callback, boolean retryOn401) {
        service.getMessagesByCommunity("Bearer " + token, "eq." + communityId, "created_at.asc").enqueue(new Callback<List<JsonObject>>() {
            @Override
            public void onResponse(Call<List<JsonObject>> call, Response<List<JsonObject>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else if (response.code() == 401 && retryOn401) {
                    // Try refresh once
                    new AuthRepository().refreshAccessToken(ctx, new AuthRepository.ResultCallback<String>() {
                        @Override
                        public void onSuccess(String newToken) {
                            // retry with new token but don't retry again
                            getMessagesByCommunityInternal(ctx, newToken, communityId, callback, false);
                        }

                        @Override
                        public void onError(String message) {
                            callback.onError("Failed to refresh token: " + message);
                        }
                    });
                } else {
                    callback.onError("Failed to load messages: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<JsonObject>> call, Throwable t) {
                callback.onError("Error: " + t.getMessage());
            }
        });
    }
}
package com.synguyen.se114project.data.remote;

import com.google.gson.JsonObject;
import com.synguyen.se114project.data.entity.Course;
import com.synguyen.se114project.data.entity.Task;
import com.synguyen.se114project.data.remote.response.AuthResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SupabaseService {

    // 1. ĐĂNG NHẬP (Trả về AuthResponse thay vì JsonObject)
    @POST("auth/v1/token?grant_type=password")
    @Headers("Content-Type: application/json")
    Call<AuthResponse> loginUser(
            @Header("apikey") String apiKey,
            @Body JsonObject body
    );

    // 2. LẤY DANH SÁCH TASK (Trả về List<Task> - GSON tự map)
    @GET("rest/v1/tasks?select=*")
    Call<List<Task>> getTasks(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Query("owner_id") String ownerId // Truyền vào: "eq.ID_CUA_USER"
    );

    // 3. TẠO TASK MỚI (Upsert)
    @POST("rest/v1/tasks")
    @Headers({"Prefer: return=representation", "Content-Type: application/json"})
    Call<List<Task>> createTask(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Body Task task // Truyền thẳng object Task vào
    );

    // 4. LẤY PROFILE (Để check Role)
    // Giả sử bạn lưu Role trong bảng users (public)
    @GET("rest/v1/users?select=*")
    Call<List<JsonObject>> getUserProfile(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Query("id") String queryId // Truyền vào: "eq.ID_CUA_USER"
    );

    // 5. LẤY KHÓA HỌC
    @GET("rest/v1/courses?select=*")
    Call<List<Course>> getCourses(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Query("owner_id") String ownerId
    );
}
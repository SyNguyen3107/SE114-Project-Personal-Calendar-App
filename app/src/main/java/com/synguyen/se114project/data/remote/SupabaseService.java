package com.synguyen.se114project.data.remote;

import com.google.gson.JsonObject;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SupabaseService {

    // 1. ĐĂNG NHẬP (Auth)
    @POST("auth/v1/token?grant_type=password")
    @Headers("Content-Type: application/json")
    Call<JsonObject> loginUser(
            @Header("apikey") String apiKey,
            @Body JsonObject body
    );

    // 2. LẤY DANH SÁCH TASK (Database)
    // select=* nghĩa là lấy tất cả cột
    @GET("rest/v1/tasks?select=*")
    Call<List<JsonObject>> getTasks(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token, // Token lấy được sau khi login
            @Query("owner_id") String ownerId // Lọc theo user (eq.ownerId)
    );

    // 3. TẠO TASK MỚI
    @POST("rest/v1/tasks")
    @Headers("Prefer: return=representation") // Để Supabase trả về task vừa tạo
    Call<List<JsonObject>> createTask(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Body JsonObject taskJson
    );
    // 4. LẤY THÔNG TIN PROFILE (Để biết là Teacher hay Student)
    // Dùng eq.id để lọc đúng user đang đăng nhập
    @GET("rest/v1/profiles?select=*")
    Call<List<JsonObject>> getUserProfile(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Query("id") String queryId // Cú pháp: eq.USER_ID
    );
    // 5. LẤY DANH SÁCH KHÓA HỌC CỦA GIẢNG VIÊN
    @GET("rest/v1/courses?select=*")
    Call<List<JsonObject>> getCourses(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Query("owner_id") String ownerId // Lọc: chỉ lấy khóa học do user này tạo
    );

    // 6. TẠO KHÓA HỌC MỚI
    @POST("rest/v1/courses")
    @Headers("Prefer: return=representation")
    Call<List<JsonObject>> createCourse(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Body JsonObject courseJson
    );
    // 7. LẤY DANH SÁCH TASK TRONG 1 COURSE
    // Lọc theo course_id
    @GET("rest/v1/tasks?select=*")
    Call<List<JsonObject>> getTasksByCourse(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Query("course_id") String courseId
    );
    // 8. LẤY DANH SÁCH SINH VIÊN TRONG LỚP
    // select=user_id, profiles(full_name, email) -> Join bảng
    @GET("rest/v1/enrollments?select=user_id,profiles(*)")
    Call<List<JsonObject>> getStudentsInCourse(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Query("course_id") String courseId
    );
}
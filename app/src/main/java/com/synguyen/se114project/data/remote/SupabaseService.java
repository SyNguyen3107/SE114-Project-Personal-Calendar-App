package com.synguyen.se114project.data.remote;

import com.google.gson.JsonObject;
import com.synguyen.se114project.data.entity.Course;
import com.synguyen.se114project.data.entity.Task;
import com.synguyen.se114project.data.remote.response.AuthResponse;
import com.synguyen.se114project.data.remote.response.FileObject;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
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
    @GET("rest/v1/tasks?select=*")
    Call<List<Task>> getTasksByCourse(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Query("course_id") String courseId // Truyền vào: "eq.ID_KHOA_HOC"
    );
    Call<List<Course>> createCourse(String supabaseKey, String token, JsonObject json);

    // 6. Đăng kí
    @POST("auth/v1/signup")
    @Headers("Content-Type: application/json")
    Call<AuthResponse> signUpUser(
            @Header("apikey") String apiKey,
            @Body JsonObject body
    );

    // 7. Insert Profile
    @POST("rest/v1/profiles")
    @Headers({"Prefer: return=minimal", "Content-Type: application/json"})
    Call<Void> insertProfile(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token, // "Bearer <access_token>"
            @Body JsonObject body
    );

    // 8.LẤY PROFILE theo id
    @GET("rest/v1/profiles?select=*")
    Call<List<com.google.gson.JsonObject>> getProfile(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Query("id") String queryId // "eq.<user_id>"
    );

    // 9.UPDATE PROFILE theo id
    @retrofit2.http.PATCH("rest/v1/profiles")
    @Headers({"Prefer: return=minimal", "Content-Type: application/json"})
    Call<Void> updateProfile(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Query("id") String queryId, // "eq.<user_id>"
            @Body com.google.gson.JsonObject body
    );
    // THÊM: Cập nhật Task (Dùng PATCH để sửa 1 phần dữ liệu)
    @retrofit2.http.PATCH("rest/v1/tasks")
    @Headers({"Prefer: return=minimal", "Content-Type: application/json"})
    Call<Void> updateTask(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Query("id") String taskId, // eq.UUID
            @Body Task task
    );

    // THÊM: Xóa Task (Xóa mềm hoặc cứng tùy logic, ở đây là xóa cứng trên server)
    @retrofit2.http.DELETE("rest/v1/tasks")
    Call<Void> deleteTask(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Query("id") String taskId // eq.UUID
    );

    @GET("rest/v1/enrollments?select=profiles(*)")
    Call<List<JsonObject>> getStudentsInCourse(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Query("course_id") String courseId // Lưu ý: Tham số truyền vào phải có dạng "eq.ID_LOP"
    );
    @Multipart
    @POST("storage/v1/object/{bucket}/{path}")
    Call<ResponseBody> uploadFile(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Path("bucket") String bucket, // Tên bucket: "materials" hoặc "assignments"
            @Path("path") String path,     // Tên file trên server (VD: "bai_giang_1.pdf")
            @Part MultipartBody.Part file  // File thực tế
    );
    @POST("storage/v1/object/list/{bucket}")
    @Headers("Content-Type: application/json")
    Call<List<FileObject>> listFiles(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Path("bucket") String bucket, // Tên bucket: "materials"
            @Body JsonObject body
    );
}
package com.synguyen.se114project.data.remote;

import com.google.gson.JsonObject;
import com.synguyen.se114project.data.entity.Course;
import com.synguyen.se114project.data.entity.Profile;
import com.synguyen.se114project.data.entity.Task;
import com.synguyen.se114project.data.remote.response.AuthResponse;
import com.synguyen.se114project.data.remote.response.EnrollmentResponse;
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
import retrofit2.http.PATCH; // Thêm import PATCH
import retrofit2.http.DELETE; // Thêm import DELETE
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SupabaseService {

    // 1. ĐĂNG NHẬP
    @POST("auth/v1/token?grant_type=password")
    @Headers("Content-Type: application/json")
    Call<AuthResponse> loginUser(
            @Header("apikey") String apiKey,
            @Body JsonObject body // Login vẫn dùng JsonObject body là ổn
    );

    // 2. LẤY DANH SÁCH TASK
    @GET("rest/v1/tasks?select=*")
    Call<List<Task>> getTasks(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Query("owner_id") String ownerId
    );

    // 3. TẠO TASK MỚI
    @POST("rest/v1/tasks")
    @Headers({"Prefer: return=representation", "Content-Type: application/json"})
    Call<List<Task>> createTask(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Body Task task
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
            @Query("course_id") String courseId
    );
    @POST("rest/v1/courses")
    @Headers({"Prefer: return=representation", "Content-Type: application/json"})
    Call<List<Course>> createCourse(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Body Course course
    );

    // 6. ĐĂNG KÝ (Signup giữ JsonObject body là hợp lý vì logic đơn giản)
    @POST("auth/v1/signup")
    @Headers("Content-Type: application/json")
    Call<AuthResponse> signUpUser(
            @Header("apikey") String apiKey,
            @Body JsonObject body
    );

    // 7. INSERT PROFILE
    @POST("rest/v1/profiles")
    @Headers({"Prefer: return=minimal", "Content-Type: application/json"})
    Call<Void> insertProfile(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Body Profile profile
    );

    // 8. LẤY PROFILE
    @GET("rest/v1/profiles?select=*")
    Call<List<Profile>> getProfile(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Query("id") String queryId
    );

    // 9. UPDATE PROFILE
    @PATCH("rest/v1/profiles")
    @Headers({"Prefer: return=minimal", "Content-Type: application/json"})
    Call<Void> updateProfile(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Query("id") String queryId,
            @Body Profile profile
    );

    // 10. UPDATE TASK
    @PATCH("rest/v1/tasks")
    @Headers({"Prefer: return=minimal", "Content-Type: application/json"})
    Call<Void> updateTask(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Query("id") String taskId,
            @Body Task task
    );

    // 11. DELETE TASK
    @DELETE("rest/v1/tasks")
    Call<Void> deleteTask(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Query("id") String taskId
    );

    // 12. LẤY SINH VIÊN TRONG LỚP (Dùng EnrollmentResponse thay vì JsonObject)
    // Lý do: Supabase trả về dạng lồng nhau [{"profiles": {...}}]
    @GET("rest/v1/enrollments?select=profiles(*)")
    Call<List<EnrollmentResponse>> getStudentsInCourse(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Query("course_id") String courseId
    );

    // 13. UPLOAD FILE
    @Multipart
    @POST("storage/v1/object/{bucket}/{path}")
    Call<ResponseBody> uploadFile(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Path("bucket") String bucket,
            @Path("path") String path,
            @Part MultipartBody.Part file
    );

    // 14. LIST FILES
    @POST("storage/v1/object/list/{bucket}")
    @Headers("Content-Type: application/json")
    Call<List<FileObject>> listFiles(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Path("bucket") String bucket,
            @Body JsonObject body
    );
}
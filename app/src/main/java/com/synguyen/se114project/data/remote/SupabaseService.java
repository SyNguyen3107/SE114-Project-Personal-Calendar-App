package com.synguyen.se114project.data.remote;

import com.google.gson.JsonObject;
import com.synguyen.se114project.data.entity.Course;
import com.synguyen.se114project.data.entity.Enrollment;
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
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SupabaseService {

    // ===========================
    // 1. AUTHENTICATION (Đăng nhập / Đăng ký)
    // ===========================

    // Đăng nhập
    @POST("auth/v1/token?grant_type=password")
    @Headers("Content-Type: application/json")
    Call<AuthResponse> loginUser(
            @Body JsonObject body
    );

    // Đăng ký
    @POST("auth/v1/signup")
    @Headers("Content-Type: application/json")
    Call<AuthResponse> signUpUser(
            @Body JsonObject body
    );

    // ===========================
    // 2. PROFILES (Thông tin người dùng)
    // ===========================

    // Lấy Profile theo ID
    @GET("rest/v1/profiles?select=*")
    Call<List<Profile>> getProfile(
            @Header("Authorization") String token,
            @Query("id") String queryId // Cú pháp: "eq.uuid"
    );

    // Tìm Profile theo MSSV (user_code)
    @GET("rest/v1/profiles")
    Call<List<Profile>> getProfileByCode(
            @Header("Authorization") String token,
            @Query("user_code") String userCode // Cú pháp: "eq.mssv"
    );

    // LẤY TẤT CẢ SINH VIÊN (Để chọn vào lớp)
    @GET("rest/v1/profiles?role=eq.student&select=*")
    Call<List<Profile>> getAllStudents(
            @Header("Authorization") String token
    );

    // Tạo mới Profile (Sau khi đăng ký thành công)
    @POST("rest/v1/profiles")
    @Headers({"Prefer: return=minimal", "Content-Type: application/json"})
    Call<Void> insertProfile(
            @Header("Authorization") String token,
            @Body Profile profile
    );

    // ===========================
    // 7. COMMUNITY (Communities & Messages)
    // ===========================

    @GET("rest/v1/communities?select=*")
    Call<List<JsonObject>> getCommunities(
            @Header("Authorization") String token
    );

    @POST("rest/v1/communities")
    @Headers({"Prefer: return=representation", "Content-Type: application/json"})
    Call<List<JsonObject>> createCommunity(
            @Header("Authorization") String token,
            @Body JsonObject body
    );

    @GET("rest/v1/messages?select=*")
    Call<List<JsonObject>> getMessagesByCommunity(
            @Header("Authorization") String token,
            @Query("community_id") String communityQuery,
            @Query("order") String order 
    );

    @retrofit2.http.FormUrlEncoded
    @POST("auth/v1/token")
    Call<com.google.gson.JsonObject> refreshToken(
            @retrofit2.http.Field("grant_type") String grantType,
            @retrofit2.http.Field("refresh_token") String refreshToken
    );

    @PATCH("rest/v1/profiles")
    @Headers({"Prefer: return=minimal", "Content-Type: application/json"})
    Call<Void> updateProfile(
            @Header("Authorization") String token,
            @Query("id") String queryId, 
            @Body JsonObject body
    );

    // ===========================
    // 3. COURSES (Khóa học)
    // ===========================

    @GET("rest/v1/courses_with_stats?select=*")
    Call<List<Course>> getCourses(
            @Header("Authorization") String token,
            @Query("teacher_id") String teacherId 
    );

    @POST("rest/v1/courses")
    @Headers({"Prefer: return=minimal", "Content-Type: application/json"})
    Call<Void> createCourse(
            @Header("Authorization") String token,
            @Body Course course
    );

    @POST("rest/v1/courses")
    @Headers({"Prefer: return=minimal", "Content-Type: application/json"})
    Call<Void> createCourseJson(
            @Header("Authorization") String token,
            @Body com.google.gson.JsonObject body
    );

    @POST("rest/v1/rpc/get_student_courses")
    Call<List<Course>> getStudentCoursesRPC(
            @Header("Authorization") String token,
            @Body JsonObject body 
    );

    // ===========================
    // 4. ENROLLMENTS (Quản lý sinh viên trong lớp)
    // ===========================

    @POST("rest/v1/enrollments")
    @Headers({"Prefer: resolution=ignore-duplicates", "Content-Type: application/json"})
    Call<Void> enrollStudent(
            @Header("Authorization") String token,
            @Body Enrollment enrollment
    );

    // Enroll nhiều sinh viên - Thêm resolution=ignore-duplicates để bỏ qua 409
    @POST("rest/v1/enrollments")
    @Headers({"Prefer: resolution=ignore-duplicates", "Content-Type: application/json"})
    Call<Void> enrollMultipleStudents(
            @Header("Authorization") String token,
            @Body List<Enrollment> enrollments
    );

    @GET("rest/v1/enrollments?select=profiles(*)")
    Call<List<EnrollmentResponse>> getStudentsInCourse(
            @Header("Authorization") String token,
            @Query("course_id") String courseId 
    );

    // ===========================
    // 5. TASKS (Bài tập)
    // ===========================

    @GET("rest/v1/tasks")
    Call<List<Task>> getTasks(
            @Header("Authorization") String token,
            @Query("course_id") String courseIdQuery, 
            @Query("select") String select,           
            @Query("order") String order              
    );
    @GET("rest/v1/tasks?select=*")
    Call<List<Task>> getTasksByCourse(
            @Header("Authorization") String token,
            @Query("course_id") String courseId 
    );
    @POST("rest/v1/tasks")
    @Headers({"Prefer: return=representation", "Content-Type: application/json"})
    Call<List<Task>> createTask(
            @Header("Authorization") String token,
            @Body Task task
    );

    @PATCH("rest/v1/tasks")
    @Headers({"Prefer: return=minimal", "Content-Type: application/json"})
    Call<Void> updateTask(
            @Header("Authorization") String token,
            @Query("id") String taskId, 
            @Body Task task
    );

    @DELETE("rest/v1/tasks")
    Call<Void> deleteTask(
            @Header("Authorization") String token,
            @Query("id") String taskId 
    );

    // ===========================
    // 6. STORAGE (File)
    // ===========================

    @Multipart
    @POST("storage/v1/object/{bucket}/{path}")
    Call<ResponseBody> uploadFile(
            @Header("Authorization") String token,
            @Header("x-upsert") String upsert,
            @Path("bucket") String bucket,
            @Path("path") String path,
            @Part MultipartBody.Part file
    );

    @POST("storage/v1/object/list/{bucket}")
    @Headers("Content-Type: application/json")
    Call<List<FileObject>> listFiles(
            @Header("Authorization") String token,
            @Path("bucket") String bucket,
            @Body JsonObject body
    );
}
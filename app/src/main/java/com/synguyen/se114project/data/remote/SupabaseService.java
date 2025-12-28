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

    // Tạo mới Profile (Sau khi đăng ký thành công)
    @POST("rest/v1/profiles")
    @Headers({"Prefer: return=minimal", "Content-Type: application/json"})
    Call<Void> insertProfile(
            @Header("Authorization") String token,
            @Body Profile profile
    );

    // Cập nhật Profile
    @PATCH("rest/v1/profiles")
    @Headers({"Prefer: return=minimal", "Content-Type: application/json"})
    Call<Void> updateProfile(
            @Header("Authorization") String token,
            @Query("id") String queryId, // Cú pháp: "eq.uuid"
            @Body JsonObject body
    );

    // ===========================
    // 3. COURSES (Khóa học)
    // ===========================

    // Lấy danh sách khóa học (Teacher: lấy lớp mình dạy)
    @GET("rest/v1/courses_with_stats?select=*")
    Call<List<Course>> getCourses(
            @Header("Authorization") String token,
            @Query("teacher_id") String teacherId // Cú pháp: "eq.uuid"
    );

    // Tạo khóa học mới
    @POST("rest/v1/courses")
    Call<Void> createCourse(
            @Header("Authorization") String token,
            @Body Course course
    );

    // Student: Lấy danh sách khóa học đã tham gia (Dùng RPC function cho bảo mật và gọn)
    @POST("rest/v1/rpc/get_student_courses")
    Call<List<Course>> getStudentCoursesRPC(
            @Header("Authorization") String token,
            @Body JsonObject body // {"sid": "uuid"}
    );

    // ===========================
    // 4. ENROLLMENTS (Quản lý sinh viên trong lớp)
    // ===========================

    // Thêm sinh viên vào lớp
    @POST("rest/v1/enrollments")
    Call<Void> enrollStudent(
            @Header("Authorization") String token,
            @Body Enrollment enrollment
    );

    // Lấy danh sách sinh viên trong lớp (Kết quả trả về lồng nhau)
    // Sử dụng cho màn hình CourseStudentsFragment
    @GET("rest/v1/enrollments?select=profiles(*)")
    Call<List<EnrollmentResponse>> getStudentsInCourse(
            @Header("Authorization") String token,
            @Query("course_id") String courseId // Cú pháp: "eq.uuid"
    );

    // ===========================
    // 5. TASKS (Bài tập)
    // ===========================

    // Lấy danh sách Task (Tổng quát, lọc theo Course ID)
    @GET("rest/v1/tasks")
    Call<List<Task>> getTasks(
            @Header("Authorization") String token,
            @Query("course_id") String courseIdQuery, // Cú pháp: "eq.uuid"
            @Query("select") String select,           // Thường là "*"
            @Query("order") String order              // Ví dụ: "due_date.asc"
    );
    @GET("rest/v1/tasks?select=*")
    Call<List<Task>> getTasksByCourse(
            @Header("Authorization") String token,
            @Query("course_id") String courseId // Cú pháp query: "eq.uuid"
    );
    // Tạo Task mới
    @POST("rest/v1/tasks")
    @Headers({"Prefer: return=representation", "Content-Type: application/json"})
    Call<List<Task>> createTask(
            @Header("Authorization") String token,
            @Body Task task
    );

    // Cập nhật Task
    @PATCH("rest/v1/tasks")
    @Headers({"Prefer: return=minimal", "Content-Type: application/json"})
    Call<Void> updateTask(
            @Header("Authorization") String token,
            @Query("id") String taskId, // Cú pháp: "eq.uuid"
            @Body Task task
    );

    // Xóa Task
    @DELETE("rest/v1/tasks")
    Call<Void> deleteTask(
            @Header("Authorization") String token,
            @Query("id") String taskId // Cú pháp: "eq.uuid"
    );

    // ===========================
    // 6. STORAGE (File)
    // ===========================

    // Upload file
    @Multipart
    @POST("storage/v1/object/{bucket}/{path}")
    Call<ResponseBody> uploadFile(
            @Header("Authorization") String token,
            @Header("x-upsert") String upsert,
            @Path("bucket") String bucket,
            @Path("path") String path,
            @Part MultipartBody.Part file
    );

    // Lấy danh sách file
    @POST("storage/v1/object/list/{bucket}")
    @Headers("Content-Type: application/json")
    Call<List<FileObject>> listFiles(
            @Header("Authorization") String token,
            @Path("bucket") String bucket,
            @Body JsonObject body
    );
}
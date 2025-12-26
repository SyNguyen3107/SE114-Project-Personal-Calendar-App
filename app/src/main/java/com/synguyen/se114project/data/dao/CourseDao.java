package com.synguyen.se114project.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.synguyen.se114project.data.entity.Course;

import java.util.List;

@Dao
public interface CourseDao {

    // Lấy tất cả lớp học (chưa bị xóa)
    @Query("SELECT * FROM courses WHERE is_deleted = 0 ORDER BY name ASC")
    LiveData<List<Course>> getAllCourses();

    // Lấy chi tiết lớp học theo ID
    @Query("SELECT * FROM courses WHERE id = :id LIMIT 1")
    LiveData<Course> getCourseById(String id);

    // Thêm hoặc thay thế
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCourse(Course Course);

    // Thêm nhiều lớp (dùng khi khởi tạo dữ liệu mẫu)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCourses(List<Course> Courses);

    @Update
    void updateCourse(Course Course);

    // Xóa mềm
    @Query("UPDATE courses SET is_deleted = 1, is_synced = 0, last_updated = :currentTime WHERE id = :id")
    void softDeleteCourse(String id, long currentTime);

    // Sync: Lấy các lớp chưa đồng bộ

    // 2. THÊM MỚI: Hàm này để xóa sạch dữ liệu cũ (bao gồm data fake) khi Sync thành công
    // Chúng ta xóa tất cả để đảm bảo Local giống hệt Server
    @Query("DELETE FROM courses")
    void deleteAllCourses();
    @Query("SELECT * FROM courses WHERE is_synced = 0")
    List<Course> getUnsyncedCourses();
}
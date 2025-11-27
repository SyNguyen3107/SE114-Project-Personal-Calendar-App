package com.synguyen.se114project.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.synguyen.se114project.data.entity.Subtask;
import com.synguyen.se114project.data.entity.Task;
import com.synguyen.se114project.data.dao.TaskDao;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// 1. Khai báo các Entity (Bảng) và phiên bản Database
@Database(entities = {Task.class, Subtask.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    // 2. Khai báo các DAO (Cổng truy cập)
    public abstract TaskDao taskDao();

    // 3. Singleton Pattern (Đảm bảo chỉ có 1 kết nối Database duy nhất)
    private static volatile AppDatabase INSTANCE;

    // 4. Tạo bộ xử lý đa luồng (Để chạy database ở background)
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    // Hàm lấy instance của Database
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "task_database") // Tên file: task_database
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
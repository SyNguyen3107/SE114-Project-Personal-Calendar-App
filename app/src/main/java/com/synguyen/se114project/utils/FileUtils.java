package com.synguyen.se114project.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {

    /**
     * Hàm 1: Lấy tên file từ Uri
     * Ví dụ: content://.../image_123.png -> Trả về "image_123.png"
     */
    public static String getFileName(Context context, Uri uri) {
        String result = null;

        // Nếu Uri thuộc dạng "content://" (thường gặp nhất khi chọn từ thư viện)
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    // Cột DISPLAY_NAME chứa tên file hiển thị
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) {
                        result = cursor.getString(index);
                    }
                }
            } catch (Exception e) {
                Log.e("FileUtils", "Không lấy được tên file: " + e.getMessage());
            }
        }

        // Fallback: Nếu không lấy được tên, thử cắt từ đường dẫn
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }

        return result;
    }

    /**
     * Hàm 2: Quan trọng nhất - Chuyển Uri thành File thực tế
     * Cơ chế: Tạo 1 file rỗng trong Cache -> Copy từng byte từ Uri sang file đó.
     */
    public static File getFileFromUri(Context context, Uri uri) throws Exception {
        // 1. Lấy tên file gốc để đặt tên cho file tạm
        String fileName = getFileName(context, uri);

        // 2. Tạo file tạm trong thư mục Cache của app (CacheDir)
        // Dùng CacheDir để hệ thống tự dọn dẹp nếu đầy bộ nhớ, không làm rác máy user
        File tempFile = new File(context.getCacheDir(), fileName);

        // 3. Mở dòng đọc (InputStream) từ Uri và dòng ghi (OutputStream) vào file tạm
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             OutputStream outputStream = new FileOutputStream(tempFile)) {

            if (inputStream == null) {
                throw new Exception("Không thể đọc dữ liệu từ Uri này");
            }

            // 4. Copy dữ liệu (Buffer 1KB - 4KB tùy chọn)
            byte[] buffer = new byte[4 * 1024]; // 4KB buffer
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }

            outputStream.flush();
        }

        // 5. Trả về file hoàn chỉnh để Retrofit sử dụng
        return tempFile;
    }
}
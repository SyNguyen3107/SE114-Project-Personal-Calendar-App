package com.synguyen.se114project.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Toast;

import com.synguyen.se114project.BuildConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {

    // ... (Giữ nguyên phần upload: getFileName, getFileFromUri) ...
    public static String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) {
                        result = cursor.getString(index);
                    }
                }
            } catch (Exception e) {
                Log.e("FileUtils", "Không lấy được tên file: " + e.getMessage());
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public static File getFileFromUri(Context context, Uri uri) throws Exception {
        String fileName = getFileName(context, uri);
        File tempFile = new File(context.getCacheDir(), fileName);
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             OutputStream outputStream = new FileOutputStream(tempFile)) {
            if (inputStream == null) throw new Exception("Không thể đọc dữ liệu");
            byte[] buffer = new byte[4 * 1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
        }
        return tempFile;
    }

    // =================================================================================
    // PHẦN 2: DOWNLOAD - PHIÊN BẢN ĐƠN GIẢN HÓA
    // =================================================================================

    public static void downloadCourseMaterial(Context context, String courseId, String fileName) {
        try {
            String bucketName = "materials";

            // --- THAY ĐỔI QUAN TRỌNG: BỎ HẾT LOGIC NỐI CHUỖI ---
            // Nếu bạn đã xóa folder, file nằm ở root -> fileName chính là "xoay 2025.pdf"
            // Nếu file nằm trong folder -> API trả về fileName là "folder/xoay 2025.pdf"
            // -> Ta chỉ cần dùng đúng fileName đó.

            String finalPath = fileName;

            // Encode để xử lý khoảng trắng (vd: "xoay 2025.pdf" -> "xoay%202025.pdf")
            String encodedPath = Uri.encode(finalPath, "/");

            // Tạo URL Public
            String fileUrl = BuildConfig.SUPABASE_URL + "/storage/v1/object/public/" + bucketName + "/" + encodedPath;

            Log.d("DOWNLOAD_URL", "Link tải thực tế: " + fileUrl);

            // Cấu hình Request
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(fileUrl));

            // Lấy tên hiển thị (Bỏ phần thư mục nếu có) để lưu vào máy
            String displayFileName = extractFileName(fileName);

            request.setTitle(displayFileName);
            request.setDescription("Đang tải tài liệu...");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            // Lưu vào thư mục Downloads
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, displayFileName);

            DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            if (manager != null) {
                manager.enqueue(request);
                Toast.makeText(context, "Đang tải: " + displayFileName, Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(context, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    // Hàm tách tên file (vd: "abc/def.pdf" -> "def.pdf")
    private static String extractFileName(String fullPath) {
        if (fullPath == null) return "file_tai_ve";
        int lastSlash = fullPath.lastIndexOf('/');
        if (lastSlash != -1) {
            return fullPath.substring(lastSlash + 1);
        }
        return fullPath;
    }
}
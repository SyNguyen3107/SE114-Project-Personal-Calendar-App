package com.synguyen.se114project.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.synguyen.se114project.BuildConfig;
import com.synguyen.se114project.R;
import com.synguyen.se114project.data.remote.response.FileObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class SubmissionAdapter extends RecyclerView.Adapter<SubmissionAdapter.SubmissionViewHolder> {

    private List<FileObject> submissions;
    private final Context context;

    public SubmissionAdapter(Context context, List<FileObject> submissions) {
        this.context = context;
        this.submissions = submissions;
    }

    public void updateData(List<FileObject> newSubmissions) {
        this.submissions = newSubmissions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SubmissionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Đảm bảo bạn đã tạo file layout item_submission.xml
        View view = LayoutInflater.from(context).inflate(R.layout.item_submission, parent, false);
        return new SubmissionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubmissionViewHolder holder, int position) {
        FileObject file = submissions.get(position);
        holder.bind(file);
    }

    @Override
    public int getItemCount() {
        return submissions == null ? 0 : submissions.size();
    }

    class SubmissionViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentId, tvTime, tvFileName;
        ImageView imgIcon;

        public SubmissionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentId = itemView.findViewById(R.id.tvStudentId);
            tvTime = itemView.findViewById(R.id.tvSubmissionTime);
            tvFileName = itemView.findViewById(R.id.tvFileName);
//            imgIcon = itemView.findViewById(R.id.imgFileIcon);
        }

        public void bind(FileObject file) {
            // 1. Dùng Getter thay vì truy cập trực tiếp
            String fileName = file.getName();
            tvFileName.setText(fileName);

            // Icon mặc định
            if (imgIcon != null) {
                imgIcon.setImageResource(R.drawable.ic_launcher_foreground); // Hoặc icon file PDF tùy chỉnh
            }

            try {
                // 2. Parse ID từ tên file mới: assign_{taskId}_{userId}_{timestamp}.pdf
                // Ví dụ: assign_123_userABC_170000.pdf
                String[] parts = fileName.split("_");

                if (parts.length >= 3) {
                    // parts[0] = assign
                    // parts[1] = taskId
                    // parts[2] = userId (Cái ta cần)
                    // parts[3] = timestamp.pdf

                    String studentId = parts[2];
                    tvStudentId.setText("SV ID: " + studentId);
                } else {
                    tvStudentId.setText("SV: (Không xác định)");
                }

                // 3. Parse thời gian (Xử lý múi giờ UTC từ Supabase)
                // String dateStr = file.createdAt; // SAI
                // String dateStr = file.getCreatedAt(); // ĐÚNG (nếu FileObject có getter này)

                // Giả sử FileObject có created_at, ta cần lấy nó:
                // Lưu ý: Nếu FileObject chưa có getCreatedAt(), bạn cần thêm vào.
                // Ở đây tôi dùng tạm logic check null an toàn
                String dateStr = null;
                if (file.getClass().getMethod("getCreatedAt") != null) {
                    dateStr = file.getCreatedAt(); // Dùng reflection hoặc gọi thẳng nếu IDE cho phép
                }

                if (dateStr != null) {
                    // Format Supabase: 2023-12-25T15:30:00.123456+00:00
                    // Dùng ISO 8601 parser đơn giản
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                    inputFormat.setTimeZone(TimeZone.getTimeZone("UTC")); // Time gốc là UTC

                    SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());

                    Date date = inputFormat.parse(dateStr);
                    tvTime.setText("Nộp lúc: " + outputFormat.format(date));
                } else {
                    tvTime.setText("Nộp lúc: --:--");
                }
            } catch (Exception e) {
                // Fallback nếu lỗi parse
                tvStudentId.setText("SV ID: " + fileName);
                tvTime.setText("Nộp lúc: --:--");
            }

            // 4. Click mở file
            itemView.setOnClickListener(v -> {
                // Sử dụng BuildConfig.SUPABASE_URL để đảm bảo linh hoạt
                // Lưu ý: bucket 'assignments' phải PUBLIC mới mở link này được
                String baseUrl = BuildConfig.SUPABASE_URL;
                if (baseUrl.endsWith("/")) baseUrl = baseUrl.substring(0, baseUrl.length() - 1);

                String url = baseUrl + "/storage/v1/object/public/assignments/" + fileName;

                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    context.startActivity(browserIntent);
                } catch (Exception e) {
                    Toast.makeText(context, "Không mở được trình duyệt", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
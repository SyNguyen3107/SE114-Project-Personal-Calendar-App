package com.synguyen.se114project.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.synguyen.se114project.BuildConfig;
import com.synguyen.se114project.R;
import com.synguyen.se114project.data.remote.response.FileObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

        public SubmissionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentId = itemView.findViewById(R.id.tvStudentId);
            tvTime = itemView.findViewById(R.id.tvSubmissionTime);
            tvFileName = itemView.findViewById(R.id.tvFileName);
        }

        public void bind(FileObject file) {
            // Tên file format: assign_{taskId}_{studentId}.pdf
            String fileName = file.name;
            tvFileName.setText(fileName);

            try {
                // Parse lấy Student ID
                String[] parts = fileName.split("_");//tách lấy các phần từ tên file
                if (parts.length >= 3) {
                    // parts[0]="assign", parts[1]=taskId, parts[2]=studentId.pdf
                    String studentPart = parts[2];
                    if (studentPart.endsWith(".pdf")) {
                        studentPart = studentPart.substring(0, studentPart.length() - 4);
                    }
                    tvStudentId.setText("SV: " + studentPart);
                } else {
                    tvStudentId.setText("File: " + fileName);
                }

                // Parse thời gian nộp (created_at từ Supabase)
                // Format gốc: 2023-12-20T10:00:00.000Z
                if (file.createdAt != null) {
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                    SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
                    Date date = inputFormat.parse(file.createdAt);
                    tvTime.setText("Nộp lúc: " + outputFormat.format(date));
                }
            } catch (Exception e) {
                tvStudentId.setText(fileName);
                tvTime.setText("");
            }

            // Click để mở file
            itemView.setOnClickListener(v -> {
                String url = BuildConfig.SUPABASE_URL + "/storage/v1/object/public/assignments/" + fileName;
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                context.startActivity(browserIntent);
            });
        }
    }
}
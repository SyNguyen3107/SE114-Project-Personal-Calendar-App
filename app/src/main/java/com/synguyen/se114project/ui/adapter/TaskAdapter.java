package com.synguyen.se114project.ui.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.synguyen.se114project.R;
import com.synguyen.se114project.data.entity.Task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Task task);
    }

    public TaskAdapter(List<Task> taskList, OnItemClickListener listener) {
        this.taskList = taskList;
        this.listener = listener;
    }

    public void setTasks(List<Task> newTasks) {
        this.taskList = newTasks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.bind(task, listener);
    }

    @Override
    public int getItemCount() {
        return taskList != null ? taskList.size() : 0;
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        // Khai báo đầy đủ các View có trong item_task.xml
        TextView tvTitle, tvSubTitle, tvTime, tvProgressStatus, tvCountdown, tvTag, tvDate;
        ProgressBar progressBar;
        ImageView imgPriority;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSubTitle = itemView.findViewById(R.id.tvSubTitle);
            tvTime = itemView.findViewById(R.id.tvTime);
            progressBar = itemView.findViewById(R.id.progressBar);
            tvProgressStatus = itemView.findViewById(R.id.tvProgressStatus);
            tvCountdown = itemView.findViewById(R.id.tvCountdown);
            imgPriority = itemView.findViewById(R.id.imgPriority);
            tvTag = itemView.findViewById(R.id.tvTag);
            tvDate = itemView.findViewById(R.id.tvDate);
        }

        public void bind(final Task task, final OnItemClickListener listener) {
            // Gán dữ liệu cơ bản
            tvTitle.setText(task.getTitle());
            tvSubTitle.setText(task.getSubTitle());
            tvTime.setText(task.getTime());

            // Kiểm tra null trước khi set tag để tránh crash nếu layout không có
            if (tvTag != null) {
                tvTag.setText(task.getTag());
            }

            // Xử lý Date (Long -> String) an toàn
            if (tvDate != null) {
                long dateTimestamp = task.getDate();
                if (dateTimestamp > 0) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    tvDate.setText(sdf.format(new Date(dateTimestamp)));
                } else {
                    tvDate.setText("");
                }
            }

            // --- LOGIC COUNTDOWN TIMER ---
            long currentTime = System.currentTimeMillis();
            long diff = task.getDate() - currentTime;

            if (diff > 0) {
                long hours = TimeUnit.MILLISECONDS.toHours(diff);
                long minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60;

                if (hours < 24) {
                    tvCountdown.setText("Due in " + hours + "h " + minutes + "m");
                    tvCountdown.setTextColor(Color.parseColor("#FF5252")); // Đỏ cảnh báo
                } else {
                    long days = TimeUnit.MILLISECONDS.toDays(diff);
                    tvCountdown.setText("Due in " + days + " days");
                    tvCountdown.setTextColor(Color.parseColor("#4CAF50")); // Xanh an toàn
                }
            } else {
                tvCountdown.setText("Overdue");
                tvCountdown.setTextColor(Color.GRAY);
            }

            // --- LOGIC PROGRESS BAR (Tạm thời Mockup) ---
            if (progressBar != null) {
                progressBar.setProgress(0);
            }
            if (tvProgressStatus != null) {
                tvProgressStatus.setText("0% Completed");
            }

            // Priority Color
            if (imgPriority != null) {
                if (task.getPriority() == 2) { // High
                    imgPriority.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFCDD2"))); // Đỏ nhạt
                } else {
                    imgPriority.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#D1C4E9"))); // Tím nhạt
                }
            }

            itemView.setOnClickListener(v -> listener.onItemClick(task));
        }
    }
}
package com.synguyen.se114project.ui.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.synguyen.se114project.R;
import com.synguyen.se114project.data.entity.Task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TaskAdapter extends ListAdapter<Task, TaskAdapter.TaskViewHolder> {

    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Task task);
    }

    public TaskAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Task> DIFF_CALLBACK = new DiffUtil.ItemCallback<Task>() {
        @Override
        public boolean areItemsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                    oldItem.getDate() == newItem.getDate() &&
                    oldItem.getPriority() == newItem.getPriority() &&
                    oldItem.isCompleted() == newItem.isCompleted(); // Giờ đây dòng này đã HỢP LỆ
        }
    };

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = getItem(position);
        holder.bind(task, listener);
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
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
            tvTitle.setText(task.getTitle());
            tvSubTitle.setText(task.getSubTitle());
            tvTime.setText(task.getTime());

            // --- XỬ LÝ TRẠNG THÁI HOÀN THÀNH ---
            if (task.isCompleted()) {
                // Gạch ngang tiêu đề nếu đã xong
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvTitle.setTextColor(Color.GRAY);
                tvCountdown.setText("Done");
                tvCountdown.setTextColor(Color.BLUE);
            } else {
                // Bỏ gạch ngang
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                tvTitle.setTextColor(Color.BLACK);

                // Logic Countdown (Chỉ hiện khi chưa xong)
                bindCountdown(task);
            }

            if (tvTag != null) tvTag.setText(task.getTag());

            if (tvDate != null) {
                long dateTimestamp = task.getDate();
                if (dateTimestamp > 0) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    tvDate.setText(sdf.format(new Date(dateTimestamp)));
                } else {
                    tvDate.setText("");
                }
            }

            // Logic Priority
            if (imgPriority != null) {
                if (task.getPriority() == 2) {
                    imgPriority.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFCDD2")));
                } else {
                    imgPriority.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#D1C4E9")));
                }
            }

            // Mockup Progress (Bạn có thể update logic này sau)
            if (progressBar != null) progressBar.setProgress(0);

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(task);
            });
        }

        private void bindCountdown(Task task) {
            long currentTime = System.currentTimeMillis();
            long diff = task.getDate() - currentTime;

            if (diff > 0) {
                long hours = TimeUnit.MILLISECONDS.toHours(diff);
                long minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60;
                if (hours < 24) {
                    tvCountdown.setText("Due in " + hours + "h " + minutes + "m");
                    tvCountdown.setTextColor(Color.parseColor("#FF5252"));
                } else {
                    long days = TimeUnit.MILLISECONDS.toDays(diff);
                    tvCountdown.setText("Due in " + days + " days");
                    tvCountdown.setTextColor(Color.parseColor("#4CAF50"));
                }
            } else {
                tvCountdown.setText("Overdue");
                tvCountdown.setTextColor(Color.GRAY);
            }
        }
    }
}
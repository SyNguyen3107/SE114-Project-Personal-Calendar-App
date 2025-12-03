package com.synguyen.se114project.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.synguyen.se114project.R;
import com.synguyen.se114project.data.entity.Task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskVH> {

    private List<Task> items;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onClick(Task task);
    }

    public TaskAdapter(List<Task> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public static class TaskVH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubTitle, tvTime, tvTag, tvDate;
        ImageView imgPriority;

        public TaskVH(@NonNull View itemView) {
            super(itemView);
            // Đảm bảo các ID này trùng khớp với file item_task.xml
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSubTitle = itemView.findViewById(R.id.tvSubTitle);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvTag = itemView.findViewById(R.id.tvTag);
            imgPriority = itemView.findViewById(R.id.imgPriority);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }

    @NonNull
    @Override
    public TaskVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskVH holder, int position) {
        Task t = items.get(position);

        holder.tvTitle.setText(t.getTitle());
        holder.tvSubTitle.setText(t.getSubTitle());
        holder.tvTime.setText(t.getTime());
        holder.tvTag.setText(t.getTag());

        // --- SỬA LỖI TẠI ĐÂY ---
        // 1. Lấy timestamp (long)
        long dateTimestamp = t.getDate();

        // 2. Chuyển đổi sang String ngày tháng (VD: 30/11/2025)
        if (dateTimestamp > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String dateString = sdf.format(new Date(dateTimestamp));
            holder.tvDate.setText(dateString);
        } else {
            holder.tvDate.setText(""); // Nếu không có ngày thì để trống
        }
        // -----------------------

        // Xử lý hiển thị độ ưu tiên (Ví dụ: priority cao thì đậm, thấp thì mờ)
        if (holder.imgPriority != null) {
            holder.imgPriority.setAlpha(t.getPriority() > 1 ? 1f : 0.4f);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(t);
        });
    }

    @Override
    public int getItemCount() {
        return (items != null) ? items.size() : 0; // Kiểm tra null cho an toàn
    }

    public void setTasks(List<Task> newTasks) {
        this.items = newTasks;
        notifyDataSetChanged();
    }
}
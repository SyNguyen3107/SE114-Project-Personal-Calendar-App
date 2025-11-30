package com.synguyen.se114project.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.synguyen.se114project.R;
import com.synguyen.se114project.data.entity.Task;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskVH> {

    private List<Task> items; // Biến lưu danh sách task hiện tại
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
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSubTitle = itemView.findViewById(R.id.tvSubTitle);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvTag = itemView.findViewById(R.id.tvTag);
            imgPriority = itemView.findViewById(R.id.imgPriority);
            tvDate = itemView.findViewById(R.id.tvDate); // Ánh xạ ID mới thêm
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

        // Gán dữ liệu ngày (Đảm bảo class Task đã có getter này)
        holder.tvDate.setText(t.getDate());

        holder.imgPriority.setAlpha(t.getPriority() > 0 ? 1f : 0.4f);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(t);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // Hàm cập nhật danh sách khi lọc
    public void setTasks(List<Task> newTasks) {
        this.items = newTasks; // Đã sửa tên biến khớp với khai báo ở trên
        notifyDataSetChanged();
    }
}
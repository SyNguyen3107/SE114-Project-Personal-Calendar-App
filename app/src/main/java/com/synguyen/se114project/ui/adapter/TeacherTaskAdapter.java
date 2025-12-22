package com.synguyen.se114project.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.synguyen.se114project.R;
import com.synguyen.se114project.data.entity.Task;

import java.util.List;

public class TeacherTaskAdapter extends RecyclerView.Adapter<TeacherTaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private OnTaskClickListener listener; // 1. Khai báo Listener

    // 2. Interface lắng nghe sự kiện
    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    // 3. Cập nhật Constructor để nhận Listener
    public TeacherTaskAdapter(List<Task> taskList, OnTaskClickListener listener) {
        this.taskList = taskList;
        this.listener = listener;
    }

    // Hàm cập nhật dữ liệu
    public void updateData(List<Task> newTasks) {
        this.taskList = newTasks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task_teacher, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return taskList == null ? 0 : taskList.size();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDeadline, tvDesc;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvDeadline = itemView.findViewById(R.id.tvTaskDeadline);
            // tvDesc = itemView.findViewById(R.id.tvTaskDesc); // Nếu layout có

            // 4. Bắt sự kiện Click vào cả Item
            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onTaskClick(taskList.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Task task) {
            tvTitle.setText(task.getTitle());
            tvDeadline.setText("Deadline: " + task.getTime());
        }
    }
}
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
    private final OnTaskClickListener listener;

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    public TeacherTaskAdapter(List<Task> taskList, OnTaskClickListener listener) {
        this.taskList = taskList;
        this.listener = listener;
    }

    public void updateData(List<Task> newTasks) {
        this.taskList = newTasks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Đảm bảo file item_task_teacher.xml đã tồn tại
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
        // Khai báo đúng các view trong layout item_task_teacher
        TextView tvTitle, tvDate, tvSubtitle, tvStatus;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ đúng ID trong XML
            tvTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvDate = itemView.findViewById(R.id.tvTaskDate);       // SỬA: tvTaskDate thay vì tvTaskDeadline
            tvSubtitle = itemView.findViewById(R.id.tvTaskSubtitle); // SỬA: tvTaskSubtitle thay vì tvDesc
            tvStatus = itemView.findViewById(R.id.tvTaskStatus);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onTaskClick(taskList.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Task task) {
            tvTitle.setText(task.getTitle());
            tvSubtitle.setText(task.getSubTitle() != null ? task.getSubTitle() : "");

            // Hiển thị Deadline (Ưu tiên hiển thị time dạng chuỗi bạn nhập)
            String timeText = task.getTime() != null ? task.getTime() : "";
            tvDate.setText(timeText);
        }
    }
}
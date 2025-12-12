package com.synguyen.se114project.ui.adapter; // Lưu ý package

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.JsonObject;
import com.synguyen.se114project.R;

import java.util.List;

public class TeacherTaskAdapter extends RecyclerView.Adapter<TeacherTaskAdapter.TaskViewHolder> {

    private List<JsonObject> taskList;

    public TeacherTaskAdapter(List<JsonObject> taskList) {
        this.taskList = taskList;
    }

    public void updateData(List<JsonObject> newList) {
        this.taskList = newList;
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
        JsonObject task = taskList.get(position);

        // Kiểm tra null an toàn tránh crash
        String title = task.has("title") && !task.get("title").isJsonNull()
                ? task.get("title").getAsString() : "Không tiêu đề";
        String time = task.has("time") && !task.get("time").isJsonNull()
                ? task.get("time").getAsString() : "---";

        holder.tvTitle.setText(title);
        holder.tvDeadline.setText("Hạn: " + time);
    }

    @Override
    public int getItemCount() {
        return taskList != null ? taskList.size() : 0;
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDeadline;
        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvDeadline = itemView.findViewById(R.id.tvTaskDeadline);
        }
    }
}
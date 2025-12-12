package com.synguyen.se114project.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.JsonObject;
import com.synguyen.se114project.R;

import java.util.List;

public class TeacherCourseAdapter extends RecyclerView.Adapter<TeacherCourseAdapter.CourseViewHolder> {

    private List<JsonObject> courseList;

    // Interface để xử lý sự kiện khi bấm vào item
    public interface OnItemClickListener {
        void onItemClick(JsonObject course);
    }
    private final OnItemClickListener listener;

    public TeacherCourseAdapter(List<JsonObject> courseList, OnItemClickListener listener) {
        this.courseList = courseList;
        this.listener = listener;
    }

    public void updateData(List<JsonObject> newList) {
        this.courseList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course_teacher, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        JsonObject course = courseList.get(position);

        // Lấy dữ liệu an toàn từ JSON
        String name = course.has("name") && !course.get("name").isJsonNull()
                ? course.get("name").getAsString() : "No Name";
        String description = course.has("description") && !course.get("description").isJsonNull()
                ? course.get("description").getAsString() : "";

        holder.tvName.setText(name);
        holder.tvCode.setText(description);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(course));
    }

    @Override
    public int getItemCount() {
        return courseList != null ? courseList.size() : 0;
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCode;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCourseName);
            tvCode = itemView.findViewById(R.id.tvCourseCode);
        }
    }
}
package com.synguyen.se114project.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.synguyen.se114project.R;
import com.synguyen.se114project.data.entity.Course;

import java.util.List;

public class TeacherCourseAdapter extends RecyclerView.Adapter<TeacherCourseAdapter.CourseViewHolder> {

    private List<Course> courseList;

    // Interface để xử lý sự kiện khi bấm vào item
    public interface OnItemClickListener {
        void onItemClick(Course course);
    }
    private final OnItemClickListener listener;

    public TeacherCourseAdapter(List<Course> courseList, OnItemClickListener listener) {
        this.courseList = courseList;
        this.listener = listener;
    }

    public void updateData(List<Course> newList) {
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
        Course course = courseList.get(position);
        String name = course.getName() != null ? course.getName() : "No Name";
        String description = course.getDescription() != null ? course.getDescription() : "";

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
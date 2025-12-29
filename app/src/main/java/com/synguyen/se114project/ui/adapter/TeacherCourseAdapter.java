package com.synguyen.se114project.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.Objects;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.synguyen.se114project.R;
import com.synguyen.se114project.data.entity.Course;

public class TeacherCourseAdapter extends ListAdapter<Course, TeacherCourseAdapter.TeacherCourseViewHolder> {

    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Course course);
    }

    public TeacherCourseAdapter(OnItemClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Course> DIFF_CALLBACK = new DiffUtil.ItemCallback<Course>() {
        @Override
        public boolean areItemsTheSame(@NonNull Course oldItem, @NonNull Course newItem) {
            return Objects.equals(oldItem.getId(), newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Course oldItem, @NonNull Course newItem) {
            return Objects.equals(oldItem.getName(), newItem.getName()) &&
                    Objects.equals(oldItem.getDescription(), newItem.getDescription()) &&
                    oldItem.getStudentCount() == newItem.getStudentCount();
        }
    };

    @NonNull
    @Override
    public TeacherCourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course_teacher, parent, false);
        return new TeacherCourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeacherCourseViewHolder holder, int position) {
        Course course = getItem(position);
        holder.bind(course, listener);
    }

    static class TeacherCourseViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDesc, tvTime, tvStudentCount;

        public TeacherCourseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_course_name);
            tvDesc = itemView.findViewById(R.id.tv_course_desc);
            tvTime = itemView.findViewById(R.id.tv_time_slot);
            tvStudentCount = itemView.findViewById(R.id.tv_student_count);
        }

        public void bind(Course course, OnItemClickListener listener) {
            if (course.getName() != null && !course.getName().isEmpty()) {
                tvName.setText(course.getName());
            } else {
                tvName.setText("Untitled Course");
            }

            // Hiển thị mã môn học
            if (tvDesc != null) {
                tvDesc.setText(course.getDescription() != null ? course.getDescription() : "No ID");
            }

            if (tvTime != null) {
                tvTime.setText(course.getTimeSlot() != null ? course.getTimeSlot() : "TBA");
            }

            if (tvStudentCount != null) {
                tvStudentCount.setText(course.getStudentCount() + " students");
            }

            // LOẠI BỎ VIỆC GHI ĐÈ MÀU TỪ DATABASE
            // Giao diện sẽ luôn dùng màu xanh đậm (holo_blue_dark) đã thiết kế trong XML

            itemView.setOnClickListener(v -> listener.onItemClick(course));
        }
    }
}
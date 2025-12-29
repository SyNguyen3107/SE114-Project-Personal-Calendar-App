package com.synguyen.se114project.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.synguyen.se114project.R;
import com.synguyen.se114project.data.entity.Course;

import java.util.Objects;

public class CourseAdapter extends ListAdapter<Course, CourseAdapter.CourseViewHolder> {

    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Course course);
    }

    public CourseAdapter(OnItemClickListener listener) {
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
                    Objects.equals(oldItem.getTeacherName(), newItem.getTeacherName()) &&
                    Objects.equals(oldItem.getTimeSlot(), newItem.getTimeSlot()) &&
                    Objects.equals(oldItem.getColorHex(), newItem.getColorHex());
        }
    };

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = getItem(position);
        if (course != null) {
            holder.bind(course, listener);
        }
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvDesc;
        private final Chip chipTime; // Updated to Chip
        private final TextView tvTeacher;
        private final MaterialCardView cardContainer;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_course_name);
            tvDesc = itemView.findViewById(R.id.tv_course_description);
            chipTime = itemView.findViewById(R.id.chip_time); // Match ID in item_course.xml
            tvTeacher = itemView.findViewById(R.id.tv_teacher_name);
            cardContainer = itemView.findViewById(R.id.card_course_container);
        }

        public void bind(Course course, OnItemClickListener listener) {
            tvName.setText(course.getName());

            String desc = course.getDescription() != null ? course.getDescription() : "No description available";
            tvDesc.setText(desc);

            // Set text to the Chip
            String time = course.getTimeSlot() != null ? course.getTimeSlot() : "TBA";
            if (chipTime != null) chipTime.setText(time);

            String teacher = course.getTeacherName() != null ? course.getTeacherName() : "Unknown Instructor";
            tvTeacher.setText(teacher);

            // Handle color background logic
            try {
                if (course.getColorHex() != null && !course.getColorHex().isEmpty()) {
                    // For modern look, we might keep background white and use color for accent
                    // cardContainer.setCardBackgroundColor(Color.parseColor(course.getColorHex()));
                }
            } catch (Exception ignored) {}

            itemView.setOnClickListener(v -> listener.onItemClick(course));
        }
    }
}
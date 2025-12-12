package com.synguyen.se114project.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.synguyen.se114project.R;
import com.synguyen.se114project.data.entity.Course;

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
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Course oldItem, @NonNull Course newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getDescription().equals(newItem.getDescription()) &&
                    oldItem.getColorHex().equals(newItem.getColorHex());
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
        holder.bind(course, listener);
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDesc, tvTime, tvTeacher;
        ConstraintLayout layoutBg;
        ImageView imgIcon;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_course_name);
            tvDesc = itemView.findViewById(R.id.tv_course_desc);
            tvTime = itemView.findViewById(R.id.tv_time_slot);
            tvTeacher = itemView.findViewById(R.id.tv_teacher_name);
            layoutBg = itemView.findViewById(R.id.layout_course_bg);
            imgIcon = itemView.findViewById(R.id.img_course_icon);
        }

        public void bind(Course course, OnItemClickListener listener) {
            tvName.setText(course.getName());

            // Format mô tả: "Ngày • Mô tả"
            String descText = (course.getDateInfo() != null ? course.getDateInfo() : "") +
                    " • " +
                    (course.getDescription() != null ? course.getDescription() : "");
            tvDesc.setText(descText);

            tvTime.setText(course.getTimeSlot());
            tvTeacher.setText(course.getTeacherName());

            // Đổi màu nền Card theo mã màu trong DB
            try {
                if (course.getColorHex() != null && !course.getColorHex().isEmpty()) {
                    layoutBg.setBackgroundColor(Color.parseColor(course.getColorHex()));
                }
            } catch (IllegalArgumentException e) {
                // Màu lỗi thì dùng mặc định (Xanh)
                layoutBg.setBackgroundColor(Color.parseColor("#2196F3"));
            }

            // TODO: Nếu sau này có icon riêng cho từng môn, load ảnh tại đây
            // imgIcon.setImageResource(...);

            itemView.setOnClickListener(v -> listener.onItemClick(course));
        }
    }
}
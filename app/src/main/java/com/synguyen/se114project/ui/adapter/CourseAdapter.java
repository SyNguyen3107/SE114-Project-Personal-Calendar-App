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
                    Objects.equals(oldItem.getTeacherName(), newItem.getTeacherName()) && // Đã thêm ở bước trước
                    Objects.equals(oldItem.getTimeSlot(), newItem.getTimeSlot()) &&       // Đã thêm ở bước trước
                    Objects.equals(oldItem.getDateInfo(), newItem.getDateInfo()) &&       // Nên so sánh cả DateInfo
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
        if (course != null) { // Check null cho item
            holder.bind(course, listener);
        }
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

            // Xử lý chuỗi Date/Description an toàn
            String dateInfo = course.getDateInfo() != null ? course.getDateInfo() : "";
            String desc = course.getDescription() != null ? course.getDescription() : "";

            if (!dateInfo.isEmpty() && !desc.isEmpty()) {
                tvDesc.setText(dateInfo + " • " + desc);
            } else {
                tvDesc.setText(dateInfo + desc); // Chỉ hiện cái nào có dữ liệu
            }

            // Xử lý TimeSlot (Nếu null thì hiện "TBA" - To Be Announced)
            tvTime.setText(course.getTimeSlot() != null ? course.getTimeSlot() : "TBA");

            // Xử lý TeacherName (Nếu null thì hiện ID hoặc "Unknown")
            if (course.getTeacherName() != null) {
                tvTeacher.setText(course.getTeacherName());
            } else {
                // Fallback nếu RPC chưa join được tên
                tvTeacher.setText("Giảng viên: --");
            }

            // Xử lý Màu nền (Giữ nguyên logic tốt của bạn)
            try {
                if (course.getColorHex() != null && !course.getColorHex().isEmpty()) {
                    layoutBg.setBackgroundColor(Color.parseColor(course.getColorHex()));
                } else {
                    layoutBg.setBackgroundColor(Color.parseColor("#2196F3")); // Màu default
                }
            } catch (IllegalArgumentException e) {
                layoutBg.setBackgroundColor(Color.parseColor("#2196F3"));
            }

            itemView.setOnClickListener(v -> listener.onItemClick(course));
        }
    }
}
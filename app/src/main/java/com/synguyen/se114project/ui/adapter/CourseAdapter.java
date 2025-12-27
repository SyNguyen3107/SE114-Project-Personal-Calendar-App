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
            // So sánh ID để xác định có phải cùng một item không
            return Objects.equals(oldItem.getId(), newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Course oldItem, @NonNull Course newItem) {
            // So sánh nội dung để biết có cần vẽ lại UI không
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
        // Khai báo View theo ID mới trong item_course.xml
        private final TextView tvName;
        private final TextView tvDesc;
        private final TextView tvTime;
        private final TextView tvTeacher;
        private final MaterialCardView cardContainer; // Dùng CardView để set màu nền

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ ID khớp với file XML item_course.xml vừa tạo
            tvName = itemView.findViewById(R.id.tv_course_name);
            tvDesc = itemView.findViewById(R.id.tv_course_description);
            tvTime = itemView.findViewById(R.id.tv_course_time);
            tvTeacher = itemView.findViewById(R.id.tv_teacher_name);
            cardContainer = itemView.findViewById(R.id.card_course_container);
        }

        public void bind(Course course, OnItemClickListener listener) {
            // 1. Tên môn học
            tvName.setText(course.getName());

            // 2. Mô tả (Nếu null thì hiện chuỗi rỗng)
            String desc = course.getDescription() != null ? course.getDescription() : "No description available";
            tvDesc.setText(desc);

            // 3. Thời gian học (Nếu null thì hiện TBA)
            String time = course.getTimeSlot() != null ? course.getTimeSlot() : "TBA";
            tvTime.setText(time);

            // 4. Tên giáo viên
            String teacher = course.getTeacherName() != null ? course.getTeacherName() : "Unknown Instructor";
            tvTeacher.setText(teacher);

            // 5. Xử lý Màu nền (Hex Color)
            try {
                if (course.getColorHex() != null && !course.getColorHex().isEmpty()) {
                    cardContainer.setCardBackgroundColor(Color.parseColor(course.getColorHex()));
                } else {
                    // Nếu không có màu, gán màu mặc định (Xanh) hoặc Hash theo ID để mỗi course có 1 màu cố định
                    cardContainer.setCardBackgroundColor(Color.parseColor("#1565C0"));
                }
            } catch (IllegalArgumentException e) {
                // Fallback nếu mã màu sai định dạng
                cardContainer.setCardBackgroundColor(Color.parseColor("#1565C0"));
            }

            // 6. Sự kiện click
            itemView.setOnClickListener(v -> listener.onItemClick(course));
        }
    }
}
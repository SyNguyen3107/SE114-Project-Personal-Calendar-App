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
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.synguyen.se114project.R;
import com.synguyen.se114project.data.entity.Course;

// 1. ĐỔI TÊN CLASS VÀ VIEWHOLDER
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
            // Dùng Objects.equals để tránh lỗi nếu ID bị null
            return Objects.equals(oldItem.getId(), newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Course oldItem, @NonNull Course newItem) {
            // SỬA LỖI: Dùng Objects.equals thay vì .equals() trực tiếp
            // Nó hoạt động như sau: Nếu cả 2 null -> true. Nếu 1 cái null -> false. Nếu cả 2 có giá trị -> so sánh giá trị.
            return Objects.equals(oldItem.getName(), newItem.getName()) &&
                    Objects.equals(oldItem.getDescription(), newItem.getDescription()) &&
                    oldItem.getStudentCount() == newItem.getStudentCount() &&
                    Objects.equals(oldItem.getColorHex(), newItem.getColorHex());
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

    // 3. LOGIC RIÊNG CHO TEACHER
    static class TeacherCourseViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDesc, tvTime, tvStudentCount;
        ConstraintLayout layoutBg;

        public TeacherCourseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_course_name);
            tvDesc = itemView.findViewById(R.id.tv_course_desc);
            tvTime = itemView.findViewById(R.id.tv_time_slot);
            layoutBg = itemView.findViewById(R.id.layout_course_bg);
            tvStudentCount = itemView.findViewById(R.id.tv_student_count);
        }

        public void bind(Course course, OnItemClickListener listener) {
            if (course.getName() != null && !course.getName().isEmpty()) {
                tvName.setText(course.getName());
            } else {
                tvName.setText("Môn học không tên");
            }

            String descText = (course.getDateInfo() != null ? course.getDateInfo() : "") +
                    " • " + (course.getDescription() != null ? course.getDescription() : "");
            if (tvDesc != null) tvDesc.setText(descText);

            if (tvTime != null) tvTime.setText(course.getTimeSlot());

            // Gán số lượng sinh viên (Đây là chỗ hay gây lỗi nhất)
            if (tvStudentCount != null) {
                int count = course.getStudentCount();
                tvStudentCount.setText(count + " sinh viên");
            }

            // Màu nền
            try {
                if (course.getColorHex() != null && !course.getColorHex().isEmpty()) {
                    layoutBg.setBackgroundColor(Color.parseColor(course.getColorHex()));
                }
            } catch (IllegalArgumentException e) {
                layoutBg.setBackgroundColor(Color.parseColor("#2196F3"));
            }

            itemView.setOnClickListener(v -> listener.onItemClick(course));
        }
    }
}
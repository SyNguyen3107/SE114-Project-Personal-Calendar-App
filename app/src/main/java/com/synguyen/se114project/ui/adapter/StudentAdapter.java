package com.synguyen.se114project.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.synguyen.se114project.R;
import com.synguyen.se114project.data.entity.Profile;

import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    private List<Profile> studentList;

    public StudentAdapter(List<Profile> studentList) {
        this.studentList = studentList;
    }

    public void updateData(List<Profile> newList) {
        this.studentList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        Profile student = studentList.get(position);
        if (student == null) return;

        // 1. Tên
        String name = student.getFullName();
        holder.tvName.setText((name != null && !name.isEmpty()) ? name : "Chưa cập nhật tên");

        // 2. Email
        holder.tvEmail.setText(student.getEmail() != null ? student.getEmail() : "");

        // 3. User Code (MSSV) - MỚI
        if (student.getUserCode() != null && !student.getUserCode().isEmpty()) {
            holder.tvCode.setText("MSSV: " + student.getUserCode());
            holder.tvCode.setVisibility(View.VISIBLE);
        } else {
            holder.tvCode.setVisibility(View.GONE);
        }

        // 4. Avatar chữ cái đầu (Cho đẹp đội hình) - MỚI
        if (name != null && !name.isEmpty()) {
            String firstChar = String.valueOf(name.charAt(0)).toUpperCase();
            holder.tvAvatar.setText(firstChar);
        } else {
            holder.tvAvatar.setText("?");
        }
    }

    @Override
    public int getItemCount() {
        return studentList != null ? studentList.size() : 0;
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvCode, tvAvatar; // Thêm tvCode, tvAvatar

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ đúng ID trong XML item_student.xml
            tvName = itemView.findViewById(R.id.tvStudentName);
            tvEmail = itemView.findViewById(R.id.tvStudentEmail);
            tvCode = itemView.findViewById(R.id.tvStudentCode); // Mới
            tvAvatar = itemView.findViewById(R.id.tvAvatarChar); // Mới
        }
    }
}
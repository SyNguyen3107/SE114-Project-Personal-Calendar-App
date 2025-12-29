package com.synguyen.se114project.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.synguyen.se114project.R;
import com.synguyen.se114project.data.entity.Profile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    private List<Profile> originalList;
    private List<Profile> studentList;

    public StudentAdapter(List<Profile> studentList) {
        this.originalList = (studentList != null) ? studentList : new ArrayList<>();
        this.studentList = new ArrayList<>(this.originalList);
    }

    public void updateData(List<Profile> newList) {
        this.originalList = (newList != null) ? newList : new ArrayList<>();
        this.studentList = new ArrayList<>(this.originalList);
        notifyDataSetChanged();
    }

    public void searchStudents(String query) {
        if (query == null || query.trim().isEmpty()) {
            studentList = new ArrayList<>(originalList);
        } else {
            List<Profile> filteredList = new ArrayList<>();
            String lowerCaseQuery = query.toLowerCase(Locale.getDefault());

            for (Profile student : originalList) {
                String name = (student.getFullName() != null) ? student.getFullName().toLowerCase() : "";
                String mssv = (student.getUserCode() != null) ? student.getUserCode().toLowerCase() : "";
                String email = (student.getEmail() != null) ? student.getEmail().toLowerCase() : "";

                if (name.contains(lowerCaseQuery) || mssv.contains(lowerCaseQuery) || email.contains(lowerCaseQuery)) {
                    filteredList.add(student);
                }
            }
            studentList = filteredList;
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng item_student.xml mới đã được thiết kế lại
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        Profile student = studentList.get(position);
        if (student == null) return;

        // 1. Tên sinh viên
        String name = student.getFullName();
        holder.tvName.setText((name != null && !name.isEmpty()) ? name : "No Name");

        // 2. Email
        holder.tvEmail.setText(student.getEmail() != null ? student.getEmail() : "No Email");

        // 3. MSSV / User Code
        if (student.getUserCode() != null && !student.getUserCode().isEmpty()) {
            holder.tvCode.setText("MSSV: " + student.getUserCode());
        } else {
            holder.tvCode.setText("MSSV: N/A");
        }

        // 4. Avatar (Chữ cái đầu)
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
        TextView tvName, tvEmail, tvCode, tvAvatar;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvStudentName);
            tvEmail = itemView.findViewById(R.id.tvStudentEmail);
            tvCode = itemView.findViewById(R.id.tvStudentCode);
            tvAvatar = itemView.findViewById(R.id.tvAvatarChar);
        }
    }
}
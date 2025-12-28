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

    private List<Profile> originalList; // Giữ data gốc để backup khi search
    private List<Profile> studentList;  // Data đang hiển thị

    public StudentAdapter(List<Profile> studentList) {
        this.originalList = (studentList != null) ? studentList : new ArrayList<>();
        this.studentList = new ArrayList<>(this.originalList); // Copy data sang list hiển thị
    }

    /**
     * Cập nhật data mới từ API/Database
     */
    public void updateData(List<Profile> newList) {
        this.originalList = (newList != null) ? newList : new ArrayList<>();
        this.studentList = new ArrayList<>(this.originalList); // Reset lại list hiển thị
        notifyDataSetChanged();
    }

    /**
     * Hàm tìm kiếm (Lọc danh sách)
     * Gọi hàm này từ SearchView trong Activity/Fragment
     * @param query: Chuỗi từ khóa người dùng nhập
     */
    public void searchStudents(String query) {
        if (query == null || query.trim().isEmpty()) {
            // Nếu ô tìm kiếm rỗng, hiển thị lại toàn bộ danh sách gốc
            studentList = new ArrayList<>(originalList);
        } else {
            List<Profile> filteredList = new ArrayList<>();
            String lowerCaseQuery = query.toLowerCase(Locale.getDefault());

            for (Profile student : originalList) {
                // Kiểm tra null trước khi so sánh
                String name = (student.getFullName() != null) ? student.getFullName().toLowerCase() : "";
                String mssv = (student.getUserCode() != null) ? student.getUserCode().toLowerCase() : "";
                String email = (student.getEmail() != null) ? student.getEmail().toLowerCase() : "";

                // Logic tìm kiếm: Tìm theo Tên HOẶC MSSV HOẶC Email
                if (name.contains(lowerCaseQuery) || mssv.contains(lowerCaseQuery) || email.contains(lowerCaseQuery)) {
                    filteredList.add(student);
                }
            }
            studentList = filteredList;
        }
        notifyDataSetChanged(); // Cập nhật lại giao diện
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

        // 3. User Code (MSSV)
        if (student.getUserCode() != null && !student.getUserCode().isEmpty()) {
            holder.tvCode.setText("MSSV: " + student.getUserCode());
            holder.tvCode.setVisibility(View.VISIBLE);
        } else {
            holder.tvCode.setVisibility(View.GONE);
        }

        // 4. Avatar chữ cái đầu
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
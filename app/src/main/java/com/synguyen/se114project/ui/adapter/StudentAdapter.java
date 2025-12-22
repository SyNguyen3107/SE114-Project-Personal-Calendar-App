package com.synguyen.se114project.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.synguyen.se114project.R;
import com.synguyen.se114project.data.entity.Profile; // Import đúng Entity Profile

import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    // 1. Thay đổi từ List<JsonObject> sang List<Profile>
    private List<Profile> studentList;

    public StudentAdapter(List<Profile> studentList) {
        this.studentList = studentList;
    }

    // 2. Cập nhật hàm updateData nhận List<Profile>
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

        // 3. Sử dụng Getter của đối tượng Profile thay vì parse JSON
        // Lưu ý: Đảm bảo class Profile của bạn đã có các hàm getter này (hoặc truy cập field public)
        String name = student.getFullName();
        String email = student.getEmail();

        // Fallback nếu null
        if (name == null || name.isEmpty()) {
            name = "Sinh viên";
        }

        holder.tvName.setText(name);
        holder.tvEmail.setText(email != null ? email : "");
    }

    @Override
    public int getItemCount() {
        return studentList != null ? studentList.size() : 0;
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvStudentName);
            tvEmail = itemView.findViewById(R.id.tvStudentEmail);
        }
    }
}
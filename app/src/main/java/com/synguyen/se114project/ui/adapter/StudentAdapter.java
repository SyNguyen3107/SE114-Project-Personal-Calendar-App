package com.synguyen.se114project.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.JsonObject;
import com.synguyen.se114project.R;
import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    private List<JsonObject> studentList;

    public StudentAdapter(List<JsonObject> studentList) {
        this.studentList = studentList;
    }

    public void updateData(List<JsonObject> newList) {
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
        JsonObject item = studentList.get(position);

        // Supabase trả về dạng: { "user_id": "...", "profiles": { "full_name": "...", "email": "..." } }
        if (item.has("profiles") && !item.get("profiles").isJsonNull()) {
            JsonObject profile = item.get("profiles").getAsJsonObject();

            String name = profile.has("full_name") && !profile.get("full_name").isJsonNull()
                    ? profile.get("full_name").getAsString() : "Sinh viên";
            String email = profile.has("email") && !profile.get("email").isJsonNull()
                    ? profile.get("email").getAsString() : "";

            holder.tvName.setText(name);
            holder.tvEmail.setText(email);
        }
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
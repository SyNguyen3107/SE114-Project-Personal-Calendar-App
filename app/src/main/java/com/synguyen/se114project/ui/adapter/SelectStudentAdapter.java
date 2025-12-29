package com.synguyen.se114project.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.synguyen.se114project.R;
import com.synguyen.se114project.data.entity.Profile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class SelectStudentAdapter extends RecyclerView.Adapter<SelectStudentAdapter.VH> {

    private List<Profile> originalList = new ArrayList<>();
    private List<Profile> displayList = new ArrayList<>();
    private final Set<String> selectedIds = new HashSet<>();

    public void updateData(List<Profile> newList) {
        this.originalList = newList != null ? newList : new ArrayList<>();
        this.displayList = new ArrayList<>(originalList);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        String lowerQuery = query.toLowerCase(Locale.getDefault());
        displayList = new ArrayList<>();
        for (Profile p : originalList) {
            if ((p.getFullName() != null && p.getFullName().toLowerCase().contains(lowerQuery)) ||
                (p.getUserCode() != null && p.getUserCode().toLowerCase().contains(lowerQuery))) {
                displayList.add(p);
            }
        }
        notifyDataSetChanged();
    }

    public List<String> getSelectedUserIds() {
        return new ArrayList<>(selectedIds);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_select_student, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Profile p = displayList.get(position);
        holder.tvName.setText(p.getFullName());
        holder.tvCode.setText(p.getUserCode());
        
        holder.cb.setOnCheckedChangeListener(null);
        holder.cb.setChecked(selectedIds.contains(p.getId()));
        
        holder.cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) selectedIds.add(p.getId());
            else selectedIds.remove(p.getId());
        });

        holder.itemView.setOnClickListener(v -> holder.cb.toggle());
    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        CheckBox cb;
        TextView tvName, tvCode;
        VH(View itemView) {
            super(itemView);
            cb = itemView.findViewById(R.id.cbSelectStudent);
            tvName = itemView.findViewById(R.id.tvSelectName);
            tvCode = itemView.findViewById(R.id.tvSelectCode);
        }
    }
}
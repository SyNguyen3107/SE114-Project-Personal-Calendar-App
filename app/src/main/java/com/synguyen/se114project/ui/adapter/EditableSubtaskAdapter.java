package com.synguyen.se114project.ui.adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.synguyen.se114project.R;
import com.synguyen.se114project.data.entity.Subtask;

import java.util.ArrayList;
import java.util.List;

public class EditableSubtaskAdapter extends RecyclerView.Adapter<EditableSubtaskAdapter.ViewHolder> {

    private List<Subtask> subtaskList = new ArrayList<>();
    private final OnSubtaskActionListener listener;

    public interface OnSubtaskActionListener {
        void onDelete(Subtask subtask);
        void onCheck(Subtask subtask, boolean isChecked);
        // Không cần onEdit vì sửa trực tiếp
    }

    public EditableSubtaskAdapter(List<Subtask> list, OnSubtaskActionListener listener) {
        this.subtaskList = list;
        this.listener = listener;
    }

    public void updateList(List<Subtask> newList) {
        this.subtaskList = newList;
        notifyDataSetChanged();
    }

    public List<Subtask> getCurrentList() {
        return subtaskList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subtask_editable, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Quan trọng: Phải remove TextWatcher cũ trước khi bind data mới
        // để tránh loop vô hạn hoặc update nhầm item khi scroll
        if (holder.textWatcher != null) {
            holder.edtTitle.removeTextChangedListener(holder.textWatcher);
        }

        Subtask subtask = subtaskList.get(position);

        holder.edtTitle.setText(subtask.getTitle());
        holder.checkBox.setChecked(subtask.isCompleted());

        // Gán lại TextWatcher mới cho item này
        holder.textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // CẬP NHẬT TRỰC TIẾP VÀO OBJECT
                subtask.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };
        holder.edtTitle.addTextChangedListener(holder.textWatcher);

        // Sự kiện Checkbox
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            subtask.setCompleted(isChecked);
            if (listener != null) listener.onCheck(subtask, isChecked);
        });

        // Sự kiện Xóa
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(subtask);
        });
    }

    @Override
    public int getItemCount() {
        return subtaskList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        EditText edtTitle;
        ImageView btnDelete;
        TextWatcher textWatcher; // Lưu tham chiếu để remove sau này

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.cbSubtask);
            edtTitle = itemView.findViewById(R.id.edtSubtaskTitle);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
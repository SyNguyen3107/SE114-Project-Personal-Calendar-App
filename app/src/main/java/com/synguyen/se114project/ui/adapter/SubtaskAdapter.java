package com.synguyen.se114project.ui.adapter;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.synguyen.se114project.R;
import com.synguyen.se114project.data.entity.Subtask;

import java.util.List;

public class SubtaskAdapter extends RecyclerView.Adapter<SubtaskAdapter.SubtaskViewHolder> {

    private List<Subtask> subtasks;
    private final OnSubtaskCheckListener listener;

    public interface OnSubtaskCheckListener {
        void onCheck(Subtask subtask, boolean isChecked);
    }

    public SubtaskAdapter(List<Subtask> subtasks, OnSubtaskCheckListener listener) {
        this.subtasks = subtasks;
        this.listener = listener;
    }

    public void setSubtasks(List<Subtask> newSubtasks) {
        this.subtasks = newSubtasks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SubtaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subtask, parent, false);
        return new SubtaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubtaskViewHolder holder, int position) {
        Subtask subtask = subtasks.get(position);
        holder.bind(subtask);
    }

    @Override
    public int getItemCount() {
        return subtasks != null ? subtasks.size() : 0;
    }

    class SubtaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbSubtask;
        TextView tvTitle;

        public SubtaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cbSubtask = itemView.findViewById(R.id.cbSubtask);
            tvTitle = itemView.findViewById(R.id.tvSubtaskTitle);
        }

        void bind(Subtask subtask) {
            tvTitle.setText(subtask.title);

            // Xóa listener cũ để tránh lỗi loop khi setChecked
            cbSubtask.setOnCheckedChangeListener(null);
            cbSubtask.setChecked(subtask.isCompleted);

            // Gạch ngang chữ nếu đã hoàn thành
            updateStrikeThrough(subtask.isCompleted);

            // Gán listener mới
            cbSubtask.setOnCheckedChangeListener((buttonView, isChecked) -> {
                subtask.isCompleted = isChecked;
                updateStrikeThrough(isChecked);
                listener.onCheck(subtask, isChecked);
            });
        }

        private void updateStrikeThrough(boolean isChecked) {
            if (isChecked) {
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvTitle.setTextColor(0xFFAAAAAA); // Màu xám
            } else {
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                tvTitle.setTextColor(0xFF000000); // Màu đen
            }
        }
    }
}
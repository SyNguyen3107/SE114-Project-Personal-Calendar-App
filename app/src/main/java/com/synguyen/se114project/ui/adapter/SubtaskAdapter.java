package com.synguyen.se114project.ui.adapter;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.synguyen.se114project.R;
import com.synguyen.se114project.data.entity.Subtask;

public class SubtaskAdapter extends ListAdapter<Subtask, SubtaskAdapter.SubtaskViewHolder> {

    private OnSubtaskActionListener listener;

    // Interface mở rộng để hỗ trợ cả Check và Delete (nếu sau này cần)
    public interface OnSubtaskActionListener {
        void onCheck(Subtask subtask, boolean isChecked);
        void onDelete(Subtask subtask); // Để dành cho nút xóa
    }

    public SubtaskAdapter() {
        super(DIFF_CALLBACK);
    }

    // Phương thức set listener mới
    public void setOnSubtaskClickListener(OnSubtaskActionListener listener) {
        this.listener = listener;
    }

    // Hỗ trợ constructor cũ để code cũ không lỗi (nhưng khuyên dùng setOnSubtaskClickListener)
    public SubtaskAdapter(java.util.List<Subtask> list, OnSubtaskActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
        submitList(list);
    }

    private static final DiffUtil.ItemCallback<Subtask> DIFF_CALLBACK = new DiffUtil.ItemCallback<Subtask>() {
        @Override
        public boolean areItemsTheSame(@NonNull Subtask oldItem, @NonNull Subtask newItem) {
            // So sánh UUID
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Subtask oldItem, @NonNull Subtask newItem) {
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                    oldItem.isCompleted() == newItem.isCompleted();
        }
    };

    @NonNull
    @Override
    public SubtaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subtask, parent, false);
        return new SubtaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubtaskViewHolder holder, int position) {
        Subtask subtask = getItem(position);
        holder.bind(subtask);
    }

    class SubtaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbSubtask;
        TextView tvTitle;
        // ImageView btnDelete; // Nếu layout có nút xóa thì uncomment dòng này

        public SubtaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cbSubtask = itemView.findViewById(R.id.cbSubtask);
            tvTitle = itemView.findViewById(R.id.tvSubtaskTitle);
            // btnDelete = itemView.findViewById(R.id.btnDeleteSubtask); // ID nút xóa
        }

        void bind(Subtask subtask) {
            tvTitle.setText(subtask.getTitle()); // Getter Title

            // Xóa listener cũ
            cbSubtask.setOnCheckedChangeListener(null);
            cbSubtask.setChecked(subtask.isCompleted()); // Getter isCompleted

            // Gạch ngang chữ
            updateStrikeThrough(subtask.isCompleted());

            // Gán listener mới
            cbSubtask.setOnCheckedChangeListener((buttonView, isChecked) -> {
                subtask.setCompleted(isChecked); // Setter
                updateStrikeThrough(isChecked);
                if (listener != null) {
                    listener.onCheck(subtask, isChecked);
                }
            });

            // Logic nút xóa (Nếu có)
            /*
            if (btnDelete != null) {
                btnDelete.setOnClickListener(v -> {
                    if (listener != null) listener.onDelete(subtask);
                });
            }
            */
        }

        private void updateStrikeThrough(boolean isChecked) {
            if (isChecked) {
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvTitle.setTextColor(0xFFAAAAAA);
            } else {
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                tvTitle.setTextColor(0xFF000000);
            }
        }
    }
}
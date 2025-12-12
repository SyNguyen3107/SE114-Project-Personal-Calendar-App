package com.synguyen.se114project.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.synguyen.se114project.R;
import com.synguyen.se114project.data.entity.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ActiveTimersAdapter extends RecyclerView.Adapter<ActiveTimersAdapter.TimerViewHolder> {

    private List<Task> activeTasks = new ArrayList<>();
    private Map<String, Long> timeRemainingMap = new HashMap<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Task task);
    }

    public ActiveTimersAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Task> tasks) {
        this.activeTasks = tasks;
        notifyDataSetChanged();
    }

    public void updateTimeMap(Map<String, Long> map) {
        this.timeRemainingMap = map;
        // Tối ưu: Chỉ notify những item thay đổi thay vì toàn bộ (để tránh nháy)
        // Nhưng để đơn giản ban đầu dùng notifyDataSetChanged
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TimerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Tái sử dụng layout card_active_task nhưng cần tách ra thành layout riêng
        // Giả sử ta tạo file item_active_timer.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_active_timer, parent, false);
        return new TimerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimerViewHolder holder, int position) {
        Task task = activeTasks.get(position);
        Long millis = timeRemainingMap.get(task.getId());
        holder.bind(task, millis != null ? millis : 0, listener);
    }

    @Override
    public int getItemCount() {
        return activeTasks.size();
    }

    static class TimerViewHolder extends RecyclerView.ViewHolder {
        TextView tvTaskName, tvTimeLeft;
        ProgressBar pbTimer;

        public TimerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTaskName = itemView.findViewById(R.id.tv_active_task_name);
            tvTimeLeft = itemView.findViewById(R.id.tv_active_timer);
            pbTimer = itemView.findViewById(R.id.pb_active_timer);
        }

        public void bind(Task task, long millisLeft, OnItemClickListener listener) {
            tvTaskName.setText(task.getTitle());

            long minutes = (millisLeft / 1000) / 60;
            tvTimeLeft.setText(String.format(Locale.getDefault(), "%d min left", minutes));

            // Max progress dựa trên duration gốc
            long maxDuration = task.getDuration() > 0 ? task.getDuration() : 25 * 60 * 1000;
            pbTimer.setMax((int) (maxDuration / 1000));
            pbTimer.setProgress((int) (millisLeft / 1000));

            itemView.setOnClickListener(v -> listener.onItemClick(task));
        }
    }
}
package com.synguyen.se114project.ui.home;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.synguyen.se114project.R;
import com.synguyen.se114project.ui.adapter.TaskAdapter;
import com.synguyen.se114project.viewmodel.MainViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private MainViewModel mainViewModel;
    private RecyclerView rvTasks;
    private LinearLayout llDatesContainer;
    private TextView btnAdd;
    private TextView tvHeaderTitle;
    private LinearLayout layoutEmptyState; // View hiển thị khi không có task

    private TaskAdapter adapter;

    // Biến lưu timestamp của ngày đang được chọn (để so sánh và highlight UI)
    private long selectedDateTimestamp = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ View
        rvTasks = view.findViewById(R.id.rvTasks);
        llDatesContainer = view.findViewById(R.id.llDatesContainer);
        btnAdd = view.findViewById(R.id.btnAdd);
        tvHeaderTitle = view.findViewById(R.id.tvHeaderTitle);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);

        // 2. Setup RecyclerView
        rvTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TaskAdapter(new ArrayList<>(), task -> {
            // Xử lý khi click vào item task -> Chuyển sang màn hình chi tiết
            Bundle bundle = new Bundle();
            bundle.putLong("taskId", task.getId());

            NavController navController = Navigation.findNavController(view);
            try {
                navController.navigate(R.id.action_homeFragment_to_taskDetailFragment, bundle);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Lỗi điều hướng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        rvTasks.setAdapter(adapter);

        // 3. Khởi tạo ViewModel
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        // 4. Quan sát dữ liệu đã được lọc theo ngày từ ViewModel
        mainViewModel.getTasksBySelectedDate().observe(getViewLifecycleOwner(), tasks -> {
            // Cập nhật Adapter
            adapter.setTasks(tasks);

            // Cập nhật tiêu đề ngày
            updateHeaderTitle(selectedDateTimestamp);

            // --- LOGIC EMPTY STATE ---
            // Nếu danh sách rỗng -> Ẩn RecyclerView, Hiện Empty State
            if (tasks == null || tasks.isEmpty()) {
                rvTasks.setVisibility(View.GONE);
                if (layoutEmptyState != null) {
                    layoutEmptyState.setVisibility(View.VISIBLE);
                }
            } else {
                // Nếu có dữ liệu -> Hiện RecyclerView, Ẩn Empty State
                rvTasks.setVisibility(View.VISIBLE);
                if (layoutEmptyState != null) {
                    layoutEmptyState.setVisibility(View.GONE);
                }
            }
        });

        // 5. Xử lý nút Thêm (+ Add Task)
        btnAdd.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            try {
                navController.navigate(R.id.action_homeFragment_to_addEditFragment);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Chưa cấu hình action Add!", Toast.LENGTH_SHORT).show();
            }
        });

        // 6. Mặc định chọn ngày hôm nay khi mở lần đầu
        if (selectedDateTimestamp == 0) {
            selectedDateTimestamp = System.currentTimeMillis();
            mainViewModel.setSelectedDate(selectedDateTimestamp);
        }

        // 7. Tạo thanh lịch ngang (30 ngày tới)
        populateDates(30);
    }

    private void updateHeaderTitle(long timestamp) {
        if (timestamp == 0) return;
        SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd 'Tasks'", Locale.ENGLISH);
        tvHeaderTitle.setText(outputFormat.format(new Date(timestamp)));
    }

    private void populateDates(int days) {
        llDatesContainer.removeAllViews();
        Calendar cal = Calendar.getInstance();

        SimpleDateFormat sdfDay = new SimpleDateFormat("EEE", Locale.ENGLISH);
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd", Locale.getDefault());
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        // Calendar dùng để so sánh highlight
        Calendar selectedCal = Calendar.getInstance();
        selectedCal.setTimeInMillis(selectedDateTimestamp);

        for (int i = 0; i < days; i++) {
            long currentDateInMillis = cal.getTimeInMillis();
            String dayName = sdfDay.format(cal.getTime());
            String dateNum = sdfDate.format(cal.getTime());

            View itemView = inflater.inflate(R.layout.item_date, llDatesContainer, false);
            TextView tvDay = itemView.findViewById(R.id.tv_day_of_week);
            TextView tvDate = itemView.findViewById(R.id.tv_date);

            tvDay.setText(dayName);
            tvDate.setText(dateNum);

            // Lưu timestamp vào tag để dùng khi click
            itemView.setTag(currentDateInMillis);

            // Highlight nếu là ngày đang chọn
            boolean isSameDay = cal.get(Calendar.YEAR) == selectedCal.get(Calendar.YEAR) &&
                    cal.get(Calendar.DAY_OF_YEAR) == selectedCal.get(Calendar.DAY_OF_YEAR);

            if (isSameDay) updateDateSelectionUI(itemView);
            else resetDateSelectionUI(itemView);

            itemView.setOnClickListener(v -> {
                // Reset màu các item khác
                for (int j = 0; j < llDatesContainer.getChildCount(); j++) {
                    resetDateSelectionUI(llDatesContainer.getChildAt(j));
                }
                // Highlight item vừa chọn
                updateDateSelectionUI(v);

                // Cập nhật biến và gọi ViewModel
                long clickedDate = (long) v.getTag();
                selectedDateTimestamp = clickedDate;
                selectedCal.setTimeInMillis(selectedDateTimestamp);

                mainViewModel.setSelectedDate(clickedDate);
            });

            llDatesContainer.addView(itemView);
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    private void updateDateSelectionUI(View view) {
        view.setBackgroundResource(R.drawable.bg_date_selected);
        TextView tvDay = view.findViewById(R.id.tv_day_of_week);
        TextView tvDate = view.findViewById(R.id.tv_date);
        tvDay.setTextColor(Color.WHITE);
        tvDate.setTextColor(Color.WHITE);
    }

    private void resetDateSelectionUI(View view) {
        view.setBackgroundResource(R.drawable.bg_date_unselected);
        TextView tvDay = view.findViewById(R.id.tv_day_of_week);
        TextView tvDate = view.findViewById(R.id.tv_date);
        tvDay.setTextColor(Color.parseColor("#757575"));
        tvDate.setTextColor(Color.BLACK);
    }
}
package com.synguyen.se114project.ui.student.home;

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
import com.synguyen.se114project.ui.adapter.ActiveTimersAdapter;
import com.synguyen.se114project.ui.adapter.TaskAdapter;
import com.synguyen.se114project.viewmodel.student.HomeViewModel;
import com.synguyen.se114project.viewmodel.student.TimerViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private TimerViewModel timerViewModel;

    private RecyclerView rvTasks, rvActiveTimers;
    private LinearLayout llDatesContainer, layoutEmptyState;
    private TextView btnAdd, tvHeaderTitle;

    private long selectedDateTimestamp = 0;
    private TaskAdapter taskAdapter;
    private ActiveTimersAdapter activeTimersAdapter;

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
        rvActiveTimers = view.findViewById(R.id.rv_active_timers);
        llDatesContainer = view.findViewById(R.id.llDatesContainer);
        btnAdd = view.findViewById(R.id.btnAdd);
        tvHeaderTitle = view.findViewById(R.id.tvHeaderTitle);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);

        // 2. Setup Task Adapter
        rvTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        taskAdapter = new TaskAdapter();

        // --- KHÔI PHỤC: SỰ KIỆN CLICK ITEM TASK ---
        taskAdapter.setOnItemClickListener(task -> {
            Bundle bundle = new Bundle();
            // Đảm bảo taskId được truyền đúng kiểu (String/Long tùy vào Entity của bạn)
            // Nếu getId trả về String thì dùng putString, Long thì putLong
            bundle.putString("taskId", String.valueOf(task.getId()));

            NavController navController = Navigation.findNavController(view);
            try {
                navController.navigate(R.id.action_homeFragment_to_taskDetailFragment, bundle);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Lỗi điều hướng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // --- KHÔI PHỤC: SỰ KIỆN CHECKBOX TASK ---
        taskAdapter.setOnTaskCheckListener((task, isChecked) -> {
            if (homeViewModel != null) {
                homeViewModel.updateTaskStatus(task, isChecked);
            }
        });

        rvTasks.setAdapter(taskAdapter);

        // 3. Setup Timer Adapter
        rvActiveTimers.setLayoutManager(new LinearLayoutManager(requireContext()));
        activeTimersAdapter = new ActiveTimersAdapter(task -> {
            Bundle bundle = new Bundle();
            bundle.putString("taskId", String.valueOf(task.getId()));
            try {
                Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_taskDetailFragment, bundle);
            } catch (Exception e) {}
        });
        rvActiveTimers.setAdapter(activeTimersAdapter);

        // --- KHÔI PHỤC: SỰ KIỆN CLICK NÚT ADD ---
        btnAdd.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            try {
                navController.navigate(R.id.action_homeFragment_to_addEditFragment);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Chưa cấu hình action Add!", Toast.LENGTH_SHORT).show();
            }
        });

        // 4. ViewModel
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        timerViewModel = new ViewModelProvider(requireActivity()).get(TimerViewModel.class);

        // Gọi Sync
        homeViewModel.syncData();

        // --- LOGIC MULTI-TIMER ---
        timerViewModel.getRunningTasks().observe(getViewLifecycleOwner(), tasks -> {
            activeTimersAdapter.submitList(tasks);
            rvActiveTimers.setVisibility(tasks.isEmpty() ? View.GONE : View.VISIBLE);
        });

        timerViewModel.getTasksTimeRemaining().observe(getViewLifecycleOwner(), map -> {
            activeTimersAdapter.updateTimeMap(map);
        });

        // --- Logic Home Cũ ---
        homeViewModel.getTasksBySelectedDate().observe(getViewLifecycleOwner(), tasks -> {
            taskAdapter.submitList(tasks);
            updateHeaderTitle(selectedDateTimestamp);
            if (tasks == null || tasks.isEmpty()) {
                rvTasks.setVisibility(View.GONE);
                if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.VISIBLE);
            } else {
                rvTasks.setVisibility(View.VISIBLE);
                if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
            }
        });

        if (selectedDateTimestamp == 0) {
            selectedDateTimestamp = System.currentTimeMillis();
            homeViewModel.setSelectedDate(selectedDateTimestamp);
        }
        populateDates(30);
    }

    private void updateHeaderTitle(long timestamp) {
        if (timestamp == 0) return;
        SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd 'Tasks'", Locale.ENGLISH);
        if (tvHeaderTitle != null) {
            tvHeaderTitle.setText(outputFormat.format(new Date(timestamp)));
        }
    }

    private void populateDates(int days) {
        if (llDatesContainer == null) return;
        llDatesContainer.removeAllViews();

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        Calendar selectedCal = Calendar.getInstance();
        selectedCal.setTimeInMillis(selectedDateTimestamp);

        for (int i = 0; i < days; i++) {
            long currentDateInMillis = cal.getTimeInMillis();
            String dayName = new SimpleDateFormat("EEE", Locale.ENGLISH).format(cal.getTime());
            String dateNum = new SimpleDateFormat("dd", Locale.getDefault()).format(cal.getTime());

            View itemView = inflater.inflate(R.layout.item_date, llDatesContainer, false);
            TextView tvDay = itemView.findViewById(R.id.tv_day_of_week);
            TextView tvDate = itemView.findViewById(R.id.tv_date);

            tvDay.setText(dayName);
            tvDate.setText(dateNum);
            itemView.setTag(currentDateInMillis);

            boolean isSameDay = cal.get(Calendar.YEAR) == selectedCal.get(Calendar.YEAR) &&
                    cal.get(Calendar.DAY_OF_YEAR) == selectedCal.get(Calendar.DAY_OF_YEAR);

            if (isSameDay) {
                itemView.setBackgroundResource(R.drawable.bg_date_selected);
                tvDay.setTextColor(Color.WHITE);
                tvDate.setTextColor(Color.WHITE);
            } else {
                itemView.setBackgroundResource(R.drawable.bg_date_unselected);
                tvDay.setTextColor(Color.parseColor("#757575"));
                tvDate.setTextColor(Color.BLACK);
            }

            itemView.setOnClickListener(v -> {
                long clickedDate = (long) v.getTag();
                selectedDateTimestamp = clickedDate;
                homeViewModel.setSelectedDate(clickedDate);
                populateDates(30);
            });

            llDatesContainer.addView(itemView);
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        // --- KHÔI PHỤC: NÚT VIEW ALL (CALENDAR) ---
        View viewMoreItem = inflater.inflate(R.layout.item_date, llDatesContainer, false);
        TextView tvDayMore = viewMoreItem.findViewById(R.id.tv_day_of_week);
        TextView tvDateMore = viewMoreItem.findViewById(R.id.tv_date);

        tvDayMore.setText("View");
        tvDateMore.setText("All");

        viewMoreItem.setBackgroundResource(R.drawable.bg_date_unselected);
        tvDayMore.setTextColor(Color.parseColor("#757575"));
        tvDateMore.setTextColor(Color.BLACK);

        viewMoreItem.setOnClickListener(v -> {
            try {
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.scheduleFragment);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Không tìm thấy màn hình Lịch", Toast.LENGTH_SHORT).show();
            }
        });

        llDatesContainer.addView(viewMoreItem);
    }
}
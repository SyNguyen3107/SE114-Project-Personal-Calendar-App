package com.synguyen.se114project.ui.schedule;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
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

import java.util.ArrayList;
import java.util.Calendar;

public class ScheduleFragment extends Fragment {

    private MainViewModel mainViewModel;
    private CalendarView calendarView;
    private RecyclerView rvScheduleTasks;
    private TaskAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_schedule, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        calendarView = view.findViewById(R.id.calendarView);
        rvScheduleTasks = view.findViewById(R.id.rvScheduleTasks);

        // 1. Setup RecyclerView
        rvScheduleTasks.setLayoutManager(new LinearLayoutManager(getContext()));

        // --- XỬ LÝ CLICK ITEM TẠI ĐÂY ---
        adapter = new TaskAdapter(new ArrayList<>(), task -> {
            // Tạo Bundle chứa ID
            Bundle bundle = new Bundle();
            bundle.putLong("taskId", task.getId());

            // Điều hướng sang TaskDetailFragment
            NavController navController = Navigation.findNavController(view);
            try {
                navController.navigate(R.id.action_scheduleFragment_to_taskDetailFragment, bundle);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Lỗi chuyển trang: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        rvScheduleTasks.setAdapter(adapter);

        // 2. ViewModel
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        // 3. Quan sát dữ liệu
        mainViewModel.getTasksBySelectedDate().observe(getViewLifecycleOwner(), tasks -> {
            adapter.setTasks(tasks);
        });

        // 4. Bắt sự kiện chọn ngày trên Lịch
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            // Reset giờ phút giây để lấy đúng mốc 00:00
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            // Gọi ViewModel để lọc dữ liệu
            mainViewModel.setSelectedDate(calendar.getTimeInMillis());
        });
    }
}
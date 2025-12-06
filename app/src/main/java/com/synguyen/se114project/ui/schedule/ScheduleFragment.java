package com.synguyen.se114project.ui.schedule;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.synguyen.se114project.R;
import com.synguyen.se114project.ui.adapter.TaskAdapter;
import com.synguyen.se114project.viewmodel.MainViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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

        adapter = new TaskAdapter(new ArrayList<>(), task -> {
            Bundle bundle = new Bundle();
            bundle.putLong("taskId", task.getId());

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

        // 3. Khi selectedDate thay đổi → load task
        mainViewModel.getTasksBySelectedDate().observe(getViewLifecycleOwner(), tasks -> {
            adapter.setTasks(tasks);
        });

        // 4. Quan sát ALL TASK để hiển thị DOT lên lịch
        mainViewModel.getAllTasks().observe(getViewLifecycleOwner(), allTasks -> {
            List<EventDay> events = new ArrayList<>();

            for (var task : allTasks) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(task.getDate());

                // Gắn icon DOT
                events.add(new EventDay(cal, R.drawable.ic_dot_blue));
            }

            calendarView.setEvents(events);
        });

        // 5. Bắt sự kiện click ngày trên lịch
        calendarView.setOnDayClickListener(eventDay -> {
            Calendar cal = eventDay.getCalendar();

            // reset giờ → mốc 00:00
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);

            long millis = cal.getTimeInMillis();

            // Gọi ViewModel để load task
            mainViewModel.setSelectedDate(millis);

            // Điều hướng sang HomeFragment
            Navigation.findNavController(view).navigate(R.id.homeFragment);
        });
    }
}

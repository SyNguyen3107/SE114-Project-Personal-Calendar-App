package com.synguyen.se114project.ui.student.schedule;

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
import com.synguyen.se114project.viewmodel.student.ScheduleViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ScheduleFragment extends Fragment {

    // 1. Thay MainViewModel bằng ScheduleViewModel
    private ScheduleViewModel mViewModel;

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

        // Ánh xạ View
        calendarView = view.findViewById(R.id.calendarView);
        rvScheduleTasks = view.findViewById(R.id.rvScheduleTasks);

        // 2. Setup RecyclerView với Adapter mới (UUID logic)
        rvScheduleTasks.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TaskAdapter();
        adapter.setOnItemClickListener(task -> {
            Bundle bundle = new Bundle();
            bundle.putString("taskId", task.getId()); // Truyền String UUID

            NavController navController = Navigation.findNavController(view);
            try {
                navController.navigate(R.id.action_scheduleFragment_to_taskDetailFragment, bundle);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Lỗi chuyển trang: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        rvScheduleTasks.setAdapter(adapter);

        // 3. Khởi tạo ScheduleViewModel
        mViewModel = new ViewModelProvider(this).get(ScheduleViewModel.class);

        // 4. Quan sát danh sách task theo ngày chọn -> Cập nhật List dưới lịch
        mViewModel.getTasksBySelectedDate().observe(getViewLifecycleOwner(), tasks -> {
            adapter.submitList(tasks);
        });

        // 5. Quan sát TOÀN BỘ task -> Hiển thị dấu chấm (Dots) trên lịch
        mViewModel.getAllTasks().observe(getViewLifecycleOwner(), allTasks -> {
            List<EventDay> events = new ArrayList<>();

            for (var task : allTasks) {
                // Chỉ đánh dấu các task chưa hoàn thành (Optional)
                if (!task.isCompleted()) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(task.getDate());

                    // Gắn icon DOT (Lưu ý: Icon phải tồn tại trong drawable)
                    events.add(new EventDay(cal, R.drawable.ic_dot_blue));
                }
            }

            calendarView.setEvents(events);
        });

        // 6. Xử lý sự kiện click chọn ngày trên lịch
        calendarView.setOnDayClickListener(eventDay -> {
            Calendar cal = eventDay.getCalendar();

            // Reset giờ về 00:00:00 để khớp với logic query trong DB
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            long millis = cal.getTimeInMillis();

            // Cập nhật ViewModel -> Trigger LiveData tải lại danh sách bên dưới
            mViewModel.setSelectedDate(millis);

            // LƯU Ý: Không navigate về Home nữa, hiển thị trực tiếp tại đây để tiện theo dõi
        });
    }
}
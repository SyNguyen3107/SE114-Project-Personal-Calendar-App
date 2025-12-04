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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.synguyen.se114project.R;
import com.synguyen.se114project.data.entity.Task;
import com.synguyen.se114project.ui.adapter.TaskAdapter;
import com.synguyen.se114project.viewmodel.MainViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    // ... (Các biến giữ nguyên như cũ)
    private MainViewModel mainViewModel;
    private RecyclerView rvTasks;
    private LinearLayout llDatesContainer;
    private TextView btnAdd;
    private TextView tvHeaderTitle;
    private TaskAdapter adapter;
    private List<Task> allTasks = new ArrayList<>();
    private String selectedDateFilter = "";

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

        // 2. Setup RecyclerView
        rvTasks.setLayoutManager(new LinearLayoutManager(requireContext()));

        // --- PHẦN QUAN TRỌNG: XỬ LÝ CLICK ---
        adapter = new TaskAdapter(new ArrayList<>(), task -> {
            // 1. Tạo Bundle để đóng gói dữ liệu
            Bundle bundle = new Bundle();
            bundle.putLong("taskId", task.getId()); // Key "taskId" phải khớp với tên argument trong nav_graph

            // 2. Tìm NavController và điều hướng kèm gói hàng (bundle)
            NavController navController = Navigation.findNavController(view);
            try {
                navController.navigate(R.id.action_homeFragment_to_taskDetailFragment, bundle);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Lỗi chuyển trang: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        rvTasks.setAdapter(adapter);

        // 3. Khởi tạo ViewModel và các phần còn lại (Giữ nguyên)
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        mainViewModel.getAllTasks().observe(getViewLifecycleOwner(), tasks -> {
            allTasks = tasks;
            if (selectedDateFilter.isEmpty()) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                selectedDateFilter = sdf.format(new Date());
            }
            filterTasksByDate(selectedDateFilter);
        });

        btnAdd.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            try {
                navController.navigate(R.id.action_homeFragment_to_addEditFragment);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Chưa cấu hình action Add!", Toast.LENGTH_SHORT).show();
            }
        });

        populateDates(30);
    }

    // ... (Các hàm filterTasksByDate, populateDates... giữ nguyên như cũ)

    private void filterTasksByDate(String dateToFilter) {
        List<Task> filteredList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        for (Task task : allTasks) {
            String taskDateStr = sdf.format(new Date(task.getDate()));
            if (taskDateStr.equals(dateToFilter)) {
                filteredList.add(task);
            }
        }
        adapter.setTasks(filteredList);
        updateHeaderTitle(dateToFilter);
    }

    private void updateHeaderTitle(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd 'Tasks'", Locale.ENGLISH);
            tvHeaderTitle.setText(outputFormat.format(date));
        } catch (Exception e) {
            tvHeaderTitle.setText("Tasks");
        }
    }

    private void populateDates(int days) {
        llDatesContainer.removeAllViews();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdfDay = new SimpleDateFormat("EEE", Locale.ENGLISH);
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd", Locale.getDefault());
        SimpleDateFormat sdfFull = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        for (int i = 0; i < days; i++) {
            String dayName = sdfDay.format(cal.getTime());
            String dateNum = sdfDate.format(cal.getTime());
            String fullDate = sdfFull.format(cal.getTime());

            View itemView = inflater.inflate(R.layout.item_date, llDatesContainer, false);
            TextView tvDay = itemView.findViewById(R.id.tv_day_of_week);
            TextView tvDate = itemView.findViewById(R.id.tv_date);
            tvDay.setText(dayName);
            tvDate.setText(dateNum);
            itemView.setTag(fullDate);

            if (fullDate.equals(selectedDateFilter)) updateDateSelectionUI(itemView);
            else resetDateSelectionUI(itemView);

            itemView.setOnClickListener(v -> {
                for (int j = 0; j < llDatesContainer.getChildCount(); j++) {
                    resetDateSelectionUI(llDatesContainer.getChildAt(j));
                }
                updateDateSelectionUI(v);
                selectedDateFilter = (String) v.getTag();
                filterTasksByDate(selectedDateFilter);
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
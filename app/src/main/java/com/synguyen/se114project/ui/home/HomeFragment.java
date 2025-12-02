package com.synguyen.se114project.ui.home;

import android.graphics.Color;
import android.graphics.Typeface;
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
import com.synguyen.se114project.viewmodel.MainViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private MainViewModel mainViewModel;

    // Các View trong layout mới
    private RecyclerView rvTasks;
    private LinearLayout llDatesContainer;
    private TextView btnAdd;         // Nút thêm mới
    private TextView tvHeaderTitle;  // Tiêu đề ngày tháng (VD: Nov 29 Tasks)

    private TaskAdapter adapter;

    // List chứa dữ liệu gốc từ Database
    private List<Task> allTasks = new ArrayList<>();
    // Ngày đang chọn (lưu dạng String "dd/MM/yyyy" để lọc dữ liệu)
    private String selectedDateFilter = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Nạp layout mới của bạn
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ View theo ID mới trong XML
        rvTasks = view.findViewById(R.id.rvTasks);
        llDatesContainer = view.findViewById(R.id.llDatesContainer);
        btnAdd = view.findViewById(R.id.btnAdd);
        tvHeaderTitle = view.findViewById(R.id.tvHeaderTitle);

        // 2. Setup RecyclerView
        rvTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TaskAdapter(new ArrayList<>(), task -> {
            // Xử lý khi click vào task (VD: mở chi tiết để sửa)
            Toast.makeText(getContext(), "Xem: " + task.getTitle(), Toast.LENGTH_SHORT).show();
            // TODO: Thêm code chuyển trang kèm ID task tại đây
        });
        rvTasks.setAdapter(adapter);

        // 3. Khởi tạo ViewModel
//        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
//
//        // 4. Quan sát dữ liệu từ Database
//        mainViewModel.getAllTasks().observe(getViewLifecycleOwner(), tasks -> {
//            allTasks = tasks;
//
//            // Nếu chưa chọn ngày nào, mặc định chọn Hôm nay
//            if (selectedDateFilter.isEmpty()) {
//                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
//                selectedDateFilter = sdf.format(new Date());
//            }
//
//            // Lọc dữ liệu và cập nhật tiêu đề
//            filterTasksByDate(selectedDateFilter);
//        });
        allTasks = generateSampleTasks();
        // 5. Xử lý nút Thêm (+ Add Task)
        btnAdd.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            // Đảm bảo bạn đã tạo action này trong nav_graph.xml
            // Nếu chưa có Navigation, dùng Toast để test trước
//            try {
//                navController.navigate(R.id.action_homeFragment_to_addEditFragment);
//            } catch (Exception e) {
                Toast.makeText(getContext(), "Chưa cấu hình Navigation!", Toast.LENGTH_SHORT).show();
//            }
        });

        // 6. Tạo thanh lịch ngang (30 ngày tới)
        populateDates(30);
    }

    private List<Task> generateSampleTasks() {
        List<Task> list = new ArrayList<>();

        // Lấy ngày hiện tại để tạo dữ liệu mẫu luôn khớp với lịch
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        //thống nhất lưu ngày dạng Long (Timestamp - mili giây) để dễ sắp xếp và truy vấn.
        long today = cal.getTimeInMillis();

        cal.add(Calendar.DAY_OF_YEAR, 1);
        long tomorrow = cal.getTimeInMillis();

        // Dữ liệu cho HÔM NAY
        list.add(new Task("Prepare project report", "Project Finance", today, "09:00", "In progress", 1));
        list.add(new Task("Code review", "Backend", today, "11:30", "Review", 0));
        list.add(new Task("Team meeting", "Sprint planning", today, "14:00", "Meeting", 0));

        // Dữ liệu cho NGÀY MAI
        list.add(new Task("Write unit tests", "Module X", tomorrow, "16:00", "To do", 2));
        list.add(new Task("Update UI", "Frontend", tomorrow, "10:00", "To do", 1));

        list.add(new Task("Write unit tests", "Module X", tomorrow, "16:00", "To do", 2));
        list.add(new Task("Update UI", "Frontend", tomorrow, "10:00", "To do", 1));

        return list;
    }

    // Hàm lọc danh sách Task theo ngày và cập nhật tiêu đề
    private void filterTasksByDate(String dateToFilter) {
        // 1. Lọc danh sách
        List<Task> filteredList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        for (Task task : allTasks) {
            String taskDateStr = sdf.format(new Date(task.getDate()));
            if (taskDateStr.equals(dateToFilter)) {
                filteredList.add(task);
            }
        }
        adapter.setTasks(filteredList);

        // 2. Cập nhật tiêu đề (Ví dụ: từ "30/11/2025" -> "Nov 30 Tasks")
        updateHeaderTitle(dateToFilter);
    }

    private void updateHeaderTitle(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateString);

            // Định dạng hiển thị: "MMM dd Tasks" (VD: Nov 29 Tasks)
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd 'Tasks'", Locale.ENGLISH);
            String title = outputFormat.format(date);

            tvHeaderTitle.setText(title);
        } catch (Exception e) {
            tvHeaderTitle.setText("Tasks");
        }
    }

    //Hàm tạo ngày cho thanh cuộn ngang
    // Hàm tạo giao diện thanh chọn ngày
    private void populateDates(int days) {
        llDatesContainer.removeAllViews();

        Calendar cal = Calendar.getInstance();

        // Các định dạng ngày
        SimpleDateFormat sdfDay = new SimpleDateFormat("EEE", Locale.ENGLISH); // Mon
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd", Locale.getDefault());  // 12
        SimpleDateFormat sdfFull = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()); // 12/11/2025

        LayoutInflater inflater = LayoutInflater.from(requireContext());

        for (int i = 0; i < days; i++) {
            String dayName = sdfDay.format(cal.getTime());
            String dateNum = sdfDate.format(cal.getTime());
            String fullDate = sdfFull.format(cal.getTime());

            // Nạp layout item_date.xml (File này bạn đã tạo ở bước trước)
            View itemView = inflater.inflate(R.layout.item_date, llDatesContainer, false);

            TextView tvDay = itemView.findViewById(R.id.tv_day_of_week);
            TextView tvDate = itemView.findViewById(R.id.tv_date);

            tvDay.setText(dayName);
            tvDate.setText(dateNum);

            // Lưu ngày đầy đủ vào Tag để dùng sau này
            itemView.setTag(fullDate);

            // Highlight ngày đang chọn
            if (fullDate.equals(selectedDateFilter)) {
                updateDateSelectionUI(itemView);
            } else {
                resetDateSelectionUI(itemView);
            }

            // Sự kiện Click vào ngày
            itemView.setOnClickListener(v -> {
                // 1. Reset màu các ngày khác
                for (int j = 0; j < llDatesContainer.getChildCount(); j++) {
                    View child = llDatesContainer.getChildAt(j);
                    resetDateSelectionUI(child);
                }

                // 2. Highlight ngày vừa chọn
                updateDateSelectionUI(v);

                // 3. Lọc dữ liệu
                selectedDateFilter = (String) v.getTag();
                filterTasksByDate(selectedDateFilter);
            });

            llDatesContainer.addView(itemView);

            // Tăng thêm 1 ngày
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
    }
    // Hàm làm nổi bật ngày
    private void updateDateSelectionUI(View view) {
        // SAI (Sẽ mất bo góc):
        // view.setBackgroundColor(Color.parseColor("#6200EE"));

        // ĐÚNG (Giữ nguyên bo góc):
        view.setBackgroundResource(R.drawable.bg_date_selected);

        TextView tvDay = view.findViewById(R.id.tv_day_of_week);
        TextView tvDate = view.findViewById(R.id.tv_date);
        tvDay.setTextColor(Color.WHITE);
        tvDate.setTextColor(Color.WHITE);
    }

    // Hàm trả về trạng thái bình thường
    private void resetDateSelectionUI(View view) {
        // SAI (Sẽ mất bo góc):
        // view.setBackgroundColor(Color.TRANSPARENT);

        // ĐÚNG (Giữ nguyên bo góc):
        view.setBackgroundResource(R.drawable.bg_date_unselected);

        TextView tvDay = view.findViewById(R.id.tv_day_of_week);
        TextView tvDate = view.findViewById(R.id.tv_date);

        // Đổi lại màu chữ (đen hoặc xám)
        tvDay.setTextColor(Color.parseColor("#757575")); // Màu xám nhạt cho Thứ
        tvDate.setTextColor(Color.BLACK); // Màu đen cho Ngày
    }
}
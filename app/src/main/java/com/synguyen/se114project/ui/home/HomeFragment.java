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
import com.synguyen.se114project.viewmodel.MainViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private MainViewModel mainViewModel;

    // Các View trong layout mới
    private RecyclerView rvTasks;
    private LinearLayout llDatesContainer;
    private TextView btnAdd;         // Nút thêm mới
    private TextView tvHeaderTitle;  // Tiêu đề ngày tháng (VD: Nov 29 Tasks)

    private TaskAdapter adapter;

    // Biến lưu timestamp của ngày đang được chọn (để so sánh và highlight UI)
    private long selectedDateTimestamp = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
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
        // Khởi tạo adapter rỗng ban đầu
        adapter = new TaskAdapter(new ArrayList<>(), task -> {
            // Xử lý khi click vào item task (VD: Mở chi tiết)
            Toast.makeText(getContext(), "Chọn: " + task.getTitle(), Toast.LENGTH_SHORT).show();
            // TODO: Thêm logic điều hướng sang màn hình sửa task tại đây
        });
        rvTasks.setAdapter(adapter);

        // 3. Khởi tạo ViewModel
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        // 4. Quan sát dữ liệu ĐÃ ĐƯỢC LỌC từ ViewModel
        // Bất cứ khi nào mainViewModel.setSelectedDate() được gọi, list này sẽ tự cập nhật
        mainViewModel.getTasksBySelectedDate().observe(getViewLifecycleOwner(), tasks -> {
            // Cập nhật lên RecyclerView
            adapter.setTasks(tasks);

            // Cập nhật tiêu đề (Ví dụ: "Nov 29th Tasks")
            updateHeaderTitle(selectedDateTimestamp);
        });

        // 5. Xử lý nút Thêm (+ Add Task)
        btnAdd.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
//            try {
//                // Đảm bảo action này đã tồn tại trong nav_graph.xml
//                navController.navigate(R.id.action_homeFragment_to_addEditFragment);
//            } catch (Exception e) {
            Toast.makeText(getContext(), "Chưa có Navigation Action!", Toast.LENGTH_SHORT).show();
//            }
        });

        // 6. Mặc định chọn ngày hôm nay khi mở màn hình lần đầu
        if (selectedDateTimestamp == 0) {
            selectedDateTimestamp = System.currentTimeMillis();
            mainViewModel.setSelectedDate(selectedDateTimestamp);
        }

        // 7. Tạo thanh lịch ngang (30 ngày tới)
        populateDates(30);
    }

    // Hàm cập nhật tiêu đề Header dựa trên timestamp
    private void updateHeaderTitle(long timestamp) {
        if (timestamp == 0) return;
        SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd 'Tasks'", Locale.ENGLISH);
        String title = outputFormat.format(new Date(timestamp));
        tvHeaderTitle.setText(title);
    }

    // Hàm tạo giao diện thanh chọn ngày
    private void populateDates(int days) {
        llDatesContainer.removeAllViews();

        Calendar cal = Calendar.getInstance();
        // Reset về giờ hiện tại để lịch bắt đầu từ hôm nay

        SimpleDateFormat sdfDay = new SimpleDateFormat("EEE", Locale.ENGLISH); // Mon
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd", Locale.getDefault());  // 12

        LayoutInflater inflater = LayoutInflater.from(requireContext());

        // Helper để so sánh ngày (bỏ qua giờ phút giây)
        Calendar selectedCal = Calendar.getInstance();
        selectedCal.setTimeInMillis(selectedDateTimestamp);

        for (int i = 0; i < days; i++) {
            long currentDateInMillis = cal.getTimeInMillis();

            String dayName = sdfDay.format(cal.getTime());
            String dateNum = sdfDate.format(cal.getTime());

            // Nạp layout item_date.xml
            View itemView = inflater.inflate(R.layout.item_date, llDatesContainer, false);

            TextView tvDay = itemView.findViewById(R.id.tv_day_of_week);
            TextView tvDate = itemView.findViewById(R.id.tv_date);

            tvDay.setText(dayName);
            tvDate.setText(dateNum);

            // Lưu timestamp vào Tag để dùng khi click
            itemView.setTag(currentDateInMillis);

            // Kiểm tra xem có phải ngày đang chọn không để highlight
            // So sánh ngày/tháng/năm
            boolean isSameDay = cal.get(Calendar.YEAR) == selectedCal.get(Calendar.YEAR) &&
                    cal.get(Calendar.DAY_OF_YEAR) == selectedCal.get(Calendar.DAY_OF_YEAR);

            if (isSameDay) {
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

                // 3. Lấy timestamp từ tag
                long clickedDate = (long) v.getTag();
                selectedDateTimestamp = clickedDate;

                // Cập nhật Calendar so sánh để lần sau vẽ lại vẫn đúng
                selectedCal.setTimeInMillis(selectedDateTimestamp);

                // 4. GỌI VIEWMODEL ĐỂ LỌC DATABASE
                mainViewModel.setSelectedDate(clickedDate);
            });

            llDatesContainer.addView(itemView);

            // Tăng thêm 1 ngày cho vòng lặp sau
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    // Hàm làm nổi bật ngày
    private void updateDateSelectionUI(View view) {
        // Sử dụng Drawable resource để giữ bo góc
        view.setBackgroundResource(R.drawable.bg_date_selected);

        TextView tvDay = view.findViewById(R.id.tv_day_of_week);
        TextView tvDate = view.findViewById(R.id.tv_date);
        tvDay.setTextColor(Color.WHITE);
        tvDate.setTextColor(Color.WHITE);
    }

    // Hàm bỏ chọn ngày
    private void resetDateSelectionUI(View view) {
        // Sử dụng Drawable resource để giữ bo góc (màu trắng/trong suốt)
        view.setBackgroundResource(R.drawable.bg_date_unselected);

        TextView tvDay = view.findViewById(R.id.tv_day_of_week);
        TextView tvDate = view.findViewById(R.id.tv_date);

        // Đặt màu xám/đen
        tvDay.setTextColor(Color.parseColor("#757575"));
        tvDate.setTextColor(Color.BLACK);
    }
}
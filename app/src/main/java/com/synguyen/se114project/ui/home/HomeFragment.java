package com.synguyen.se114project.ui.home;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.synguyen.se114project.R;
import com.synguyen.se114project.data.entity.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private RecyclerView rvTasks;
    private LinearLayout llDatesContainer;
    private TaskAdapter adapter;

    // List chứa toàn bộ dữ liệu gốc
    private List<Task> allTasks = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        rvTasks = root.findViewById(R.id.rvTasks);
        llDatesContainer = root.findViewById(R.id.llDatesContainer);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Tạo dữ liệu mẫu dynamic (tự động lấy ngày hôm nay)
        allTasks = generateSampleTasks();

        // 2. Khởi tạo adapter (ban đầu để rỗng, lát nữa hàm populateDates sẽ nạp dữ liệu)
        adapter = new TaskAdapter(new ArrayList<>(), task -> {
            // Xử lý khi click vào task (nếu cần)
        });

        rvTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTasks.setAdapter(adapter);

        // 3. Tạo thanh lịch và kích hoạt logic lọc
        populateDates(14);
    }

    private List<Task> generateSampleTasks() {
        List<Task> list = new ArrayList<>();

        // Lấy ngày hiện tại để tạo dữ liệu mẫu luôn khớp với lịch
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        String today = sdf.format(cal.getTime());

        cal.add(Calendar.DAY_OF_YEAR, 1);
        String tomorrow = sdf.format(cal.getTime());

        // Dữ liệu cho HÔM NAY
        list.add(new Task(1, today, "Prepare project report", "Project Finance", "09:00", "In progress", 1));
        list.add(new Task(2, today, "Code review", "Backend", "11:30", "Review", 0));
        list.add(new Task(3, today, "Team meeting", "Sprint planning", "14:00", "Meeting", 0));

        // Dữ liệu cho NGÀY MAI
        list.add(new Task(4, tomorrow, "Write unit tests", "Module X", "16:00", "To do", 2));
        list.add(new Task(5, tomorrow, "Update UI", "Frontend", "10:00", "To do", 1));

        list.add(new Task(6, tomorrow, "Write unit tests", "Module X", "16:00", "To do", 2));
        list.add(new Task(7, tomorrow, "Update UI", "Frontend", "10:00", "To do", 1));

        return list;
    }

    private void filterTasksByDate(String dateToFilter) {
        List<Task> filteredList = new ArrayList<>();
        for (Task task : allTasks) {
            // So sánh ngày của task với ngày được chọn từ lịch
            if (task.getDate().equals(dateToFilter)) {
                filteredList.add(task);
            }
        }
        // Cập nhật lên RecyclerView
        adapter.setTasks(filteredList);
    }

    private void populateDates(int days) {
        llDatesContainer.removeAllViews();

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdfDay = new SimpleDateFormat("EEE", Locale.getDefault());
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd", Locale.getDefault());
        SimpleDateFormat sdfFull = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        for (int i = 0; i < days; i++) {
            String dayName = sdfDay.format(cal.getTime());
            String dateNum = sdfDate.format(cal.getTime());
            String fullDate = sdfFull.format(cal.getTime()); // "30/11/2025"

            // Mặc định chọn ngày đầu tiên (i==0)
            boolean isFirstItem = (i == 0);

            TextView tv = createDateView(dayName, dateNum, fullDate, isFirstItem);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(dp(6), 0, dp(6), 0);
            llDatesContainer.addView(tv, params);

            // Nếu là ngày đầu tiên, gọi hàm lọc ngay lập tức để hiển thị dữ liệu hôm nay
            if (isFirstItem) {
                filterTasksByDate(fullDate);
            }

            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    private TextView createDateView(String dayName, String dateNum, String fullDate, boolean isSelected) {
        TextView tv = new TextView(requireContext());
        tv.setText(dayName + "\n" + dateNum);

        // QUAN TRỌNG: Lưu fullDate vào Tag để khi click lấy ra dùng
        tv.setTag(fullDate);

        tv.setTypeface(null, Typeface.NORMAL);
        tv.setTextSize(13f);
        tv.setPadding(dp(14), dp(10), dp(14), dp(10));
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        if (isSelected) {
            tv.setBackgroundResource(R.drawable.bg_date_selected);
            tv.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
        } else {
            tv.setBackgroundResource(R.drawable.bg_date_unselected);
            tv.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black));
        }

        tv.setOnClickListener(v -> {
            // 1. Reset màu tất cả các ngày về trạng thái chưa chọn
            for (int i = 0; i < llDatesContainer.getChildCount(); i++) {
                TextView child = (TextView) llDatesContainer.getChildAt(i);
                child.setBackgroundResource(R.drawable.bg_date_unselected);
                child.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black));
            }

            // 2. Highlight ngày vừa được click
            tv.setBackgroundResource(R.drawable.bg_date_selected);
            tv.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));

            // 3. Lấy ngày từ Tag ra và gọi hàm lọc
            String selectedDate = (String) v.getTag();
            filterTasksByDate(selectedDate);
        });

        return tv;
    }

    private int dp(int value) {
        if (getContext() == null) return value;
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (value * scale);
    }
}
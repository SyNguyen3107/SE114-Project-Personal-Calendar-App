package com.synguyen.se114project.ui.teacher.schedule;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView; // Import mới
import com.synguyen.se114project.R;
import com.synguyen.se114project.data.entity.Task;
import com.synguyen.se114project.ui.adapter.TeacherTaskAdapter;
import com.synguyen.se114project.ui.teacher.taskdetail.TeacherTaskDetailFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TeacherScheduleFragment extends Fragment {

    private LinearLayout llDatesContainer;
    private RecyclerView rcvTasks;
    private TextView tvHeaderTitle, btnAdd;
    private View layoutEmpty;

    private TeacherTaskAdapter adapter;
    private Calendar selectedDate = Calendar.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teacher_schedule, container, false);

        llDatesContainer = view.findViewById(R.id.llDatesContainer);
        tvHeaderTitle = view.findViewById(R.id.tvHeaderTitle);
        btnAdd = view.findViewById(R.id.btnAdd);
        rcvTasks = view.findViewById(R.id.rcvDailyTasks);
        layoutEmpty = view.findViewById(R.id.layoutEmptyState);

        rcvTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TeacherTaskAdapter(new ArrayList<>(), task -> {
            openTaskDetail(task);
        });
        rcvTasks.setAdapter(adapter);

        // Gọi hàm setup lịch
        setupHorizontalCalendar();
        updateHeaderDate();

        btnAdd.setOnClickListener(v -> Toast.makeText(getContext(), "Thêm task", Toast.LENGTH_SHORT).show());

        return view;
    }
    private void openTaskDetail(Task task) {
        TeacherTaskDetailFragment detailFragment = TeacherTaskDetailFragment.newInstance(task.getId(), task.getTitle());

        // Thực hiện giao dịch Fragment từ Activity cha (TeacherMainActivity)
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(
                            android.R.anim.slide_in_left,  // Hiệu ứng vào
                            android.R.anim.slide_out_right, // Hiệu ứng ra
                            android.R.anim.slide_in_left,
                            android.R.anim.slide_out_right
                    )
                    // Thay thế vào container full màn hình mà ta vừa tạo ở Bước 2
                    .add(R.id.fragment_container_full, detailFragment)
                    .addToBackStack(null) // Để user bấm nút Back trên điện thoại thì quay lại được
                    .commit();
        }
    }
    // --- CẬP NHẬT LOGIC LỊCH ---
    private void setupHorizontalCalendar() {
        llDatesContainer.removeAllViews();

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -3); // Bắt đầu từ 3 ngày trước

        for (int i = 0; i < 14; i++) { // Hiển thị 14 ngày
            // Inflate layout item_date mới
            View itemView = LayoutInflater.from(getContext()).inflate(R.layout.item_date, llDatesContainer, false);

            // 1. Ánh xạ theo ID mới trong item_date.xml
            MaterialCardView cardView = itemView.findViewById(R.id.card_date);
            TextView tvDayOfWeek = itemView.findViewById(R.id.tv_day_of_week);
            TextView tvDate = itemView.findViewById(R.id.tv_date);

            Date date = cal.getTime();
            int day = cal.get(Calendar.DAY_OF_MONTH);
            String dayName = new SimpleDateFormat("EEE", Locale.getDefault()).format(date).toUpperCase();

            tvDayOfWeek.setText(dayName);
            tvDate.setText(String.valueOf(day));

            // Kiểm tra ngày đang chọn
            boolean isSelected = isSameDay(cal, selectedDate);
            updateDateItemStyle(cardView, tvDayOfWeek, tvDate, isSelected);

            Calendar currentItemCal = (Calendar) cal.clone();
            itemView.setOnClickListener(v -> {
                selectedDate = currentItemCal;
                setupHorizontalCalendar(); // Vẽ lại
                updateHeaderDate();
                // TODO: loadTasksForDate(selectedDate);
            });

            llDatesContainer.addView(itemView);
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    // --- HÀM STYLE MỚI CHO MATERIAL CARD ---
    private void updateDateItemStyle(MaterialCardView card, TextView tvDay, TextView tvNum, boolean isSelected) {
        if (isSelected) {
            // Style KHI CHỌN: Nền xanh, Chữ trắng, Stroke ẩn (hoặc trùng màu nền)
            card.setCardBackgroundColor(Color.parseColor("#2196F3")); // Màu chủ đạo App
            card.setStrokeColor(Color.TRANSPARENT);
            card.setStrokeWidth(0);

            tvDay.setTextColor(Color.WHITE);
            tvNum.setTextColor(Color.WHITE);
        } else {
            // Style KHÔNG CHỌN: Nền trắng, Chữ đen/xám, Stroke xám nhạt
            card.setCardBackgroundColor(Color.WHITE);
            card.setStrokeColor(Color.parseColor("#E0E0E0")); // Màu viền xám nhạt
            card.setStrokeWidth(2); // 1dp ~ 2-3px tùy màn hình, set cứng int

            tvDay.setTextColor(Color.parseColor("#757575")); // Xám
            tvNum.setTextColor(Color.BLACK);
        }
    }

    private void updateHeaderDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault());
        tvHeaderTitle.setText(sdf.format(selectedDate.getTime()));
    }

    private boolean isSameDay(Calendar c1, Calendar c2) {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }
}
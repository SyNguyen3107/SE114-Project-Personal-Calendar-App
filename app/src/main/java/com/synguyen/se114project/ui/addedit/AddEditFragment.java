package com.synguyen.se114project.ui.addedit;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.textfield.TextInputEditText;
import com.synguyen.se114project.R;
import com.synguyen.se114project.data.entity.Subtask;
import com.synguyen.se114project.data.entity.Task;
import com.synguyen.se114project.viewmodel.MainViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddEditFragment extends Fragment {

    private MainViewModel mainViewModel;

    // Views
    private TextInputEditText etTitle, etSubtitle;
    private TextView btnDate, btnTime, btnAddSubtask;
    private Button btnSave;
    private LinearLayout layoutSubtasksContainer;

    // Data Temp
    private long selectedDateTimestamp = 0;
    private String selectedTimeString = "";
    private final Calendar calendar = Calendar.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_edit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ View
        etTitle = view.findViewById(R.id.etTitle);
        etSubtitle = view.findViewById(R.id.etSubtitle);
        btnDate = view.findViewById(R.id.btnDate);
        btnTime = view.findViewById(R.id.btnTime);
        btnSave = view.findViewById(R.id.btnSave);

        // Subtask Views
        btnAddSubtask = view.findViewById(R.id.btnAddSubtask);
        layoutSubtasksContainer = view.findViewById(R.id.layoutSubtasksContainer);

        // 2. ViewModel
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        // 3. Khởi tạo mặc định
        updateDateDisplay(System.currentTimeMillis());
        updateTimeDisplay(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));

        // 4. Sự kiện click
        btnDate.setOnClickListener(v -> showDatePicker());
        btnTime.setOnClickListener(v -> showTimePicker());

        // Sự kiện thêm dòng Subtask mới
        btnAddSubtask.setOnClickListener(v -> addSubtaskInputRow(""));

        // Sự kiện Lưu
        btnSave.setOnClickListener(v -> saveTask());

        // Setup nút Back (Sử dụng ID btnBack mới trong XML)
        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());
        }
    }

    // Hàm thêm một dòng nhập liệu Subtask vào Container
    private void addSubtaskInputRow(String content) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View subtaskView = inflater.inflate(R.layout.item_input_subtask, layoutSubtasksContainer, false);

        EditText etSubtaskName = subtaskView.findViewById(R.id.etSubtaskName);
        View btnRemove = subtaskView.findViewById(R.id.btnRemoveSubtask);

        etSubtaskName.setText(content);

        // Xử lý nút xóa dòng này
        btnRemove.setOnClickListener(v -> {
            layoutSubtasksContainer.removeView(subtaskView);
        });

        layoutSubtasksContainer.addView(subtaskView);
    }

    private void saveTask() {
        String title = etTitle.getText().toString().trim();
        String subtitle = etSubtitle.getText().toString().trim();

        // VALIDATE DỮ LIỆU
        if (title.isEmpty()) {
            etTitle.setError("Title cannot be empty");
            etTitle.requestFocus();
            return;
        }

        // Tạo Task cha
        Task newTask = new Task(
                title,
                subtitle,
                selectedDateTimestamp,
                selectedTimeString,
                "General",
                1
        );

        // Thu thập danh sách Subtask từ giao diện
        List<Subtask> subtaskList = new ArrayList<>();

        // Duyệt qua tất cả các view con trong Container
        for (int i = 0; i < layoutSubtasksContainer.getChildCount(); i++) {
            View row = layoutSubtasksContainer.getChildAt(i);
            EditText etName = row.findViewById(R.id.etSubtaskName);
            String subtaskTitle = etName.getText().toString().trim();

            // Chỉ thêm nếu tên subtask không rỗng
            if (!subtaskTitle.isEmpty()) {
                // taskId tạm thời là 0, Repository sẽ xử lý gán ID thật sau
                subtaskList.add(new Subtask(0, subtaskTitle));
            }
        }

        // Gọi ViewModel lưu tất cả (Hàm này bạn đã viết ở bước trước trong MainViewModel)
        mainViewModel.saveTask(newTask, subtaskList);

        Toast.makeText(getContext(), "Task saved successfully!", Toast.LENGTH_SHORT).show();
        Navigation.findNavController(requireView()).popBackStack();
    }

    // --- Các hàm hỗ trợ Date/Time Picker ---
    private void showDatePicker() {
        DatePickerDialog datePicker = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    updateDateDisplay(calendar.getTimeInMillis());
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePicker.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePicker = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> updateTimeDisplay(hourOfDay, minute),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );
        timePicker.show();
    }

    private void updateDateDisplay(long timestamp) {
        selectedDateTimestamp = timestamp;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        btnDate.setText(sdf.format(timestamp));
    }

    private void updateTimeDisplay(int hour, int minute) {
        selectedTimeString = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
        btnTime.setText(selectedTimeString);
    }
}
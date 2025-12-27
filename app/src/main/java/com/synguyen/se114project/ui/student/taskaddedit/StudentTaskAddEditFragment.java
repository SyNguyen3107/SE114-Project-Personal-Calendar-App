package com.synguyen.se114project.ui.student.taskaddedit;

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
import androidx.navigation.Navigation;

import com.google.android.material.textfield.TextInputEditText;
import com.synguyen.se114project.R;
import com.synguyen.se114project.data.entity.Subtask;
import com.synguyen.se114project.data.entity.Task;
import com.synguyen.se114project.viewmodel.student.AddEditViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StudentTaskAddEditFragment extends Fragment {

    private AddEditViewModel mViewModel;

    // Views
    private TextInputEditText etTitle, etSubtitle;
    private EditText etDuration; // Ô nhập thời lượng
    private TextView btnDate, btnTime, btnAddSubtask;
    private Button btnSave;
    private LinearLayout layoutSubtasksContainer;
    private View btnBack;

    private long selectedDateTimestamp = System.currentTimeMillis();
    private String selectedTimeString = "";
    private final Calendar calendar = Calendar.getInstance();

    private String currentTaskId = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_student_task_add_edit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ View
        etTitle = view.findViewById(R.id.etTitle);
        etSubtitle = view.findViewById(R.id.etSubtitle);
        etDuration = view.findViewById(R.id.etDuration); // Đảm bảo ID khớp với XML
        btnDate = view.findViewById(R.id.btnDate);
        btnTime = view.findViewById(R.id.btnTime);
        btnSave = view.findViewById(R.id.btnSave);
        btnAddSubtask = view.findViewById(R.id.btnAddSubtask);
        layoutSubtasksContainer = view.findViewById(R.id.layoutSubtasksContainer);
        btnBack = view.findViewById(R.id.btnBack);

        mViewModel = new ViewModelProvider(this).get(AddEditViewModel.class);

        // Mặc định
        updateDateDisplay(selectedDateTimestamp);
        updateTimeDisplay(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));

        // Mặc định duration là 25 phút nếu tạo mới
        if (currentTaskId == null) {
            etDuration.setText("25");
        }

        // Events
        btnDate.setOnClickListener(v -> showDatePicker());
        btnTime.setOnClickListener(v -> showTimePicker());
        btnAddSubtask.setOnClickListener(v -> addSubtaskInputRow(""));
        btnSave.setOnClickListener(v -> saveTask());
        if (btnBack != null) btnBack.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());

        if (getArguments() != null) {
            currentTaskId = getArguments().getString("taskId");
            // TODO: Nếu là Edit, cần load data cũ (bao gồm duration) lên View. Tạm thời chưa có logic load.
        }
    }

    private void showDatePicker() {
        DatePickerDialog datePicker = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            Calendar dateOnly = (Calendar) calendar.clone();
            dateOnly.set(Calendar.HOUR_OF_DAY, 0);
            dateOnly.set(Calendar.MINUTE, 0);
            dateOnly.set(Calendar.SECOND, 0);
            dateOnly.set(Calendar.MILLISECOND, 0);
            updateDateDisplay(dateOnly.getTimeInMillis());
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePicker = new TimePickerDialog(requireContext(), (view, hourOfDay, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            updateTimeDisplay(hourOfDay, minute);
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
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

    private void addSubtaskInputRow(String content) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View subtaskView = inflater.inflate(R.layout.item_input_subtask, layoutSubtasksContainer, false);
        EditText etSubtaskName = subtaskView.findViewById(R.id.etSubtaskName);
        View btnRemove = subtaskView.findViewById(R.id.btnRemoveSubtask);
        etSubtaskName.setText(content);
        btnRemove.setOnClickListener(v -> layoutSubtasksContainer.removeView(subtaskView));
        layoutSubtasksContainer.addView(subtaskView);
    }

    private void saveTask() {
        String title = etTitle.getText().toString().trim();
        String subtitle = etSubtitle.getText().toString().trim();
        String durationStr = etDuration.getText().toString().trim();

        if (title.isEmpty()) {
            etTitle.setError("Title cannot be empty");
            return;
        }

        // Tạo Task mới
        Task newTask = new Task(title, subtitle, selectedDateTimestamp, selectedTimeString, "General", 1);

        // Xử lý Duration (Chuyển từ Phút sang Mili giây)
        long durationMillis = 25 * 60 * 1000; // Mặc định 25p
        if (!durationStr.isEmpty()) {
            try {
                long minutes = Long.parseLong(durationStr);
                if (minutes > 0) {
                    durationMillis = minutes * 60 * 1000;
                }
            } catch (NumberFormatException e) {
                // Nếu nhập sai định dạng thì dùng mặc định, có thể thông báo lỗi nếu muốn
            }
        }
        newTask.setDuration(durationMillis);

        if (currentTaskId != null) {
            newTask.setId(currentTaskId);
        }

        List<Subtask> subtaskList = new ArrayList<>();
        String parentId = newTask.getId();
        for (int i = 0; i < layoutSubtasksContainer.getChildCount(); i++) {
            View row = layoutSubtasksContainer.getChildAt(i);
            EditText etName = row.findViewById(R.id.etSubtaskName);
            String subtaskTitle = etName.getText().toString().trim();
            if (!subtaskTitle.isEmpty()) {
                subtaskList.add(new Subtask(parentId, subtaskTitle));
            }
        }

        mViewModel.saveNewTask(newTask, subtaskList);
        Toast.makeText(getContext(), "Task saved with duration: " + (durationMillis / 60000) + " min", Toast.LENGTH_SHORT).show();
        Navigation.findNavController(requireView()).popBackStack();
    }
}
package com.synguyen.se114project.ui.addedit;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.synguyen.se114project.data.entity.Task;
import com.synguyen.se114project.viewmodel.MainViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddEditFragment extends Fragment {

    private MainViewModel mainViewModel;

    // Views
    private TextInputEditText etTitle, etSubtitle;
    private TextView btnDate, btnTime;
    private Button btnSave;

    // Biến lưu dữ liệu tạm thời
    private long selectedDateTimestamp = 0;
    private String selectedTimeString = "";

    // Calendar để xử lý chọn ngày giờ
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

        // 2. Khởi tạo ViewModel
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        // 3. Mặc định chọn ngày giờ hiện tại
        updateDateDisplay(System.currentTimeMillis());
        updateTimeDisplay(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));

        // 4. Sự kiện chọn Ngày
        btnDate.setOnClickListener(v -> showDatePicker());

        // 5. Sự kiện chọn Giờ
        btnTime.setOnClickListener(v -> showTimePicker());

        // 6. Sự kiện Lưu Task
        btnSave.setOnClickListener(v -> saveTask());

        // Setup nút Back trên toolbar (nếu có)
        view.findViewById(R.id.tvScreenTitle).setOnClickListener(v ->
                Navigation.findNavController(view).navigateUp()
        );
    }

    private void showDatePicker() {
        DatePickerDialog datePicker = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    // Reset giờ phút giây về 0 để lưu đúng mốc ngày
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
                true // True = 24h format
        );
        timePicker.show();
    }

    private void updateDateDisplay(long timestamp) {
        selectedDateTimestamp = timestamp;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        btnDate.setText(sdf.format(timestamp));
    }

    private void updateTimeDisplay(int hour, int minute) {
        // Format giờ đẹp (VD: 09:05)
        selectedTimeString = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
        btnTime.setText(selectedTimeString);
    }

    private void saveTask() {
        String title = etTitle.getText().toString().trim();
        String subtitle = etSubtitle.getText().toString().trim();

        // Validate dữ liệu
        if (title.isEmpty()) {
            etTitle.setError("Please enter title");
            return;
        }

        // Tạo đối tượng Task mới
        // (Lưu ý: Constructor Task của bạn: Title, Subtitle, Date(long), Time, Tag, Priority)
        Task newTask = new Task(
                title,
                subtitle,
                selectedDateTimestamp,
                selectedTimeString,
                "General", // Tag mặc định
                1 // Priority mặc định (Low)
        );

        // Gọi ViewModel để lưu vào Database
        mainViewModel.insertTask(newTask);

        Toast.makeText(getContext(), "Task saved successfully!", Toast.LENGTH_SHORT).show();

        // Quay lại màn hình Home
        NavController navController = Navigation.findNavController(requireView());
        navController.popBackStack();
    }
}
package com.synguyen.se114project.ui.teacher.course;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.gson.JsonObject;
import com.synguyen.se114project.R;
import com.synguyen.se114project.data.entity.Course;
import com.synguyen.se114project.data.remote.RetrofitClient;
import com.synguyen.se114project.data.remote.SupabaseService;
import com.synguyen.se114project.ui.adapter.TeacherCourseAdapter;
import com.synguyen.se114project.worker.SyncWorker;

import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherCourseFragment extends Fragment {

    private RecyclerView rcvCourses;
    private View fabAdd; 
    private TeacherCourseAdapter adapter;
    private String token;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teacher_course, container, false);

        SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        token = prefs.getString("ACCESS_TOKEN", null);
        userId = prefs.getString("USER_ID", null);

        rcvCourses = view.findViewById(R.id.rcvCourses);
        fabAdd = view.findViewById(R.id.fabAddCourse);

        rcvCourses.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TeacherCourseAdapter(course -> {
            String id = (course.getId() != null) ? course.getId() : "";
            String name = (course.getName() != null) ? course.getName() : "Chi tiết";

            Bundle bundle = new Bundle();
            bundle.putString("COURSE_ID", id);
            bundle.putString("COURSE_NAME", name);

            try {
                Navigation.findNavController(requireView()).navigate(
                        R.id.action_teacherCourseFragment_to_teacherCourseDetailFragment,
                        bundle
                );
            } catch (Exception e) {
                Toast.makeText(getContext(), "Lỗi điều hướng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        rcvCourses.setAdapter(adapter);

        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> showAddCourseDialog());
        }
        setupAutoSync();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCourses();
    }

    private void setupAutoSync() {
        if (getContext() == null) return;

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest syncRequest =
                new PeriodicWorkRequest.Builder(SyncWorker.class, 15, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .build();

        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
                "SyncTasksWork",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
        );
    }

    private void loadCourses() {
        if (token == null) return;

        SupabaseService service = RetrofitClient.getRetrofitInstance().create(SupabaseService.class);

        service.getCourses( "Bearer " + token, "eq." + userId)
                .enqueue(new Callback<List<Course>>() {
                    @Override
                    public void onResponse(Call<List<Course>> call, Response<List<Course>> response) {
                        if (!isAdded() || getContext() == null) return;

                        if (response.isSuccessful() && response.body() != null) {
                            adapter.submitList(response.body());
                        } else {
                            if (response.code() == 401) {
                                forceLogout();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Course>> call, Throwable t) {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Lỗi mạng", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void forceLogout() {
        if (getActivity() == null) return;

        SharedPreferences prefs = getActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(getContext(), com.synguyen.se114project.ui.auth.LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }

    private void showAddCourseDialog() {
        if (getContext() == null) return;

        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_course, null);
        
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(view)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        final EditText edtName = view.findViewById(R.id.edtCourseName);
        final EditText edtTime = view.findViewById(R.id.edtTimeSlot); 
        final EditText edtDesc = view.findViewById(R.id.edtCourseDesc);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnAdd = view.findViewById(R.id.btnAddCourse);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnAdd.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String time = edtTime != null ? edtTime.getText().toString().trim() : "";
            String desc = edtDesc.getText().toString().trim();
            
            if (!name.isEmpty()) {
                createCourseAPI(name, time, desc); 
                dialog.dismiss();
            } else {
                edtName.setError("Course name is required");
            }
        });

        dialog.show();
    }

    private void createCourseAPI(String name, String timeSlot, String description) {
        SupabaseService service = RetrofitClient.getRetrofitInstance().create(SupabaseService.class);

        // SỬA LỖI 400: Sử dụng JsonObject để chỉ gửi các trường hợp lệ lên Server
        JsonObject body = new JsonObject();
        body.addProperty("name", name);
        body.addProperty("time_slot", timeSlot);
        body.addProperty("description", description);
        body.addProperty("teacher_id", userId);
        body.addProperty("color_hex", "#304FFE"); // Mặc định xanh đậm

        service.createCourseJson("Bearer " + token, body)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (!isAdded()) return;

                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Created course successfully!", Toast.LENGTH_SHORT).show();
                            loadCourses();
                        } else {
                            Log.e("CREATE_COURSE", "Error Code: " + response.code());
                            try {
                                if (response.errorBody() != null) {
                                    Log.e("CREATE_COURSE", "Error Body: " + response.errorBody().string());
                                }
                            } catch (Exception ignored) {}
                            
                            Toast.makeText(getContext(), "Failed to create course: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        if (getContext() != null) {
                            Log.e("CREATE_COURSE", "Network Failure: " + t.getMessage());
                            Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
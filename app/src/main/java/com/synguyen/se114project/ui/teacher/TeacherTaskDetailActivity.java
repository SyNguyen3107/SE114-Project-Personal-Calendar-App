package com.synguyen.se114project.ui.teacher;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;
import com.synguyen.se114project.BuildConfig;
import com.synguyen.se114project.R;
import com.synguyen.se114project.data.remote.RetrofitClient;
import com.synguyen.se114project.data.remote.SupabaseService;
import com.synguyen.se114project.data.remote.response.FileObject;
import com.synguyen.se114project.ui.adapter.SubmissionAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherTaskDetailActivity extends AppCompatActivity {

    private TextView tvTitle, tvDeadline, tvDesc, tvEmpty;
    private RecyclerView rcvSubmissions;
    private ProgressBar progressBar;
    private ImageView btnBack;

    private SubmissionAdapter adapter;
    private String taskId, taskTitle, taskDesc, taskDeadline;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_task_detail);

        // Nhận dữ liệu từ Intent (Truyền từ TeacherTasksFragment)
        if (getIntent() != null) {
            taskId = getIntent().getStringExtra("TASK_ID");
            taskTitle = getIntent().getStringExtra("TASK_TITLE");
            taskDesc = getIntent().getStringExtra("TASK_DESC");
            taskDeadline = getIntent().getStringExtra("TASK_DEADLINE");
        }

        initViews();
        setupData();
        loadSubmissions();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvTaskTitle);
        tvDeadline = findViewById(R.id.tvTaskDeadline);
        tvDesc = findViewById(R.id.tvTaskDesc);
        tvEmpty = findViewById(R.id.tvEmpty);
        progressBar = findViewById(R.id.progressBar);
        rcvSubmissions = findViewById(R.id.rcvSubmissions);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        rcvSubmissions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SubmissionAdapter(this, new ArrayList<>());
        rcvSubmissions.setAdapter(adapter);
    }

    private void setupData() {
        tvTitle.setText(taskTitle != null ? taskTitle : "Chi tiết bài tập");
        tvDesc.setText(taskDesc != null ? taskDesc : "");
        tvDeadline.setText(taskDeadline != null ? "Hạn nộp: " + taskDeadline : "");
    }

    private void loadSubmissions() {
        if (taskId == null) return;

        progressBar.setVisibility(View.VISIBLE);
        SharedPreferences prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("ACCESS_TOKEN", "");

        SupabaseService service = RetrofitClient.getRetrofitInstance().create(SupabaseService.class);

        // Lọc file trong bucket 'assignments' có tên bắt đầu bằng "assign_{taskId}"
        JsonObject body = new JsonObject();
        body.addProperty("prefix", "assign_" + taskId);
        body.addProperty("limit", 100);

        service.listFiles(BuildConfig.SUPABASE_KEY, "Bearer " + token, "assignments", body)
                .enqueue(new Callback<List<FileObject>>() {
                    @Override
                    public void onResponse(Call<List<FileObject>> call, Response<List<FileObject>> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            List<FileObject> list = response.body();
                            if (list.isEmpty()) {
                                tvEmpty.setVisibility(View.VISIBLE);
                            } else {
                                tvEmpty.setVisibility(View.GONE);
                                adapter.updateData(list);
                            }
                        } else {
                            Toast.makeText(TeacherTaskDetailActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<FileObject>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(TeacherTaskDetailActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
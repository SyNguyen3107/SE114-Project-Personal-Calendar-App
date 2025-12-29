package com.synguyen.se114project.ui.community;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;
import com.synguyen.se114project.R;
import com.synguyen.se114project.data.repository.CommunityRepository;

import java.util.ArrayList;
import java.util.List;

public class CommunityListActivity extends AppCompatActivity {

    private RecyclerView rvCommunities;
    private Button btnCreate;
    private CommunityRepository repo;
    private CommunityAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_list);

        rvCommunities = findViewById(R.id.rvCommunities);
        btnCreate = findViewById(R.id.btnCreateCommunity);

        repo = new CommunityRepository();

        adapter = new CommunityAdapter(new ArrayList<>(), this::openChat);
        rvCommunities.setLayoutManager(new LinearLayoutManager(this));
        rvCommunities.setAdapter(adapter);

        btnCreate.setOnClickListener(v -> showCreateDialog());

        loadCommunities();
    }

    private String getToken() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        return prefs.getString("ACCESS_TOKEN", null);
    }

    private String getUserId() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        return prefs.getString("USER_ID", null);
    }

    private void loadCommunities() {
        String token = getToken();
        if (token == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }
        repo.getCommunities(this, token, new CommunityRepository.ResultCallback<List<JsonObject>>() {
            @Override
            public void onSuccess(List<JsonObject> data) {
                runOnUiThread(() -> adapter.setItems(data));
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(CommunityListActivity.this, message, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void showCreateDialog() {
        View v = getLayoutInflater().inflate(R.layout.dialog_create_community, null);
        EditText edtName = v.findViewById(R.id.edtCommunityName);
        EditText edtDesc = v.findViewById(R.id.edtCommunityDesc);

        new AlertDialog.Builder(this)
                .setTitle("Create Community")
                .setView(v)
                .setPositiveButton("Create", (dialog, which) -> {
                    String name = edtName.getText() != null ? edtName.getText().toString().trim() : "";
                    String desc = edtDesc.getText() != null ? edtDesc.getText().toString().trim() : "";
                    createCommunity(name, desc);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void createCommunity(String name, String desc) {
        String token = getToken();
        String owner = getUserId();
        if (token == null || owner == null) { Toast.makeText(this, "Please login", Toast.LENGTH_SHORT).show(); return; }
        repo.createCommunity(token, owner, name, desc, true, new CommunityRepository.ResultCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject data) {
                runOnUiThread(() -> {
                    Toast.makeText(CommunityListActivity.this, "Created", Toast.LENGTH_SHORT).show();
                    loadCommunities();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(CommunityListActivity.this, message, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void openChat(JsonObject community) {
        String id = community.has("id") ? community.get("id").getAsString() : null;
        String name = community.has("name") ? community.get("name").getAsString() : "Community";
        if (id == null) return;
        Intent intent = new Intent(this, CommunityChatActivity.class);
        intent.putExtra("communityId", id);
        intent.putExtra("communityName", name);
        startActivity(intent);
    }
}

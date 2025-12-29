package com.synguyen.se114project.ui.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.synguyen.se114project.R;

public class CommunityFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_community, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Try to find the whole profile button card first, then fall back to avatar
        View btnProfile = view.findViewById(R.id.btnProfile);
        if (btnProfile == null) {
            btnProfile = view.findViewById(R.id.imgUserAvatar);
        }

        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> {
                try {
                    NavHostFragment.findNavController(CommunityFragment.this).navigate(R.id.profileFragment);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Navigation Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        androidx.recyclerview.widget.RecyclerView rv = view.findViewById(R.id.rvCommunities);
        if (rv != null) {
            com.synguyen.se114project.ui.community.CommunityAdapter adapter = new com.synguyen.se114project.ui.community.CommunityAdapter(new java.util.ArrayList<>(), this::openChat);
            rv.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
            rv.setAdapter(adapter);

            loadCommunities(adapter);
        }

        View fab = view.findViewById(R.id.fabCreateCommunity);
        if (fab != null) {
            fab.setOnClickListener(v -> showCreateDialog());
        }
    }

    private void loadCommunities(com.synguyen.se114project.ui.community.CommunityAdapter adapter) {
        if (getContext() == null) return;
        
        com.synguyen.se114project.data.repository.CommunityRepository repo = new com.synguyen.se114project.data.repository.CommunityRepository();
        String token = getContext().getSharedPreferences("AppPrefs", android.content.Context.MODE_PRIVATE).getString("ACCESS_TOKEN", null);
        
        if (token != null) {
            repo.getCommunities(getContext(), token, new com.synguyen.se114project.data.repository.CommunityRepository.ResultCallback<java.util.List<com.google.gson.JsonObject>>() {
                @Override
                public void onSuccess(java.util.List<com.google.gson.JsonObject> data) {
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(() -> adapter.setItems(data));
                }

                @Override
                public void onError(String message) {
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show());
                }
            });
        }
    }

    private void showCreateDialog() {
        if (getContext() == null) return;

        View v = getLayoutInflater().inflate(R.layout.dialog_create_community, null);
        EditText edtName = v.findViewById(R.id.edtCommunityName);
        EditText edtDesc = v.findViewById(R.id.edtCommunityDesc);
        Button btnCancel = v.findViewById(R.id.btnCancel);
        Button btnCreate = v.findViewById(R.id.btnCreate);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(v)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        if (btnCancel != null) btnCancel.setOnClickListener(view -> dialog.dismiss());

        if (btnCreate != null) {
            btnCreate.setOnClickListener(view -> {
                String name = edtName.getText() != null ? edtName.getText().toString().trim() : "";
                String desc = edtDesc.getText() != null ? edtDesc.getText().toString().trim() : "";
                if (!name.isEmpty()) {
                    createCommunity(name, desc);
                    dialog.dismiss();
                } else {
                    edtName.setError("Name is required");
                }
            });
        }

        dialog.show();
    }

    private void createCommunity(String name, String desc) {
        String token = getContext() == null ? null : getContext().getSharedPreferences("AppPrefs", android.content.Context.MODE_PRIVATE).getString("ACCESS_TOKEN", null);
        String owner = getContext() == null ? null : getContext().getSharedPreferences("AppPrefs", android.content.Context.MODE_PRIVATE).getString("USER_ID", null);
        if (token == null || owner == null) return;

        com.synguyen.se114project.data.repository.CommunityRepository repo = new com.synguyen.se114project.data.repository.CommunityRepository();
        repo.createCommunity(token, owner, name, desc, true, new com.synguyen.se114project.data.repository.CommunityRepository.ResultCallback<com.google.gson.JsonObject>() {
            @Override
            public void onSuccess(com.google.gson.JsonObject data) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Community created!", Toast.LENGTH_SHORT).show();
                    getActivity().recreate();
                });
            }

            @Override
            public void onError(String message) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void openChat(com.google.gson.JsonObject community) {
        String id = community.has("id") ? community.get("id").getAsString() : null;
        String name = community.has("name") ? community.get("name").getAsString() : "Community";
        if (id == null) return;
        Bundle b = new Bundle();
        b.putString("communityId", id);
        b.putString("communityName", name);
        try {
            NavHostFragment.findNavController(CommunityFragment.this).navigate(R.id.action_community_to_chat, b);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Chat navigation error", Toast.LENGTH_SHORT).show();
        }
    }
}
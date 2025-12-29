package com.synguyen.se114project.ui.community;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;
import com.synguyen.se114project.BuildConfig;
import com.synguyen.se114project.R;
import com.synguyen.se114project.data.socket.CommunitySocketManager;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class CommunityChatFragment extends Fragment {

    private String communityId;
    private CommunitySocketManager socketManager;
    private com.synguyen.se114project.data.repository.AuthRepository authRepository;
    private boolean triedRefresh = false;
    private RecyclerView rvMessages;
    private MessageAdapter msgAdapter;
    private EditText edtMessage;
    private android.widget.ImageButton btnSend;
    private TextView tvTitle;

    private java.util.Map<String, Boolean> pendingReactions = new java.util.HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_community_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            communityId = getArguments().getString("communityId");
            String name = getArguments().getString("communityName");
            tvTitle = view.findViewById(R.id.tvCommunityTitle);
            tvTitle.setText(name != null ? name : "Community");
        }

        rvMessages = view.findViewById(R.id.rvMessages);
        edtMessage = view.findViewById(R.id.edtMessage);
        btnSend = view.findViewById(R.id.btnSend);

        msgAdapter = new MessageAdapter(new ArrayList<>(), getUserId());
        msgAdapter.setOnMessageActionListener(new MessageAdapter.OnMessageActionListener() {
            @Override
            public void onReact(com.google.gson.JsonObject message, int position) {
                try {
                    String serverMsgId = message.has("id") ? message.get("id").getAsString() : null;
                    boolean liked = message.has("liked") && message.get("liked").getAsBoolean();
                    if (serverMsgId == null || serverMsgId.isEmpty()) {
                        String clientId = message.has("client_message_id") ? message.get("client_message_id").getAsString() : null;
                        if (clientId != null && !clientId.isEmpty()) {
                            pendingReactions.put(clientId, liked);
                            msgAdapter.updateReaction(clientId, liked, getUserId());
                            socketManager.sendReactionByClientId(communityId, clientId, liked);
                            return;
                        }
                        return;
                    }
                    socketManager.sendReaction(communityId, serverMsgId, liked);
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
        rvMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMessages.setAdapter(msgAdapter);

        socketManager = CommunitySocketManager.getInstance();
        authRepository = new com.synguyen.se114project.data.repository.AuthRepository();

        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> {
            try {
                androidx.navigation.fragment.NavHostFragment.findNavController(CommunityChatFragment.this).navigateUp();
            } catch (Exception e) {
                if (getActivity() != null) requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });

        if (getToken() == null) {
            Toast.makeText(getContext(), "Please login to use Community chat", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSend.setOnClickListener(v -> {
            String txt = edtMessage.getText() != null ? edtMessage.getText().toString().trim() : "";
            if (txt.isEmpty()) return;

            String clientId = java.util.UUID.randomUUID().toString();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            String nowIso = sdf.format(new Date());

            JsonObject localJo = new JsonObject();
            localJo.addProperty("userId", getUserId());
            localJo.addProperty("content", txt);
            localJo.addProperty("created_at", nowIso);
            localJo.addProperty("client_message_id", clientId);

            msgAdapter.addMessage(localJo);
            rvMessages.scrollToPosition(msgAdapter.getItemCount()-1);
            edtMessage.setText("");

            socketManager.sendMessage(communityId, txt, clientId, args -> {
                try {
                    Object first = args.length > 0 ? args[0] : null;
                    if (first instanceof org.json.JSONObject) {
                        org.json.JSONObject res = (org.json.JSONObject) first;
                        if (!"ok".equals(res.optString("status", ""))) {
                            if (getActivity() != null) getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Send failed", Toast.LENGTH_SHORT).show());
                        }
                    }
                } catch (Exception e) { e.printStackTrace(); }
            });
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        String token = getToken();

        com.synguyen.se114project.data.repository.CommunityRepository repo = new com.synguyen.se114project.data.repository.CommunityRepository();
        repo.getMessagesByCommunity(getContext(), token, communityId, new com.synguyen.se114project.data.repository.CommunityRepository.ResultCallback<java.util.List<com.google.gson.JsonObject>>() {
            @Override
            public void onSuccess(java.util.List<com.google.gson.JsonObject> data) {
                if (getActivity() != null) getActivity().runOnUiThread(() -> {
                    java.util.List<com.google.gson.JsonObject> normalized = new java.util.ArrayList<>();
                    for (com.google.gson.JsonObject jo : data) {
                        com.google.gson.JsonObject n = new com.google.gson.JsonObject();
                        String uid = jo.has("user_id") ? jo.get("user_id").getAsString() : jo.has("userId") ? jo.get("userId").getAsString() : "";
                        String content = jo.has("content") ? jo.get("content").getAsString() : "";
                        n.addProperty("userId", uid);
                        n.addProperty("content", content);
                        n.addProperty("created_at", jo.has("created_at") ? jo.get("created_at").getAsString() : "");
                        normalized.add(n);
                    }
                    msgAdapter.setMessages(normalized);
                    rvMessages.scrollToPosition(msgAdapter.getItemCount()-1);
                });
            }

            @Override
            public void onError(String message) {
                if (getActivity() != null) getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Failed to load messages", Toast.LENGTH_SHORT).show());
            }
        });

        socketManager.connect(BuildConfig.SOCKET_SERVER_URL, token, args -> {
            socketManager.joinCommunity(communityId);
        }, args -> {
            try {
                JSONObject o = (JSONObject) args[0];
                if (o.has("message_id") || o.has("messageId") || o.has("client_message_id") || o.has("clientMessageId")) {
                    final String msgId = o.optString("message_id", o.optString("messageId", o.optString("client_message_id", o.optString("clientMessageId", ""))));
                    final boolean liked = o.optBoolean("liked", false);
                    if (getActivity() != null) getActivity().runOnUiThread(() -> msgAdapter.updateReaction(msgId, liked, o.optString("user_id", "")));
                    return;
                }

                if (getActivity() != null) getActivity().runOnUiThread(() -> {
                    try {
                        JsonObject jo = new JsonObject();
                        jo.addProperty("id", o.optString("id", ""));
                        jo.addProperty("client_message_id", o.optString("client_message_id", o.optString("clientMessageId", "")));
                        jo.addProperty("userId", o.optString("user_id", ""));
                        String contentStr = o.optString("content", "");
                        if (contentStr != null && contentStr.trim().equals("<reaction>")) return;
                        jo.addProperty("content", contentStr);
                        jo.addProperty("created_at", o.optString("created_at", ""));
                        String savedServerId = msgAdapter.updateOrInsertMessage(jo);
                        rvMessages.scrollToPosition(msgAdapter.getItemCount()-1);

                        String clientId = jo.has("client_message_id") ? jo.get("client_message_id").getAsString() : null;
                        if (clientId != null && pendingReactions.containsKey(clientId) && savedServerId != null) {
                            boolean liked = pendingReactions.remove(clientId);
                            socketManager.sendReaction(communityId, savedServerId, liked);
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                });
            } catch (Exception e) { e.printStackTrace(); }
        }, args -> {
            if (!triedRefresh) {
                triedRefresh = true;
                if (getContext() != null) {
                    authRepository.refreshAccessToken(getContext(), new com.synguyen.se114project.data.repository.AuthRepository.ResultCallback<String>() {
                        @Override
                        public void onSuccess(String newToken) {
                            socketManager.disconnect();
                            socketManager.connect(BuildConfig.SOCKET_SERVER_URL, newToken, args2 -> socketManager.joinCommunity(communityId), args2 -> {}, args2 -> {});
                        }
                        @Override
                        public void onError(String message) {}
                    });
                }
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        if (socketManager != null) {
            socketManager.leaveCommunity(communityId);
            socketManager.disconnect();
        }
    }

    private String getToken() {
        if (getContext() == null) return null;
        SharedPreferences prefs = getContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        return prefs.getString("ACCESS_TOKEN", null);
    }

    private String getUserId() {
        if (getContext() == null) return null;
        SharedPreferences prefs = getContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        return prefs.getString("USER_ID", null);
    }
}
package com.synguyen.se114project.ui.community;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;
import com.synguyen.se114project.BuildConfig;
import com.synguyen.se114project.R;
import com.synguyen.se114project.data.socket.CommunitySocketManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommunityChatActivity extends AppCompatActivity {

    private String communityId;
    private CommunitySocketManager socketManager;
    private RecyclerView rvMessages;
    private MessageAdapter msgAdapter;
    private EditText edtMessage;
    private Button btnSend;
    private TextView tvTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_chat);

        communityId = getIntent().getStringExtra("communityId");
        String name = getIntent().getStringExtra("communityName");

        rvMessages = findViewById(R.id.rvMessages);
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);
        tvTitle = findViewById(R.id.tvCommunityTitle);
        tvTitle.setText(name != null ? name : "Community");

        // Back button
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        msgAdapter = new MessageAdapter(new ArrayList<>(), getUserId());
        msgAdapter.setOnMessageActionListener(new MessageAdapter.OnMessageActionListener() {
            @Override
            public void onReact(com.google.gson.JsonObject message, int position) {
                try {
                    String serverMsgId = message.has("id") ? message.get("id").getAsString() : null;
                    boolean liked = message.has("liked") && message.get("liked").getAsBoolean();
                    if (serverMsgId == null || serverMsgId.isEmpty()) {
                        Toast.makeText(CommunityChatActivity.this, "Vui lòng đợi tin nhắn được gửi xong", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    socketManager.sendReaction(communityId, serverMsgId, liked);
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(msgAdapter);

        socketManager = CommunitySocketManager.getInstance();
        String token = getToken();
        socketManager.connect(BuildConfig.SOCKET_SERVER_URL, token, args -> {
            runOnUiThread(() -> {
                socketManager.joinCommunity(communityId);
            });
        }, args -> {
            // Nhận tin nhắn từ Server
            try {
                JSONObject o = (JSONObject) args[0];
                runOnUiThread(() -> {
                    com.google.gson.JsonObject jo = new com.google.gson.JsonObject();
                    jo.addProperty("id", o.optString("id", ""));
                    jo.addProperty("userId", o.optString("user_id", ""));
                    jo.addProperty("content", o.optString("content", ""));
                    jo.addProperty("client_message_id", o.optString("client_message_id", ""));
                    jo.addProperty("created_at", o.optString("created_at", ""));
                    
                    // Cập nhật hoặc chèn mới để tránh trùng lặp tin nhắn vừa gửi
                    msgAdapter.updateOrInsertMessage(jo);
                    rvMessages.scrollToPosition(msgAdapter.getItemCount()-1);
                });
            } catch (Exception e) { e.printStackTrace(); }
        }, args -> {
            runOnUiThread(() -> Toast.makeText(CommunityChatActivity.this, "Lỗi kết nối Server", Toast.LENGTH_SHORT).show());
        });

        btnSend.setOnClickListener(v -> {
            String txt = edtMessage.getText() != null ? edtMessage.getText().toString().trim() : "";
            if (txt.isEmpty()) return;

            // 1. Tạo Client ID duy nhất
            String clientMsgId = UUID.randomUUID().toString();

            // 2. Optimistic UI: hiển thị tin nhắn ngay lập tức
            com.google.gson.JsonObject localJo = new com.google.gson.JsonObject();
            localJo.addProperty("userId", getUserId());
            localJo.addProperty("content", txt);
            localJo.addProperty("client_message_id", clientMsgId); // Gán ID tạm
            
            msgAdapter.addMessage(localJo);
            rvMessages.scrollToPosition(msgAdapter.getItemCount()-1);
            edtMessage.setText("");

            // 3. Gửi kèm client_message_id để Server trả về đúng tin nhắn đó
            socketManager.sendMessage(communityId, txt, args2 -> {
                // Server phản hồi Ack
                try {
                    if (args2.length > 0 && args2[0] instanceof org.json.JSONObject) {
                        org.json.JSONObject res = (org.json.JSONObject) args2[0];
                        if ("ok".equals(res.optString("status"))) {
                            // Cập nhật lại tin nhắn bằng dữ liệu thật từ Server
                            JSONObject data = res.optJSONObject("data");
                            if (data != null) {
                                runOnUiThread(() -> {
                                    com.google.gson.JsonObject serverJo = new com.google.gson.JsonObject();
                                    serverJo.addProperty("id", data.optString("id"));
                                    serverJo.addProperty("userId", data.optString("user_id"));
                                    serverJo.addProperty("content", data.optString("content"));
                                    serverJo.addProperty("client_message_id", clientMsgId);
                                    serverJo.addProperty("created_at", data.optString("created_at"));
                                    
                                    msgAdapter.updateOrInsertMessage(serverJo);
                                });
                            }
                        }
                    }
                } catch (Exception e) { e.printStackTrace(); }
            });
        });
    }

    private String getToken() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        return prefs.getString("ACCESS_TOKEN", null);
    }

    private String getUserId() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        return prefs.getString("USER_ID", null);
    }
}
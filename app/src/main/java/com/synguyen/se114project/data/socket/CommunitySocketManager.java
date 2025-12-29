package com.synguyen.se114project.data.socket;

import android.util.Log;

import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class CommunitySocketManager {
    private static CommunitySocketManager instance;
    private Socket socket;

    private CommunitySocketManager() {}

    public static CommunitySocketManager getInstance() {
        if (instance == null) instance = new CommunitySocketManager();
        return instance;
    }

    public void connect(String serverUrl, String token, final Emitter.Listener onConnect, final Emitter.Listener onMessage, final Emitter.Listener onError) {
        try {
            if (token == null || token.isEmpty()) {
                Log.e("CommunitySocket", "Missing token: cannot connect");
                if (onError != null) onError.call(new Object[]{"Missing token"});
                return;
            }

            Log.d("CommunitySocket", "Connecting; token present? " + (token != null && !token.isEmpty()) + ", preview=" + (token==null?"<null>":token.substring(0, Math.min(8, token.length())) + "..."));

            IO.Options opts = new IO.Options();
            opts.forceNew = true;
            opts.reconnection = true;
            // Allow websocket first, but fall back to xhr polling if websocket fails
            opts.transports = new String[]{"websocket", "polling"};
            // increase handshake timeout
            opts.timeout = 20000;
            opts.reconnectionAttempts = 5;
            opts.reconnectionDelay = 2000; // ms
            // Put token in query so server can read handshake.auth.token equivalent
            opts.query = "token=" + token;
            Log.d("CommunitySocket", "Connecting to " + serverUrl + " with token length=" + token.length() + ", preview=" + token.substring(0, Math.min(20, token.length())) + "...");
            socket = IO.socket(serverUrl, opts);

            socket.on(Socket.EVENT_CONNECT, args -> {
                Log.d("CommunitySocket", "connected");
                if (onConnect != null) onConnect.call(args);
            });

            socket.on("message", args -> {
                if (onMessage != null) onMessage.call(args);
            });

            socket.on(Socket.EVENT_CONNECT_ERROR, args -> {
                String msg = args != null && args.length > 0 ? String.valueOf(args[0]) : "Connect error";
                Log.e("CommunitySocket", "connect error: " + msg);
                if (args != null) {
                    for (int i = 0; i < args.length; i++) {
                        Object a = args[i];
                        Log.e("CommunitySocket", "connect error arg[" + i + "]: " + String.valueOf(a));
                    }
                }
                if (onError != null) onError.call(new Object[]{msg});
            });

            socket.on("error", args -> {
                Log.e("CommunitySocket", "EVENT_ERROR (string): " + (args != null && args.length>0 ? String.valueOf(args[0]) : "<no-arg>"));
                if (onError != null) onError.call(new Object[]{"EVENT_ERROR"});
            });

            socket.on(Socket.EVENT_DISCONNECT, args -> {
                String reason = args != null && args.length > 0 ? String.valueOf(args[0]) : "<no-reason>";
                Log.w("CommunitySocket", "disconnected: " + reason);
            });

            socket.on("connect_error", args -> {
                Log.e("CommunitySocket", "connect_error event: " + (args != null && args.length>0 ? String.valueOf(args[0]) : "<no-arg>"));
            });

            // Some socket.io-client Java versions do not expose EVENT_CONNECT_TIMEOUT constant
            socket.on("connect_timeout", args -> {
                String msg = "Connect timeout";
                Log.e("CommunitySocket", msg);
                if (onError != null) onError.call(new Object[]{msg});
            });

            socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            if (onError != null) onError.call(new Object[]{e.getMessage()});
        }
    }

    public void joinCommunity(String communityId) {
        if (socket == null) return;
        JSONObject payload = new JSONObject();
        try { payload.put("communityId", communityId); } catch (Exception ignored) {}
        socket.emit("join_community", payload);
    }

    // Diagnostic helper: attempt connection using polling transport only (xhr polling)
    public void connectWithPolling(String serverUrl, String token, final Emitter.Listener onConnect, final Emitter.Listener onError) {
        try {
            if (token == null || token.isEmpty()) {
                Log.e("CommunitySocket", "Missing token: cannot connect (polling)");
                if (onError != null) onError.call(new Object[]{"Missing token"});
                return;
            }

            // Disconnect existing
            if (socket != null && socket.connected()) socket.disconnect();

            IO.Options opts = new IO.Options();
            opts.forceNew = true;
            opts.reconnection = true;
            opts.transports = new String[]{"polling"};
            opts.timeout = 20000;
            opts.reconnectionAttempts = 3;
            opts.reconnectionDelay = 1000;
            opts.query = "token=" + token;

            Log.d("CommunitySocket", "(polling) Connecting to " + serverUrl + " with token? " + (token != null));
            socket = IO.socket(serverUrl, opts);

            socket.on(Socket.EVENT_CONNECT, args -> {
                Log.d("CommunitySocket", "(polling) connected");
                if (onConnect != null) onConnect.call(args);
            });

            socket.on(Socket.EVENT_CONNECT_ERROR, args -> {
                String msg = args != null && args.length > 0 ? String.valueOf(args[0]) : "Connect error";
                Log.e("CommunitySocket", "(polling) connect error: " + msg);
                if (onError != null) onError.call(new Object[]{msg});
            });

            socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            if (onError != null) onError.call(new Object[]{e.getMessage()});
        }
    }

    public void leaveCommunity(String communityId) {
        if (socket == null) return;
        JSONObject payload = new JSONObject();
        try { payload.put("communityId", communityId); } catch (Exception ignored) {}
        socket.emit("leave_community", payload);
    }

    public void sendMessage(String communityId, String content) {
        sendMessage(communityId, content, null, null);
    }

    // Overload: include clientMessageId
    public void sendMessage(String communityId, String content, String clientMessageId, io.socket.client.Ack ack) {
        if (socket == null) {
            Log.e("CommunitySocket", "Cannot sendMessage: socket null");
            return;
        }
        JSONObject payload = new JSONObject();
        try {
            payload.put("communityId", communityId);
            payload.put("content", content);
            payload.put("type", "text");
            if (clientMessageId != null) payload.put("client_message_id", clientMessageId);
            Log.d("CommunitySocket", "emit send_message (client_message_id=" + clientMessageId + ") payload=" + payload.toString());
        } catch (Exception ignored) {}
        if (ack != null) socket.emit("send_message", payload, ack);
        else socket.emit("send_message", payload);
    }

    // Backward compatible signature
    public void sendMessage(String communityId, String content, io.socket.client.Ack ack) {
        sendMessage(communityId, content, null, ack);
    }

    // Send a reaction event (not persisted as a message)
    public void sendReaction(String communityId, String messageId, boolean liked) {
        if (socket == null) return;
        JSONObject payload = new JSONObject();
        try {
            payload.put("communityId", communityId);
            payload.put("message_id", messageId);
            payload.put("liked", liked);
        } catch (Exception ignored) {}
        socket.emit("reaction", payload);
    }

    // Send a reaction event referring to a client-generated message id (optimistic message not yet saved)
    public void sendReactionByClientId(String communityId, String clientMessageId, boolean liked) {
        if (socket == null) return;
        JSONObject payload = new JSONObject();
        try {
            payload.put("communityId", communityId);
            payload.put("client_message_id", clientMessageId);
            payload.put("liked", liked);
        } catch (Exception ignored) {}
        socket.emit("reaction", payload);
    }

    public void disconnect() {
        if (socket != null) socket.disconnect();
    }
}

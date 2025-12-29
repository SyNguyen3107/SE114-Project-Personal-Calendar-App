package com.synguyen.se114project.ui.community;

import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;
import com.synguyen.se114project.R;

import java.util.List;
import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.VH> {

    private List<JsonObject> items;
    private String currentUserId;
    private OnMessageActionListener actionListener;

    public interface OnMessageActionListener {
        void onReact(JsonObject message, int position);
    }

    public MessageAdapter(List<JsonObject> items, String currentUserId) {
        this.items = items;
        this.currentUserId = currentUserId;
    }

    public void setOnMessageActionListener(OnMessageActionListener l) {
        this.actionListener = l;
    }

    public void addMessage(JsonObject jo) {
        items.add(jo);
        notifyItemInserted(items.size()-1);
    }

    public void setMessages(List<JsonObject> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    // Update existing local message by client_message_id or id, otherwise append
    // Returns the server message id if available (or null)
    public String updateOrInsertMessage(JsonObject serverJo) {
        String clientId = serverJo.has("client_message_id") ? serverJo.get("client_message_id").getAsString() : null;
        String serverId = serverJo.has("id") ? serverJo.get("id").getAsString() : null;

        // Try find by client_message_id
        for (int i = 0; i < items.size(); i++) {
            JsonObject it = items.get(i);
            if (clientId != null && it.has("client_message_id") && clientId.equals(it.get("client_message_id").getAsString())) {
                // replace and done
                items.set(i, serverJo);
                notifyItemChanged(i);
                return serverId;
            }
            if (serverId != null && it.has("id") && serverId.equals(it.get("id").getAsString())) {
                items.set(i, serverJo);
                notifyItemChanged(i);
                return serverId;
            }
        }
        // not found -> append
        addMessage(serverJo);
        return serverId;
    }

    public void updateReaction(String messageId, boolean liked, String userId) {
        if (messageId == null || messageId.isEmpty()) return;
        for (int i = 0; i < items.size(); i++) {
            JsonObject it = items.get(i);
            // Match either saved id or client_message_id
            if ((it.has("id") && messageId.equals(it.get("id").getAsString()))
                || (it.has("client_message_id") && messageId.equals(it.get("client_message_id").getAsString()))) {
                it.addProperty("liked", liked);
                notifyItemChanged(i);
                return;
            }
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        JsonObject jo = items.get(position);
        String sender = jo.has("userId") ? jo.get("userId").getAsString() : "";
        String content = jo.has("content") ? jo.get("content").getAsString() : "";
        String time = jo.has("created_at") ? jo.get("created_at").getAsString() : "";
        boolean liked = jo.has("liked") && jo.get("liked").getAsBoolean();

        holder.tvContent.setText(content);

        // Format timestamp nicely: show time (HH:mm) for messages sent today, otherwise show dd/MM/yyyy HH:mm
        String timeDisplay = "";
        if (!time.isEmpty()) {
            try {
                OffsetDateTime odt = OffsetDateTime.parse(time);
                LocalDate msgDate = odt.toLocalDate();
                LocalTime msgTime = odt.toLocalTime().truncatedTo(ChronoUnit.MINUTES);
                DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
                DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                if (msgDate.equals(LocalDate.now())) {
                    timeDisplay = msgTime.format(timeFmt);
                } else {
                    timeDisplay = dateFmt.format(msgDate) + " " + msgTime.format(timeFmt);
                }
            } catch (Exception e) {
                // fallback: simple parsing to avoid crashes if format unexpected
                try {
                    String[] parts = time.split("T");
                    String datePart = parts.length > 0 ? parts[0] : time;
                    String timePart = "";
                    if (parts.length > 1) {
                        String raw = parts[1];
                        String[] tParts = raw.split("\\.|\\+|-");
                        timePart = tParts.length > 0 ? tParts[0] : raw;
                        if (timePart.length() >= 5) timePart = timePart.substring(0, 5);
                    }
                    if (datePart.equals(java.time.LocalDate.now().toString())) timeDisplay = timePart;
                    else timeDisplay = datePart + (timePart.isEmpty() ? "" : " " + timePart);
                } catch (Exception ex) {
                    timeDisplay = time.split("T")[0];
                }
            }
        }
        holder.tvTime.setText(timeDisplay);

        // Decide alignment
        boolean isMe = currentUserId != null && currentUserId.equals(sender);
        if (holder.bubbleWrapper.getLayoutParams() instanceof FrameLayout.LayoutParams) {
            FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams) holder.bubbleWrapper.getLayoutParams();
            flp.gravity = isMe ? Gravity.END : Gravity.START;
            holder.bubbleWrapper.setLayoutParams(flp);
        }

        // Set bubble background and text color
        if (isMe) {
            holder.bubble.setBackgroundResource(R.drawable.bubble_right);
            holder.tvContent.setTextColor(0xFFFFFFFF);
            holder.tvTime.setTextColor(0xFFEEEEEE);
        } else {
            holder.bubble.setBackgroundResource(R.drawable.bubble_left);
            holder.tvContent.setTextColor(0xFF000000);
            holder.tvTime.setTextColor(0xFF777777);
        }

        // Heart visibility
        holder.ivHeart.setVisibility(liked ? View.VISIBLE : View.GONE);

        // Long press to toggle heart (kept for accessibility)
        holder.bubble.setOnLongClickListener(v -> {
            boolean newLiked = !(jo.has("liked") && jo.get("liked").getAsBoolean());
            jo.addProperty("liked", newLiked);
            holder.ivHeart.setVisibility(newLiked ? View.VISIBLE : View.GONE);
            if (actionListener != null) actionListener.onReact(jo, position);
            return true;
        });

        // Double-tap to like with pop animation
        GestureDetector gestureDetector = new GestureDetector(holder.itemView.getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                try {
                    boolean newLiked = !(jo.has("liked") && jo.get("liked").getAsBoolean());
                    jo.addProperty("liked", newLiked);
                    holder.ivHeart.setVisibility(newLiked ? View.VISIBLE : View.GONE);

                    // Big heart pop animation (if view exists)
                    if (holder.ivHeartBig != null) {
                        holder.ivHeartBig.setScaleX(0f);
                        holder.ivHeartBig.setScaleY(0f);
                        holder.ivHeartBig.setAlpha(1f);
                        holder.ivHeartBig.setVisibility(View.VISIBLE);
                        holder.ivHeartBig.animate().scaleX(1.3f).scaleY(1.3f).setDuration(140).withEndAction(() -> {
                            holder.ivHeartBig.animate().scaleX(1f).scaleY(1f).setDuration(90).withEndAction(() -> {
                                holder.ivHeartBig.animate().alpha(0f).setDuration(220).withEndAction(() -> holder.ivHeartBig.setVisibility(View.GONE)).start();
                            }).start();
                        }).start();
                    }

                    if (actionListener != null) actionListener.onReact(jo, position);
                } catch (Exception ex) { ex.printStackTrace(); }
                return true;
            }
        });

        holder.bubble.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        // Keep single click no-op
        holder.bubble.setOnClickListener(v -> {});
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        View bubbleWrapper;
        View bubble;
        TextView tvContent, tvTime;
        android.widget.ImageView ivHeart;
        android.widget.ImageView ivHeartBig;

        VH(@NonNull View itemView) {
            super(itemView);
            bubbleWrapper = itemView.findViewById(R.id.bubbleWrapper);
            bubble = itemView.findViewById(R.id.bubble);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvTime = itemView.findViewById(R.id.tvTime);
            ivHeart = itemView.findViewById(R.id.ivHeart);
            // large center heart for animation
            // (may be null on older layouts)
            try { ivHeartBig = itemView.findViewById(R.id.ivHeartBig); } catch (Exception ignored) { ivHeartBig = null; }
        }
    }
}
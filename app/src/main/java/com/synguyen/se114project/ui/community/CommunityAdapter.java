package com.synguyen.se114project.ui.community;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;
import com.synguyen.se114project.R;

import java.util.List;

public class CommunityAdapter extends RecyclerView.Adapter<CommunityAdapter.VH> {

    public interface OnClick {
        void onClick(JsonObject item);
    }

    private List<JsonObject> items;
    private final OnClick listener;

    public CommunityAdapter(List<JsonObject> items, OnClick listener) {
        this.items = items;
        this.listener = listener;
    }

    public void setItems(List<JsonObject> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Thay thế layout mặc định bằng item_community.xml mới thiết kế
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_community, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        JsonObject jo = items.get(position);
        String name = jo.has("name") ? jo.get("name").getAsString() : "Community";
        String desc = jo.has("description") ? jo.get("description").getAsString() : "";
        
        holder.tvName.setText(name);
        holder.tvDesc.setText(desc);
        
        holder.itemView.setOnClickListener(v -> listener.onClick(jo));
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvDesc;
        
        VH(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ đúng các ID trong item_community.xml
            tvName = itemView.findViewById(R.id.tvCommunityName);
            tvDesc = itemView.findViewById(R.id.tvCommunityDesc);
        }
    }
}
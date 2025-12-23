package com.synguyen.se114project.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.synguyen.se114project.R;
import com.synguyen.se114project.data.remote.response.FileObject;
import java.util.ArrayList;
import java.util.List;

public class MaterialAdapter extends RecyclerView.Adapter<MaterialAdapter.ViewHolder> {

    private List<FileObject> fileList = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(FileObject file);
    }

    public MaterialAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setFiles(List<FileObject> files) {
        this.fileList = files;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_material, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FileObject file = fileList.get(position);
        holder.tvName.setText(file.getName());
        holder.tvSize.setText(file.getSize());

        holder.itemView.setOnClickListener(v -> listener.onItemClick(file));
    }

    @Override
    public int getItemCount() { return fileList.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvSize;
        ImageView imgIcon;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvFileName);
            tvSize = itemView.findViewById(R.id.tvFileSize);
            imgIcon = itemView.findViewById(R.id.imgFileIcon);
        }
    }
}
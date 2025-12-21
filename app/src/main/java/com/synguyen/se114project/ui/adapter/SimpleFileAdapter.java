package com.synguyen.se114project.ui.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.synguyen.se114project.R;
import com.synguyen.se114project.data.remote.response.FileObject;

import java.util.List;

public class SimpleFileAdapter extends RecyclerView.Adapter<SimpleFileAdapter.FileViewHolder> {

    private List<FileObject> fileList;
    private final OnFileClickListener listener;

    // Interface để hứng sự kiện click
    public interface OnFileClickListener {
        void onFileClick(FileObject file);
    }

    public SimpleFileAdapter(List<FileObject> fileList, OnFileClickListener listener) {
        this.fileList = fileList;
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<FileObject> newFiles) {
        this.fileList = newFiles;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        FileObject file = fileList.get(position);
        holder.bind(file, listener);
    }

    @Override
    public int getItemCount() {
        return fileList == null ? 0 : fileList.size();
    }

    static class FileViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvSize;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvFileName);
            tvSize = itemView.findViewById(R.id.tvFileSize);
        }

        public void bind(FileObject file, OnFileClickListener listener) {
            tvName.setText(file.name);

            // Tính toán size file (byte -> KB/MB)
            if (file.metadata != null) {
                long sizeKb = file.metadata.size / 1024;
                if (sizeKb > 1024) {
                    tvSize.setText(String.format("%.1f MB", sizeKb / 1024.0));
                } else {
                    tvSize.setText(sizeKb + " KB");
                }
            } else {
                tvSize.setText("");
            }

            // Click vào item
            itemView.setOnClickListener(v -> listener.onFileClick(file));
        }
    }
}
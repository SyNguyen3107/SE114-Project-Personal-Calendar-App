package com.synguyen.se114project.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.synguyen.se114project.R;
import com.synguyen.se114project.data.remote.response.FileObject; // Model này bạn đã có
import java.util.ArrayList;
import java.util.List;

public class MaterialAdapter extends RecyclerView.Adapter<MaterialAdapter.MaterialViewHolder> {

    private List<FileObject> mList = new ArrayList<>();
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onDownloadClick(FileObject file);
    }

    public void setList(List<FileObject> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    @NonNull
    @Override
    public MaterialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_material, parent, false);
        return new MaterialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MaterialViewHolder holder, int position) {
        FileObject file = mList.get(position);
        holder.bind(file);
    }

    @Override
    public int getItemCount() {
        return mList != null ? mList.size() : 0;
    }

    class MaterialViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvSize;
        View btnDownload;

        public MaterialViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvFileName);
            tvSize = itemView.findViewById(R.id.tvFileSize);
            btnDownload = itemView.findViewById(R.id.btnDownload);
        }

        public void bind(FileObject file) {
            tvName.setText(file.getName());

            // Format ngày tháng hoặc size nếu có (tạm thời để cứng hoặc lấy từ API nếu có field created_at)
            // String meta = "Size: " + file.getMetadata().getSize();
            tvSize.setText("Course Material");

            btnDownload.setOnClickListener(v -> {
                if (mListener != null) mListener.onDownloadClick(file);
            });
        }
    }
}
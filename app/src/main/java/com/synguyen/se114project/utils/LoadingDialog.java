package com.synguyen.se114project.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.synguyen.se114project.R;

public class LoadingDialog {
    private AlertDialog dialog;
    private TextView tvMessage;

    public LoadingDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null);
        tvMessage = view.findViewById(R.id.tvLoadingMessage);

        builder.setView(view);
        builder.setCancelable(false); // Không cho bấm ra ngoài để tắt
        dialog = builder.create();
    }

    public void show(String message) {
        if (tvMessage != null) tvMessage.setText(message);
        if (!dialog.isShowing()) dialog.show();
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
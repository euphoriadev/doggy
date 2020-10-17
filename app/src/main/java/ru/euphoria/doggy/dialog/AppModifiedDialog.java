package ru.euphoria.doggy.dialog;

import android.app.Activity;
import android.content.Context;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import ru.euphoria.doggy.R;

public class AppModifiedDialog extends MaterialAlertDialogBuilder {
    public AppModifiedDialog(Context context) {
        super(context);

        setTitle(R.string.error_check_license);
        setMessage(R.string.error_app_modified);
        setCancelable(false);
        setPositiveButton(android.R.string.ok, (dialog, which) -> ((Activity) context).finish());
    }
}

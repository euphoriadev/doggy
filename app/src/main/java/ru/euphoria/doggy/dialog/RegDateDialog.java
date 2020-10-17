package ru.euphoria.doggy.dialog;

import android.content.Context;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import ru.euphoria.doggy.api.model.User;
import ru.euphoria.doggy.util.AndroidUtil;

public class RegDateDialog extends MaterialAlertDialogBuilder {
    public RegDateDialog(Context context, User user, String msg) {
        super(context);

        setTitle(user.toString());
        setMessage(msg);
        setNeutralButton(android.R.string.copy, (dialog, which)
                -> AndroidUtil.copyText(context, msg));
        setPositiveButton(android.R.string.ok, null);
    }
}

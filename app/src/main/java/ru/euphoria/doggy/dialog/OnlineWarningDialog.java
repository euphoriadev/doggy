package ru.euphoria.doggy.dialog;

import android.content.Context;
import android.content.DialogInterface;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import ru.euphoria.doggy.R;

import static android.content.DialogInterface.BUTTON_POSITIVE;

public class OnlineWarningDialog extends MaterialAlertDialogBuilder {
    public OnlineWarningDialog(Context context,
                               DialogInterface.OnClickListener positive,
                               DialogInterface.OnClickListener negative) {
        super(context);

        setTitle(R.string.settings_online);
        setMessage(R.string.online_warring);
        setPositiveButton(android.R.string.ok, positive);
        setNegativeButton(android.R.string.cancel, negative);
        setOnCancelListener(dialog -> negative.onClick(dialog, BUTTON_POSITIVE));
    }
}

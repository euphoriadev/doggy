package ru.euphoria.doggy.dialog;

import android.content.Context;
import android.content.Intent;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;

import ru.euphoria.doggy.R;
import ru.euphoria.doggy.util.AndroidUtil;

public class CrashDialog extends MaterialAlertDialogBuilder {
    public CrashDialog(Context context, String crashedMsg) {
        super(context);

        setTitle(R.string.alert_feedback_title);
        setMessage(String.format(Locale.ROOT, "%s\n\n%s", context.getString(R.string.alert_feedback_message), crashedMsg));
        setPositiveButton("VK", (dialog, which) -> AndroidUtil.browse(context,
                "https://vk.com/igor.morozkin"));
        setNeutralButton("email", (dialog, which) -> {

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("message/rfc822");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"igmorozkin@gmail.com"});
            intent.putExtra(Intent.EXTRA_SUBJECT, "Scripts for VK. Crash Report");

            context.startActivity(Intent.createChooser(intent, "Choice email app"));
        });
        setNegativeButton(android.R.string.cancel, null);
    }
}

package ru.euphoria.doggy.dialog;

import android.content.Context;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;

import ru.euphoria.doggy.R;
import ru.euphoria.doggy.common.UpdateChecker;
import ru.euphoria.doggy.util.AndroidUtil;

public class UpdateDialog extends MaterialAlertDialogBuilder {
    public UpdateDialog(Context context, UpdateChecker.Config config) {
        super(context);

        String message = context.getString(R.string.found_update_message);

        setTitle(R.string.found_update);
        setMessage(String.format(Locale.getDefault(), message, config.version, config.build));
        setNegativeButton(android.R.string.cancel, null);
        setPositiveButton(android.R.string.ok, (dialog, which) -> {
            switch (config.type) {
                case UpdateChecker.Config.TYPE_PAGE:
                    AndroidUtil.browse(context, config.link);
                    break;
                case UpdateChecker.Config.TYPE_DIRECT:
                    AndroidUtil.download(context, config.link,
                            context.getString(R.string.app_name) + " "
                                    + config.version, "apk");
                    break;
            }
        });
    }
}

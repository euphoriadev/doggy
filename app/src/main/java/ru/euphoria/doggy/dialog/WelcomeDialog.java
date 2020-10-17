package ru.euphoria.doggy.dialog;

import android.app.Activity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import ru.euphoria.doggy.R;
import ru.euphoria.doggy.util.AndroidUtil;

public class WelcomeDialog extends MaterialAlertDialogBuilder {
    public WelcomeDialog(Activity context) {
        super(context);

        setTitle(R.string.settings_disable_ads);
        setMessage(R.string.disable_ads_first);
        setPositiveButton(R.string.buy, (dialog, which)
                -> AndroidUtil.purchaseDisableAds(context));
        setNegativeButton(android.R.string.cancel, null);
    }
}

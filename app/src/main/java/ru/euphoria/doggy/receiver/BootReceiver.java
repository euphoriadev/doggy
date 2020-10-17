package ru.euphoria.doggy.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

import ru.euphoria.doggy.SettingsFragment;
import ru.euphoria.doggy.service.OnlineService;
import ru.euphoria.doggy.util.AndroidUtil;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) && isEternalOnline(context)) {
            AndroidUtil.startService(context, new Intent(context, OnlineService.class));
        }
    }

    private boolean isEternalOnline(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(SettingsFragment.KEY_ONLINE, false);
    }
}

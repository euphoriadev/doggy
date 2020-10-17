package ru.euphoria.doggy;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import ru.euphoria.doggy.util.AndroidUtil;

public class SettingsActivity extends BaseActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setTitle(R.string.settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        boolean changeTheme = getIntent().getBooleanExtra("change_theme", false);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, new SettingsFragment())
                .commit();
        if (changeTheme) {
            addFragment(new SettingsFragment.GeneralSettingsFragment(getString(R.string.settings_general)));
        }
    }

    public void addFragment(PreferenceFragmentCompat fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_telegram:
                AndroidUtil.browse(this, SettingsFragment.LINK_TELEGRAM);
        }
        return super.onOptionsItemSelected(item);
    }


    public static void purchaseDialog(Activity activity) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity);
        builder.setTitle(R.string.settings_disable_ads);
        builder.setMessage(R.string.disable_ads_message);
        builder.setPositiveButton(R.string.action_buy, (dialog, which) -> AndroidUtil.purchaseDisableAds(activity));
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setNeutralButton(R.string.action_watch_video, (dialog, which) -> {
            if (!AndroidUtil.hasConnection()) {
                AndroidUtil.toastErrorConnection(activity);
            } else {
                AndroidUtil.loadRewardedAds(activity);
            }
        });
        builder.show();
    }
}

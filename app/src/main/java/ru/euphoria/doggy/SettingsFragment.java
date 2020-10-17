package ru.euphoria.doggy;

import android.app.Activity;
import android.app.TaskStackBuilder;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.SwitchPreference;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.yandex.metrica.YandexMetrica;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import ru.euphoria.doggy.api.VKApi;
import ru.euphoria.doggy.common.DirectLogin;
import ru.euphoria.doggy.data.SettingsStore;
import ru.euphoria.doggy.db.AppDatabase;
import ru.euphoria.doggy.dialog.AuthDialog;
import ru.euphoria.doggy.dialog.OnlineWarningDialog;
import ru.euphoria.doggy.dialog.TwoStepAuthDialog;
import ru.euphoria.doggy.service.OnlineService;
import ru.euphoria.doggy.util.AndroidUtil;

import static ru.euphoria.doggy.data.SettingsStore.KEY_LOGIN;
import static ru.euphoria.doggy.data.SettingsStore.nightMode;

@SuppressWarnings("CheckResult")
public class SettingsFragment extends BaseSettingsFragment
        implements Preference.OnPreferenceClickListener {
    public static final String KEY_NIGHT_MODE = "night_mode";
    public static final String KEY_APP_VERSION = "app_version";
    public static final String KEY_DEVELOPER = "developer";
    public static final String KEY_ONLINE = "online";
    public static final String KEY_ONLINE_PLATFORM = "online_platform";
    public static final String KEY_PAGE = "page";
    public static final String KEY_4PDA = "4pda";
    public static final String KEY_QIWI = "qiwi";
    public static final String KEY_VK_SCCRIPTS = "vk_scripts";
    public static final String KEY_DISABLE_ADS = "disable_ads";
    public static final String KEY_SHARE = "share";
    public static final String KEY_OAUTH_DOMAIN = "oauth_domain";
    public static final String KEY_API_DOMAIN = "api_domain";
    public static final String KEY_PROXY = "proxy";
    public static final String KEY_MUSIC_FOLDER = "music_folder";
    public static final String KEY_OPTIMIZED_SQLITE = "optimized_sqlite";
    public static final String KEY_IN_MEMORY_DATABASE = "in_memory_database";
    public static final String KEY_EXPORT_DATABASE = "export_database";
    public static final String KEY_CLEAR_CACHE = "clear_local_database";
    public static final String KEY_CLEAR_IMAGE_CACHE = "clear_image_cache";
    public static final String KEY_CLEAR_AUDIO_CACHE = "clear_audio_cache";
    public static final String KEY_LOGOUT = "logout";

    public static final String LINK_TELEGRAM = "https://t.me/euphoria_devs";
    public static final String LINK_DEVELOPER = "https://vk.com/igor.morozkin";
    public static final String LINK_4PDA = "http://4pda.ru/forum/index.php?showtopic=904174&st=20";
    public static final String LINK_SCRIPTS = "https://vkscripts.ru/";
    public static final String LINK_QIWI = "https://qiwi.me/igormorozkin";

    public static final String SCREEN_GENERAL = "screen_general";
    public static final String SCREEN_DATA_MEMORY = "screen_data_memory";
    public static final String SCREEN_OTHER = "screen_other";
    public static final String SCREEN_ABOUT = "screen_about";

    private static final int REQUEST_CODE_OPEN_DIRECTORY = 110;

    private SettingsActivity activity;

    public SettingsFragment() {
        super(AppContext.context.getString(R.string.settings));
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.prefs_main);
        activity = (SettingsActivity) getActivity();

        findPreference(SCREEN_GENERAL).setOnPreferenceClickListener(this);
        findPreference(SCREEN_DATA_MEMORY).setOnPreferenceClickListener(this);
        findPreference(SCREEN_OTHER).setOnPreferenceClickListener(this);
        findPreference(SCREEN_ABOUT).setOnPreferenceClickListener(this);

        findPreference(KEY_LOGOUT).setOnPreferenceClickListener(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OPEN_DIRECTORY
                && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();

        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String title = preference.getTitle().toString();

        switch (preference.getKey()) {
            case SCREEN_GENERAL: activity.addFragment(new GeneralSettingsFragment(title)); break;
            case SCREEN_DATA_MEMORY: activity.addFragment(new DataSettingsFragment(title)); break;
            case SCREEN_OTHER: activity.addFragment(new OtherSettingsFragment(title)); break;
            case SCREEN_ABOUT: activity.addFragment(new AboutSettingsFragment(title)); break;

            case KEY_LOGOUT: logout(); break;
        }
        return true;
    }

    private static void auth(Activity activity, String login, String password, String code, DirectLogin.Platform platform, int index) {
        DirectLogin.auth(login, password, platform, String.valueOf(VKApi.API_VERSION), AndroidUtil.getKateUserAgent(), code,
                accessToken -> activity.runOnUiThread(() -> {
                    if (AppContext.ads) {
                        AndroidUtil.loadInterstitialAds(activity);
                    }

                    SettingsStore.putValue(SettingsStore.KEY_ONLINE_ACCESS_TOKEN, accessToken);
                    SettingsStore.putValue(KEY_ONLINE_PLATFORM, index);

                    System.out.println("auth: " + index + " - " + platform + ", token " + accessToken);

                    HashMap<String, Object> attrs = new HashMap<>();
                    attrs.put("Платформа", platform.platform);
                    YandexMetrica.reportEvent("Смена онлайн платформы", attrs);

                    AndroidUtil.toast(activity, R.string.success);
                    SettingsStore.putValue(KEY_LOGIN, login);

                    if (SettingsStore.getBoolean("save_password")) {
                        SettingsStore.putValue(SettingsStore.KEY_PASSWORD, password);
                    }
                    if (SettingsStore.getBoolean(KEY_ONLINE)) {
                        Intent intent = new Intent(activity, OnlineService.class);
                        intent.putExtra("index", index);
                        intent.putExtra("token", accessToken);

                        AndroidUtil.startService(activity, intent);
                    }
                }), (needCode, captcha) -> {
                    if (needCode) {
                        activity.runOnUiThread(() -> {
                            TwoStepAuthDialog twoStepAuthDialog = new TwoStepAuthDialog(activity);
                            twoStepAuthDialog.setPositiveButton(android.R.string.ok, (dialog, which) ->
                                    auth(activity, login, password, twoStepAuthDialog.getCode(), platform, index));

                            twoStepAuthDialog.show();
                        });
                    } else {
                        activity.runOnUiThread(() -> createAuthDialog(activity, platform, index));
                    }
                });
    }

    public static void createAuthDialog(Activity activity, DirectLogin.Platform item, int index) {
        AuthDialog authDialog = new AuthDialog(activity);
        authDialog.setPositiveButton(android.R.string.ok, (dialog, which) ->
                auth(activity, authDialog.getLogin(), authDialog.getPassword(), null, item, index));
        authDialog.show();
    }

    private void logout() {
        SettingsStore.clear();
        AndroidUtil.clearCache();
        AndroidUtil.clearImageCache(getActivity());

        Intent intent = new Intent(activity, StartActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public static class GeneralSettingsFragment extends BaseSettingsFragment
            implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {
        private Activity activity;

        public GeneralSettingsFragment(String title) {
            super(title);
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.prefs_general);
            activity = getActivity();

            findPreference(KEY_NIGHT_MODE).setOnPreferenceChangeListener(this);
            findPreference(KEY_ONLINE).setOnPreferenceChangeListener(this);
            findPreference(KEY_ONLINE_PLATFORM).setOnPreferenceClickListener(this);

            Preference ads = findPreference(KEY_DISABLE_ADS);
            if (AppContext.ads) {
                ads.setOnPreferenceClickListener(this);
            } else {
                getPreferenceScreen().removePreference(ads);
            }

            findPreference(KEY_NIGHT_MODE)
                    .setIcon(nightMode() ?
                            R.drawable.ic_vector_night_outline
                            : R.drawable.ic_vector_sun);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            switch (preference.getKey()) {
                case KEY_NIGHT_MODE:
                    reloadTheme();
                    break;
                case KEY_ONLINE:
                    tryStartOnlineService((Boolean) newValue);
                    break;
            }
            return true;
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            switch (preference.getKey()) {
                case KEY_DISABLE_ADS:
                    SettingsActivity.purchaseDialog(activity);
                    break;
                case KEY_ONLINE_PLATFORM:
                    createOnlinePlatformDialog();
                    break;
            }
            return true;
        }


        private void reloadTheme() {
            TaskStackBuilder.create(activity)
                    .addNextIntent(new Intent(activity, MainActivity.class))
                    .addNextIntent(activity.getIntent().putExtra("change_theme", true))
                    .startActivities();

            activity.overridePendingTransition(R.anim.alpha_out, R.anim.alpha_in);
        }

        private void alertOnlineWarning() {
            DialogInterface.OnClickListener positive = (dialog, which)
                    -> changeOnlineServiceState(true);
            DialogInterface.OnClickListener negative = (dialog, which)
                    -> {
                changeOnlineServiceState(false);
                SwitchPreference preference = findPreference(KEY_ONLINE);
                preference.setChecked(false);
            };

            OnlineWarningDialog builder = new OnlineWarningDialog(activity, positive, negative);
            builder.show();
        }

        private void tryStartOnlineService(boolean value) {
            if (value) {
                alertOnlineWarning();
            } else {
                changeOnlineServiceState(false);
            }
        }

        private void changeOnlineServiceState(boolean value) {
            if (value) {
                activity.startService(new Intent(activity, OnlineService.class));
            } else {
                activity.stopService(new Intent(activity, OnlineService.class));
            }
        }

        public void createOnlinePlatformDialog() {
            String[] items = new String[DirectLogin.platforms.size() + 1];
            items[0] = getString(R.string.app_name);

            for (int i = 1; i < DirectLogin.platforms.size() + 1; i++) {
                items[i] = DirectLogin.platforms.get(i - 1).platform;
            }

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity);
            builder.setTitle(R.string.choose_platform);
            builder.setSingleChoiceItems(items, getCheckedOnlinePlatform(), (dialog, which) -> {
                if (which == 0) {
                    SettingsStore.getString(SettingsStore.KEY_ONLINE_ACCESS_TOKEN, SettingsStore.getAccessToken());
                    SettingsStore.putValue(KEY_ONLINE_PLATFORM, -1);
                } else {
                    createAuthDialog(activity, DirectLogin.platforms.get(which - 1), which - 1);
                }
                dialog.dismiss();
            });
            builder.show();
        }

        public int getCheckedOnlinePlatform() {
            return SettingsStore.getInt(KEY_ONLINE_PLATFORM) + 1;
        }

    }

    public static class DataSettingsFragment extends BaseSettingsFragment
            implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {
        private Activity activity;

        public DataSettingsFragment(String title) {
            super(title);
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.prefs_data_memory);
            activity = getActivity();

            findPreference(KEY_IN_MEMORY_DATABASE).setOnPreferenceChangeListener(this);
            findPreference(KEY_OPTIMIZED_SQLITE).setOnPreferenceChangeListener(this);

            findPreference(KEY_MUSIC_FOLDER).setOnPreferenceClickListener(this);
            findPreference(KEY_CLEAR_CACHE).setOnPreferenceClickListener(this);
            findPreference(KEY_CLEAR_IMAGE_CACHE).setOnPreferenceClickListener(this);
            findPreference(KEY_CLEAR_AUDIO_CACHE).setOnPreferenceClickListener(this);
            findPreference(KEY_EXPORT_DATABASE).setOnPreferenceClickListener(this);
            findPreference(KEY_MUSIC_FOLDER).setOnPreferenceClickListener(this);

        }

        @Override
        public void onResume() {
            super.onResume();

            if (isAdded()) {
                refreshPathAndSizes();
            }
        }


        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            switch (preference.getKey()) {
                case KEY_IN_MEMORY_DATABASE:
                    refreshDatabaseSize((Boolean) newValue);
                    break;
            }
            return true;
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            switch (preference.getKey()) {
                case KEY_MUSIC_FOLDER:
                    if (AndroidUtil.isGooglePlaySignature(activity)) {
                        suggestJoinTelegram();
                    }
                    break;

                case KEY_EXPORT_DATABASE:
                    exportCache();
                    break;
                case KEY_CLEAR_CACHE:
                    AndroidUtil.clearCache();
                    refreshPathAndSizes();
                    break;
                case KEY_CLEAR_IMAGE_CACHE:
                    AndroidUtil.clearImageCache(activity);
                    refreshPathAndSizes();
                    break;
                case KEY_CLEAR_AUDIO_CACHE:
                    AndroidUtil.clearAudioCache(activity);
                    refreshPathAndSizes();
                    break;
            }
            return true;
        }

        private void refreshDatabaseSize(boolean inMemory) {
            Preference cache = findPreference(KEY_CLEAR_CACHE);
            if (inMemory) {
                cache.setSummary(R.string.settings_in_memory_db);
            } else {
                cache.setSummary(getString(R.string.size_format, getDatabaseCacheSize()));
            }
            cache.setEnabled(!inMemory);
        }

        private void refreshPathAndSizes() {
            boolean inMemory = SettingsStore.getBoolean(KEY_IN_MEMORY_DATABASE);
            refreshDatabaseSize(inMemory);

            Preference imageCache = findPreference(KEY_CLEAR_IMAGE_CACHE);
            getImagesCacheSize(imageCache);

            Preference audioCache = findPreference(KEY_CLEAR_AUDIO_CACHE);
            getAudioCacheSize(audioCache);

            Preference musicFolder = findPreference(KEY_MUSIC_FOLDER);
            musicFolder.setSummary(getMusicFolder());

            String sqliteSummary = getString(R.string.settings_optimized_sqlite_summary);
            Preference sqliteVersion = findPreference(KEY_OPTIMIZED_SQLITE);
            sqliteVersion.setSummary(sqliteSummary + "\nCurrent version: " + AppContext.getSQLiteVersion());
        }

        public String getMusicFolder() {
            return SettingsStore.getMusicFolder();
        }

        private String getDatabaseCacheSize() {
            String name = AppDatabase.database().getOpenHelper().getDatabaseName();
            if (TextUtils.isEmpty(name)) return AndroidUtil.formatSize(0);

            File file = activity.getDatabasePath(name);
            return AndroidUtil.formatSize(file.length());
        }


        private void getImagesCacheSize(Preference preference) {
            File imageCache = AndroidUtil.getImageCacheFolder(activity);
            getCacheSize(preference, imageCache);
        }

        private void getAudioCacheSize(Preference preference) {
            File audioCache = AndroidUtil.getAudioCacheFolder(activity);
            getCacheSize(preference, audioCache);
        }

        private void getCacheSize(Preference preference, File folder) {
            AndroidUtil.folderSizeAsync(folder)
                    .filter(v -> isAdded())
                    .subscribe(size -> {
                        preference.setSummary(getString(R.string.size_format,
                                AndroidUtil.formatSize(size)));
                    });
        }

        private void suggestJoinTelegram() {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
            builder.setTitle("Пасхалочка");
            builder.setMessage("Хей, не многие знают, но в нашем телеграм канале есть версия с дополнительными фишками. Например рабочая музыка и возможность ее скачивать. Хочешь посмотреть?");
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
               AndroidUtil.browse(getActivity(), LINK_TELEGRAM);
               YandexMetrica.reportEvent("Пасхалка с музыкой");
            });
            builder.show();
        }

        private void exportCache() {
            if (AndroidUtil.checkStoragePermissions(activity)) {
                String name = AppDatabase.database().getOpenHelper().getDatabaseName();
                File file = activity.getDatabasePath(name);
                File dest = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        "cache-" + SettingsStore.getUserId() + ".db");

                try {
                    AndroidUtil.copy(file, dest);
                    AndroidUtil.toast(activity, "Exported to " + dest.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                    AndroidUtil.toast(activity, e.getMessage());
                }
            }
        }
    }

    public static class OtherSettingsFragment extends BaseSettingsFragment
            implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener{

        public OtherSettingsFragment(String title) {
            super(title);
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.prefs_other);

            findPreference(KEY_API_DOMAIN).setOnPreferenceChangeListener(this);
            findPreference(KEY_OAUTH_DOMAIN).setOnPreferenceChangeListener(this);
            findPreference(KEY_PROXY).setOnPreferenceChangeListener(this);
            findPreference(KEY_PROXY)
                    .setIcon(SettingsStore.getBoolean(KEY_PROXY)
                            ? R.drawable.ic_vector_shield_check
                            : R.drawable.ic_vector_shield);

            updateProxySummary();
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            switch (preference.getKey()) {
                case KEY_PROXY:
                    changeProxyState((Boolean) newValue);
                    break;
                case KEY_API_DOMAIN:
                    changeApiDomain(String.valueOf(newValue));
                    break;
                case KEY_OAUTH_DOMAIN:
                    changeOauthDomain(String.valueOf(newValue));
                    break;
            }
            updateProxySummary();
            return true;
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            return true;
        }

        private void changeApiDomain(String api) {
            if (TextUtils.isEmpty(api)) {
                api = VKApi.API_DOMAIN;
            }

            SettingsStore.putValue(KEY_API_DOMAIN, VKApi.apiDomain = api);
        }

        private void changeOauthDomain(String oauth) {
            if (TextUtils.isEmpty(oauth)) {
                oauth = VKApi.OAUTH_DOMAIN;
            }

            SettingsStore.putValue(KEY_OAUTH_DOMAIN, VKApi.oauthDomain = oauth);
        }

        private void updateProxySummary() {
            findPreference(KEY_API_DOMAIN).setSummary(VKApi.apiDomain);
            findPreference(KEY_OAUTH_DOMAIN).setSummary(VKApi.oauthDomain);
        }

        private void changeProxyState(boolean value) {
            findPreference(KEY_API_DOMAIN).setEnabled(!value);
            findPreference(KEY_OAUTH_DOMAIN).setEnabled(!value);
            findPreference(KEY_PROXY)
                    .setIcon(value ? R.drawable.ic_vector_shield_check
                            : R.drawable.ic_vector_shield);

            SettingsStore.putValue(KEY_API_DOMAIN, VKApi.apiDomain = (value)
                    ? VKApi.API_PROXY : VKApi.API_DOMAIN);
            SettingsStore.putValue(KEY_OAUTH_DOMAIN, VKApi.oauthDomain = (value)
                    ? VKApi.OAUTH_PROXY : VKApi.OAUTH_DOMAIN);


            YandexMetrica.reportEvent(value ? "Запуск прокси" : "Выключение прокси");
        }

    }

    public static class AboutSettingsFragment extends BaseSettingsFragment
            implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {
        private Activity activity;

        public AboutSettingsFragment(String title) {
            super(title);
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.prefs_about);
            activity = getActivity();

            findPreference(KEY_PAGE).setOnPreferenceClickListener(this);
            findPreference(KEY_VK_SCCRIPTS).setOnPreferenceClickListener(this);
            findPreference(KEY_SHARE).setOnPreferenceClickListener(this);
            findPreference(KEY_DEVELOPER).setOnPreferenceClickListener(this);

            updateAppVersion();
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            return false;
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            switch (preference.getKey()) {
                case KEY_PAGE:
                    AndroidUtil.browse(activity, LINK_TELEGRAM);
                    break;
                case KEY_VK_SCCRIPTS:
                    AndroidUtil.browse(activity, LINK_SCRIPTS);
                    break;
                case KEY_DEVELOPER:
                    AndroidUtil.browse(activity, LINK_DEVELOPER);
                    break;
                case KEY_QIWI:
                    AndroidUtil.browse(activity, LINK_QIWI);
                    break;
                case KEY_4PDA:
                    AndroidUtil.browse(activity, LINK_4PDA);
                    break;
                case KEY_SHARE:
                    createPost();
                    break;
                case KEY_MUSIC_FOLDER:
                    pickMusicFolder();
                    break;
            }
            return true;
        }

        private void createPost() {
            EditText text = new EditText(activity);
            text.setGravity(Gravity.START | Gravity.TOP);
            text.setHint("Ваш отзыв");
            text.setLines(3);
            AndroidUtil.setDialogContentPadding(text);

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity);
            builder.setTitle(R.string.settings_share);
            builder.setView(text);
            builder.setPositiveButton(android.R.string.ok, (dialog, which) -> new Thread(() -> {
                try {
                    VKApi.walls().post()
                            .ownerId(SettingsStore.getUserId())
                            .message(text.getText().toString() + "\n\nhttps://play.google.com/store/apps/details?id=ru.euphoria.doggy")
                            .json();

                    HashMap<String, Object> attrs = new HashMap<>();
                    attrs.put("Сообщение", text.getText().toString());

                    YandexMetrica.reportEvent("Поделились", attrs);

                    activity.runOnUiThread(() -> Toast.makeText(activity, "Спасибо!", Toast.LENGTH_SHORT).show());
                } catch (Exception e) {
                    e.printStackTrace();
                    activity.runOnUiThread(() -> Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }).start());
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.show();
        }

        private void updateAppVersion() {
            Preference app = findPreference(KEY_APP_VERSION);
            app.setTitle(R.string.app_name);
            app.setSummary(String.format(Locale.getDefault(),
                    activity.getString(R.string.setting_app_version), BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));
        }

        private void pickMusicFolder() {
//        if (AndroidUtils.checkStoragePermissions(activity)) {
//            Intent intent = new Intent(Intent.ACTION_PICK);
//            startActivityForResult(intent, REQUEST_CODE_OPEN_DIRECTORY);
//        }
        }
    }
}

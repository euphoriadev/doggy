package ru.euphoria.doggy;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.yandex.metrica.YandexMetrica;
import com.yandex.metrica.profile.Attribute;
import com.yandex.metrica.profile.GenderAttribute;
import com.yandex.metrica.profile.UserProfile;

import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.euphoria.doggy.ads.AdsManager;
import ru.euphoria.doggy.api.FoafParser;
import ru.euphoria.doggy.api.model.User;
import ru.euphoria.doggy.common.SignatureChecker;
import ru.euphoria.doggy.common.UpdateChecker;
import ru.euphoria.doggy.data.SettingsStore;
import ru.euphoria.doggy.db.AppDatabase;
import ru.euphoria.doggy.dialog.AppModifiedDialog;
import ru.euphoria.doggy.dialog.CheckLicenseDialog;
import ru.euphoria.doggy.dialog.CrashDialog;
import ru.euphoria.doggy.dialog.RegDateDialog;
import ru.euphoria.doggy.dialog.UpdateDialog;
import ru.euphoria.doggy.dialog.WelcomeDialog;
import ru.euphoria.doggy.service.OnlineService;
import ru.euphoria.doggy.util.AndroidUtil;
import ru.euphoria.doggy.util.GooglePlayUtil;
import ru.euphoria.doggy.util.UserUtil;
import ru.euphoria.doggy.util.ViewUtil;

import static android.view.View.GONE;
import static ru.euphoria.doggy.adapter.UsersAdapter.getOnlineIndicatorResource;

@SuppressLint("CheckResult")
public class MainActivity extends BaseActivity implements View.OnClickListener {

    public static final int ID_MESSAGES_STATS = View.generateViewId();
    public static final int ID_MESSAGES_CACHE_ALL = View.generateViewId();
    public static final int ID_MESSAGES_CHAT_RESTORE = View.generateViewId();
    public static final int ID_MESSAGES_CLEAN = View.generateViewId();
    public static final int ID_FRIENDS_REG_DATE = View.generateViewId();
    public static final int ID_FRIENDS_CONTACTS = View.generateViewId();
    public static final int ID_FRIENDS_DOGS_CLEAN = View.generateViewId();
    public static final int ID_FRIENDS_BIRTHDAY = View.generateViewId();
    public static final int ID_FRIENDS_LISTS = View.generateViewId();
    public static final int ID_FRIENDS_REQUESTS = View.generateViewId();
    public static final int ID_FRIENDS_MONITOR = View.generateViewId();
    public static final int ID_FRIENDS_HIDDEN = View.generateViewId();
    public static final int ID_PHOTOS_MAR = View.generateViewId();
    public static final int ID_GROUPS_CLEAN = View.generateViewId();
    public static final int ID_MUSIC_DOWNLOAD = View.generateViewId();
    public static final int ID_OTHER_CODE_EXECUTE = View.generateViewId();

    @BindView(R.id.user_fullname) TextView userName;
    @BindView(R.id.user_summary) TextView userScreenName;
    @BindView(R.id.user_avatar) ImageView userImage;

    private UnifiedNativeAdView adTemplate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar.setNavigationIcon(SettingsStore.nightMode()
                ? R.drawable.ic_vector_vk_dog_secondary
                : R.drawable.ic_vector_vk_dog);

        if (SettingsStore.getUserId() == -1) {
            startActivity(new Intent(this, StartActivity.class));
            finish();
            return;
        }
        YandexMetrica.setUserProfileID("id" + SettingsStore.getUserId());
        if (!AndroidUtil.hasConnection()) {
            YandexMetrica.reportEvent("Запуск в оффлайн режиме");
        }

        includeCards();
        startOnlineService();
        showFirstDialog();
        loadAds();

        checkCrashed();
        checkLicense();
        checkSignature();
        checkForUpdates();
        suggestJoinTelegram();

        AppDatabase.database().users().byId(SettingsStore.getUserId())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::displayInfo, AndroidUtil.handleError(this));

//        PhotoMapActivity.start(this);
//        TestActivity.start(this);
//        MessageGraphActivity.start(this);
//        MessageIgnoreList.start(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (!AppContext.ads) {
            menu.findItem(R.id.item_noads).setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.item_noads:
                SettingsActivity.purchaseDialog(this);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUser();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        YandexMetrica.sendEventsBuffer();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == ID_MESSAGES_STATS) {
            startActivity(data.setClass(this, MessageStatsActivity.class));
            return;
        }

        if (requestCode == ID_FRIENDS_REG_DATE) {
            if (!AndroidUtil.hasConnection()) {
                AndroidUtil.toastErrorConnection(this);
                return;
            }
            int id = data.getIntExtra("id", 0);
            User user = data.getParcelableExtra("user");

            UserUtil.getRegDate(id)
                    .map(FoafParser::parse)
                    .map(this::parseTime)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(s -> createRegDialog(user, s),
                            AndroidUtil.handleError(this));
        }
    }

    @OnClick(R.id.header)
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == ID_MESSAGES_STATS) {
            Intent intent = new Intent(this, DialogsChoiceActivity.class);
            intent.putExtra("id", ID_MESSAGES_STATS);
            startActivity(intent);
        } else if (id == ID_FRIENDS_REG_DATE || id == ID_FRIENDS_HIDDEN) {
            Intent intent = new Intent(this, FriendsActivity.class);
            startActivityForResult(intent, id);
        } else if (id == ID_FRIENDS_CONTACTS) {
            Intent intent = new Intent(this, PhonesActivity.class);
            startActivity(intent);
        } else if (id == ID_FRIENDS_DOGS_CLEAN) {
            Intent intent = new Intent(this, FriendsActivity.class);
            intent.putExtra("dogs_clean", true);
            startActivity(intent);
        } else if (id == ID_MESSAGES_CACHE_ALL) {
//            startActivity(new Intent(this, MessagesCacheActivity.class));
        } else if (id == ID_MESSAGES_CHAT_RESTORE) {
            startActivity(new Intent(this, ChatsActivity.class));
        } else if (id == ID_FRIENDS_BIRTHDAY) {
            startActivity(new Intent(this, BirthdayActivity.class));
        } else if (id == ID_FRIENDS_LISTS) {
            startActivity(new Intent(this, FriendListsActivity.class));
        } else if (id == ID_GROUPS_CLEAN) {
            startActivity(new Intent(this, GroupsCleanActivity.class));
        } else if (id == ID_FRIENDS_REQUESTS) {
            startActivity(new Intent(this, FriendsRequestsActivity.class));
        } else if (id == ID_MUSIC_DOWNLOAD) {
            startActivity(new Intent(this, AudiosActivity.class));
        } else if (id == ID_MESSAGES_CLEAN) {
            startActivity(new Intent(this, DialogsCleanActivity.class));
        } else if (id == ID_OTHER_CODE_EXECUTE) {
            startActivity(new Intent(this, RequestsActivity.class));
        } else if (id == ID_PHOTOS_MAR) {
            PhotoMapActivity.start(this);
        } else if (id == ID_FRIENDS_MONITOR) {
            MonitorActivity.start(this);
        }

        if (v.getId() == R.id.header) {
            createUserHeaderDialog();
        }
    }

    // fucking SimpleDateFormat not works :(
    private Date parseTime(String s) {
        // 2009-11-25T19:29:52+03:00
        int year = Integer.valueOf(s.substring(0, 4)) - 1900;
        int month = Integer.valueOf(s.substring(5, 7)) - 1;
        int days = Integer.valueOf(s.substring(8, 10));

        int hours = Integer.valueOf(s.substring(11, 13));
        int mins = Integer.valueOf(s.substring(14, 16));
        int secs = Integer.valueOf(s.substring(17, 19));
        return new Date(year, month, days, hours, mins, secs);
    }

    private void addTextView(CardView root, int res, int id) {
        TextView view = (TextView) inflater.inflate(R.layout.list_item_script, root, false);
        view.setText(res);
        view.setId(id);
        view.setOnClickListener(this);

        ViewGroup group = root.findViewById(R.id.linearCard);
        group.addView(view);
    }

    private void showFirstDialog() {
        if (AppContext.ads) {
            boolean first = SettingsStore.getBoolean("first_launch", true);
            if (first) {
                new WelcomeDialog(this).show();
            }
        }
        SettingsStore.putValue("first_launch", false);
    }

    private void showUpdatesDialog(UpdateChecker.Config config) {
        if (config.build > BuildConfig.VERSION_CODE) {
            new UpdateDialog(this, config).show();
        }
    }

    private CardView inflateAdTemplate(ViewGroup root) {
        if (!AppContext.ads) {
            return null;
        }
        return (CardView) inflater.inflate(R.layout.ad_native_template, root, false);
    }

    private void loadAds() {
        if (AppContext.ads) {
            new Thread(() -> {
                Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);
                AdsManager.init();
                runOnUiThread(() -> {
                    adTemplate = findViewById(R.id.ad_template);

                    AdLoader loader = new AdLoader.Builder(this, AdsManager.nativeId())
                            .forUnifiedNativeAd(ad -> {
                                String store = ad.getStore();
                                String advertiser = ad.getAdvertiser();
                                String headline = ad.getHeadline();
                                String body = ad.getBody();
                                String cta = ad.getCallToAction();
                                Double starRating = ad.getStarRating();
                                NativeAd.Image icon = ad.getIcon();

                                adTemplate.setCallToActionView(adTemplate.findViewById(R.id.ad_container));

                                // Headline Primary
                                TextView headlineView = adTemplate.findViewById(R.id.ad_headline_primary);
                                headlineView.setText(headline);
                                adTemplate.setHeadlineView(headlineView);

                                TextView secondaryView = adTemplate.findViewById(R.id.ad_headline_secondary);
                                String secondaryText;

                                if (AdsManager.adHasOnlyStore(ad)) {
                                    adTemplate.setStoreView(secondaryView);
                                    secondaryText = store;
                                } else if (!TextUtils.isEmpty(advertiser)) {
                                    adTemplate.setAdvertiserView(secondaryView);
                                    secondaryText = advertiser;
                                } else {
                                    secondaryText = "";
                                }
                                LinearLayout startContainer = adTemplate.findViewById(R.id.ad_start_container);
                                TextView startRatingView = adTemplate.findViewById(R.id.ad_start_text);

                                //  Set the secondary view to be the star rating if available.
                                if (starRating != null && starRating > 0) {
                                    startRatingView.setText(String.valueOf(starRating));
                                    adTemplate.setStarRatingView(startRatingView);
                                } else {
                                    secondaryView.setText(secondaryText);
                                    startContainer.setVisibility(GONE);
                                }

                                // Icon
                                ImageView iconView = adTemplate.findViewById(R.id.ad_icon);
                                if (icon != null) {
                                    iconView.setImageDrawable(icon.getDrawable());
                                    adTemplate.setIconView(iconView);
                                } else {
                                    iconView.setVisibility(GONE);
                                }

                                adTemplate.setNativeAd(ad);
                            })
                            .withNativeAdOptions(new NativeAdOptions.Builder()

                                    .build())
                            .build();

                    loader.loadAd(new AdRequest.Builder().build());
                });
            }).start();
        }
    }

    private void includeCards() {
        LinearLayout container = findViewById(R.id.cardContainer);
        CardView friends = ViewUtil.createCardGroup(this, R.string.item_friends);
        CardView messages = ViewUtil.createCardGroup(this, R.string.item_messages);
        CardView photos = ViewUtil.createCardGroup(this, R.string.item_photos);
        CardView music = ViewUtil.createCardGroup(this, R.string.item_audios);
        CardView groups = ViewUtil.createCardGroup(this, R.string.item_groups);
        CardView developers = ViewUtil.createCardGroup(this, R.string.item_developers);


        CardView adCard = inflateAdTemplate(container);
        ViewUtil.addViews(container, music, friends, adCard, messages, photos, groups, developers);

        // FIXME: 2020-01-15 Добавить наконец мониторинг
        addTextView(friends, R.string.friends_contacts, ID_FRIENDS_CONTACTS);
        addTextView(friends, R.string.friends_reg_date, ID_FRIENDS_REG_DATE);
        addTextView(friends, R.string.friends_dogs_clean, ID_FRIENDS_DOGS_CLEAN);
        addTextView(friends, R.string.friends_birthday, ID_FRIENDS_BIRTHDAY);
        addTextView(friends, R.string.friends_lists, ID_FRIENDS_LISTS);
//      addTextView(friends, R.string.item_monitor, ID_FRIENDS_MONITOR);
        addTextView(friends, R.string.friends_requests, ID_FRIENDS_REQUESTS);
        addTextView(messages, R.string.messages_analyze, ID_MESSAGES_STATS);
        addTextView(messages, R.string.item_chats, ID_MESSAGES_CHAT_RESTORE);
        addTextView(messages, R.string.dialogs_clean, ID_MESSAGES_CLEAN);
        if (isGooglePlaySignature()) {
            music.setVisibility(View.GONE);
        } else {
            addTextView(music, R.string.item_audios, ID_MUSIC_DOWNLOAD);
        }

        addTextView(photos, R.string.item_photos_map, ID_PHOTOS_MAR);
        addTextView(groups, R.string.groups_clean, ID_GROUPS_CLEAN);
        addTextView(developers, R.string.item_requests, ID_OTHER_CODE_EXECUTE);
    }

    private boolean isGooglePlaySignature() {
        return AndroidUtil.isGooglePlaySignature(this);
    }

    private void createUserHeaderDialog() {
        AppDatabase.database().users().byId(SettingsStore.getUserId())
                .observeOn(AndroidSchedulers.mainThread())
                .distinctUntilChanged()
                .subscribe(user ->  {
                    String[] items = getResources().getStringArray(R.array.user_header);

                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
                    builder.setTitle(user.toString());
                    builder.setItems(items, (dialog, which) -> {
                        switch (which) {
                            case 0:
                                AndroidUtil.copyText(this, String.valueOf(user.id));
                                break;
                            case 1:
                                AndroidUtil.copyText(this, AndroidUtil.link(user));
                                break;
                            case 2:
                                AndroidUtil.copyText(this, SettingsStore.getAccessToken());
                                break;
                        }
                    });
                    builder.show();
                });
    }

    private void createRegDialog(User user, Date date) {
        if (isDestroyed() || isFinishing()) {
            return;
        }

        YandexMetrica.reportEvent("Дата регистрации друга");
        new RegDateDialog(this, user, getRegMessage(date)).show();
    }

    private String getRegMessage(Date date) {
        DateFormat formatter = SimpleDateFormat.getDateTimeInstance();
        long elapsed = System.currentTimeMillis() - date.getTime();
        long days = TimeUnit.MILLISECONDS.toDays(elapsed);
        long years = days / 356;
        long hours = TimeUnit.MILLISECONDS.toHours(elapsed) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsed) % 60;

        return getString(R.string.friends_reg_date_info,
                formatter.format(date), days, years, hours, minutes);
    }

    @SuppressLint("CheckResult")
    private void updateUser() {
        if (!AndroidUtil.hasConnection()) {
            Toast.makeText(this, R.string.error_connection, Toast.LENGTH_SHORT).show();
            return;
        }

        UserUtil.getUser(SettingsStore.getUserId())
                .subscribe(user -> {
                    setProfile(user);
                    AppDatabase.database().users().insert(user);
                }, AndroidUtil.handleError(this));
    }

    private void startOnlineService() {
        if (SettingsStore.getBoolean(SettingsFragment.KEY_ONLINE)) {
            AndroidUtil.startService(this, new Intent(this, OnlineService.class));
        } else {
            stopService(new Intent(this, OnlineService.class));
        }
    }

    private void setProfile(User user) {
        UserProfile.Builder builder = UserProfile.newBuilder()
                .apply(Attribute.customNumber("id").withValue(user.id))
                .apply(Attribute.name().withValue(user.toString()))
                .apply(Attribute.gender().withValue(user.sex == User.SEX_MALE ?
                        GenderAttribute.Gender.MALE : GenderAttribute.Gender.FEMALE));

        String[] birthday = user.birthday.split("\\.");
        if (birthday.length > 2) {
            int day = Integer.parseInt(birthday[0]);
            int mouth = Integer.parseInt(birthday[1]);
            int year = Integer.parseInt(birthday[2]);

            builder.apply(Attribute.birthDate().withBirthDate(year, mouth, day));
        }
        YandexMetrica.reportUserProfile(builder.build());
    }

    private void checkCrashed() {
        if (SettingsStore.getBoolean("crashed")) {
            SettingsStore.putValue("crashed", false);

            new CrashDialog(this, SettingsStore.getString("crashed_msg")).show();
        }
    }

    private void displayInfo(User user) {
        AndroidUtil.loadImage(userImage, user.photo_50);

        userName.setText(user.toString());
        userScreenName.setText("@id" + user.id);

        ImageView online = findViewById(R.id.user_online_second);
        if (user.online) {
            online.setVisibility(View.VISIBLE);
            online.setImageResource(getOnlineIndicatorResource(user));
        } else {
            online.setVisibility(View.GONE);
        }
    }

    private void checkSignature() {
        if (BuildConfig.DEBUG) {
            return;
        }

        try {
            if (!SignatureChecker.checkGooglePlaySignature(this)
                    && !SignatureChecker.checkReleaseSignature(this)) {
                new AppModifiedDialog(this).show();
                YandexMetrica.reportEvent("Запуск модифицировнной версии");
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void checkLicense() {
        GooglePlayUtil.checkLicense((licensed, purchase) -> {
            String token = SettingsStore.getPurchaseToken();

            if (!TextUtils.isEmpty(token) && !licensed) {
                new CheckLicenseDialog(this).show();
            } else if (licensed) {
                AndroidUtil.refreshPurchaseToken(this, purchase);
            }
        });
    }

    private void suggestJoinTelegram() {
        int launchCount = SettingsStore.getInt("launch_count", 1);

        if (launchCount == 2) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            builder.setTitle(R.string.tg_support_developer);
            builder.setMessage(R.string.tg_support_message);
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.setPositiveButton(R.string.join, (dialog, which)
                    -> {
                AndroidUtil.browse(this, SettingsFragment.LINK_TELEGRAM);
                YandexMetrica.reportEvent("Посмотрел телеграм канал");
            });
            builder.show();
        }
        SettingsStore.putValue("launch_count", ++launchCount);
    }

    private void checkForUpdates() {
        UpdateChecker.config()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::showUpdatesDialog, AndroidUtil.handleError(this));
    }
}

package ru.euphoria.doggy.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.yandex.metrica.YandexMetrica;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ru.euphoria.doggy.MainActivity;
import ru.euphoria.doggy.R;
import ru.euphoria.doggy.api.VKApi;
import ru.euphoria.doggy.common.DebugLog;
import ru.euphoria.doggy.common.DirectLogin;
import ru.euphoria.doggy.data.SettingsStore;
import ru.euphoria.doggy.util.AndroidUtil;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static ru.euphoria.doggy.SettingsFragment.KEY_ONLINE_PLATFORM;

/**
 * Created by admin on 02.05.18.
 */

public class OnlineService extends Service {
    private static final String TAG = "OnlineService";
    private static final int NOTIFICATION_ID = 124257;
    private static final String CHANNEL_ID = "12345678";
    private Disposable subscribe;

    @Override
    public void onCreate() {
        super.onCreate();
        DebugLog.w(TAG, "onCreate");

        YandexMetrica.reportEvent("Вечный онлайн");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        DebugLog.w(TAG, "onStartCommand");

        String token = intent != null ? intent.getStringExtra("token") : null;
        int index = intent != null ? intent.getIntExtra("index", -1) : -1;

        if (subscribe == null) {
            subscribe = Observable.interval(0, 30, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe(count -> setOnline(token), System.err::println);
        }

        createNotification(index);
        return START_STICKY;
    }

    private void createNotification(int index) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    getString(R.string.settings_online), NotificationManager.IMPORTANCE_HIGH);

            channel.setDescription(getString(R.string.settings_online_summary));
            channel.enableLights(false);
            channel.enableVibration(false);
            if (nm != null) {
                nm.createNotificationChannel(channel);
            }
        }

        if (index == -1) {
            index = SettingsStore.getInt(KEY_ONLINE_PLATFORM);
        }
        String platform = index >= 0 ? DirectLogin.platforms.get(index).platform : "";

        DebugLog.w(TAG, "createNotification: " + index + " - " + platform);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setContentTitle(getString(R.string.settings_online));
        builder.setContentText(!TextUtils.isEmpty(platform) ? platform : getString(R.string.works));
        builder.setSmallIcon(R.drawable.ic_vector_vk_dog);
        builder.setWhen(System.currentTimeMillis());
        builder.setOngoing(true);
        builder.setCategory(NotificationCompat.CATEGORY_SERVICE);
        builder.setContentIntent(pendingIntent);

        Notification notification = builder.build();
        startForeground(NOTIFICATION_ID, notification);
    }

    private void setOnline(String token) {
        if (!AndroidUtil.hasConnection()) {
            return;
        }
        try {
            VKApi.messages()
                    .getConversations()
                    .count(5)
                    .accessToken(TextUtils.isEmpty(token) ?
                            SettingsStore.getOnlineAccessToken() : token)
                    .json();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        DebugLog.w(TAG, "onDestroy");

        if (subscribe != null) {
            subscribe.dispose();
            subscribe = null;
        }
    }
}

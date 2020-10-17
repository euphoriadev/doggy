package ru.euphoria.doggy.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Binder;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ServiceCompat;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.yandex.metrica.YandexMetrica;

import java.util.ArrayList;

import ru.euphoria.doggy.AppContext;
import ru.euphoria.doggy.PlayerActivity;
import ru.euphoria.doggy.R;
import ru.euphoria.doggy.api.model.Album;
import ru.euphoria.doggy.api.model.Audio;
import ru.euphoria.doggy.common.DataHolder;
import ru.euphoria.doggy.common.DebugLog;
import ru.euphoria.doggy.data.SettingsStore;
import ru.euphoria.doggy.util.AudioUtil;

import static ru.euphoria.doggy.BuildConfig.APPLICATION_ID;

public class AudioPlayerService extends Service {
    private static final String LOG_TAG = "AudioPlayerService";
    private static final String MEDIA_SESSION_TAG = AppContext.context.getPackageName();
    private static final String PLAYBACK_CHANNEL_ID = APPLICATION_ID + ".player";
    private static final int PLAYBACK_NOTIFICATION_ID = 31000;

    public static void play(Context context, ArrayList<? extends Audio> audios, int position) {
        Intent intent = new Intent(context, AudioPlayerService.class);
        intent.putExtra("position", position);
        DataHolder.setObject("audios", audios);

        // java.lang.RuntimeException: android.os.TransactionTooLargeException: data parcel size
        // intent.putParcelableArrayListExtra("audios", audio);
        Util.startForegroundService(context, intent);
    }

    private Context context;
    private SimpleExoPlayer player;
    private AudioAttributes attrs;
    private DataSource.Factory factory;
    private CacheDataSourceFactory cacheFactory;
    private PlayerNotificationManager notificationManager;
    private MediaSessionCompat mediaSession;
    private MediaSessionConnector sessionConnector;
    private ConcatenatingMediaSource sources;
    private DefaultTrackSelector trackSelector;
    private PlayerNotificationManager.MediaDescriptionAdapter descriptionAdapter;
    private PlayerNotificationManager.NotificationListener notificationListener;
    private LocalBinder binder = new LocalBinder();
    private ArrayList<Audio> audios;
    private int position;

    @Override
    public LocalBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        DebugLog.w(LOG_TAG, "Start service...");
        YandexMetrica.reportEvent("Запуск плеера (service)");

        context = this;
        trackSelector = new DefaultTrackSelector(this);
        attrs = new AudioAttributes.Builder()
                .setContentType(C.CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build();

        player = new SimpleExoPlayer.Builder(this)
                .setTrackSelector(trackSelector)
                .build();
        player.setAudioAttributes(attrs, true);
        player.addListener(new Player.EventListener() {
            @Override
            public void onPlayerError(ExoPlaybackException error) {
                error.printStackTrace();
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();

                if (player.hasNext()) {
                    int nextPosition = position = player.getNextWindowIndex();
                    player.seekTo(nextPosition, 0);
                    player.prepare(sources, false, false);
                }
            }
        });

        factory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, getString(R.string.app_name)));
        sources = new ConcatenatingMediaSource();
        cacheFactory = new CacheDataSourceFactory(AudioUtil.getCache(), factory);

        descriptionAdapter = new PlayerNotificationManager.MediaDescriptionAdapter() {
            @Override
            public String getCurrentContentTitle(Player player) {
                return audios.get(player.getCurrentWindowIndex()).title;
            }

            @Nullable
            @Override
            public PendingIntent createCurrentContentIntent(Player player) {
                Intent starter = new Intent(context, PlayerActivity.class);
                return PendingIntent.getActivity(context, 0, starter,
                        PendingIntent.FLAG_UPDATE_CURRENT);
            }

            @Nullable
            @Override
            public String getCurrentContentText(Player player) {
                return audios.get(player.getCurrentWindowIndex()).artist;
            }

            @Nullable
            @Override
            public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
                Audio audio = audios.get(player.getCurrentWindowIndex());
                String cover = AudioUtil.getCover(audio);
                if (Album.NO_IMAGE.equals(cover)) {
                    return BitmapFactory.decodeResource(getResources(), R.drawable.audio_placeholder);
                }

                if (!TextUtils.isEmpty(cover)) {
                    Picasso.get()
                            .load(cover)
                            .config(Bitmap.Config.ARGB_8888)
                            .into(new Target() {
                                @Override
                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                    AppContext.imageCache.set(cover, bitmap);
                                    callback.onBitmap(bitmap);
                                }

                                @Override
                                public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                                }

                                @Override
                                public void onPrepareLoad(Drawable placeHolderDrawable) {

                                }
                            });
                    return AppContext.imageCache.get(cover);
                }
                return BitmapFactory.decodeResource(getResources(), R.drawable.audio_placeholder);
            }
        };

        notificationListener = new PlayerNotificationManager.NotificationListener() {
            @Override
            public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
                stopSelf();
            }

            @Override
            public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
                if (ongoing) {
                    startForeground(notificationId, notification);
                } else {
                    ServiceCompat.stopForeground(AudioPlayerService.this, 0);
                }
            }
        };

        notificationManager = PlayerNotificationManager.createWithNotificationChannel(
                this, PLAYBACK_CHANNEL_ID, R.string.playback_channel_name,
                0, PLAYBACK_NOTIFICATION_ID,
                descriptionAdapter,
                notificationListener
        );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DebugLog.w(LOG_TAG, "Stop service....");

        notificationManager.setPlayer(null);
        if (mediaSession != null) {
            mediaSession.release();
            sessionConnector.setPlayer(null);
            mediaSession = null;
            sessionConnector = null;
        }
        if (player != null) {
            player.release();
            player = null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public int onStartCommand(Intent data, int flags, int startId) {
        if (data == null) {
            stopSelf();
            return START_NOT_STICKY;
        }
        position = data.getIntExtra("position", 0);
        audios = new ArrayList<>((ArrayList<Audio>) DataHolder.getObject("audios"));

        sources.clear();
        boolean useCache = SettingsStore.useCache();
        for (Audio audio : audios) {
            ProgressiveMediaSource source = new ProgressiveMediaSource.Factory(useCache
                            ? cacheFactory
                            : factory)
                    .setTag(audio.id)
                    .setCustomCacheKey(String.valueOf(audio.id))
                    .createMediaSource(Uri.parse(audio.url));
            sources.addMediaSource(source);
        }

        player.prepare(sources);
        player.seekTo(position, C.INDEX_UNSET);
        player.setPlayWhenReady(true);

        notificationManager.setPlayer(player);

        mediaSession = new MediaSessionCompat(this, MEDIA_SESSION_TAG);
        mediaSession.setActive(true);
        notificationManager.setMediaSessionToken(mediaSession.getSessionToken());

        sessionConnector = new MediaSessionConnector(mediaSession);
        sessionConnector.setQueueNavigator(new TimelineQueueNavigator(mediaSession, 2) {
            @Override
            public MediaDescriptionCompat getMediaDescription(Player player, int windowIndex) {
                Audio audio = audios.get(windowIndex);

                return AudioUtil.getMediaDescriptor(audio);
            }
        });
        sessionConnector.setPlayer(player);

        return START_STICKY;
    }

    public ArrayList<Audio> getAudios() {
        return audios;
    }

    public DefaultTrackSelector getTrackSelector() {
        return trackSelector;
    }

    public ConcatenatingMediaSource getSources() {
        return sources;
    }

    public SimpleExoPlayer getPlayer() {
        return player;
    }

    public class LocalBinder extends Binder {
        public AudioPlayerService getService() {
            return AudioPlayerService.this;
        }
    }

}
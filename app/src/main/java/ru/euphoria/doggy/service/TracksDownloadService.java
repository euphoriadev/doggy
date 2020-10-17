package ru.euphoria.doggy.service;

import android.app.DownloadManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.LongSparseArray;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.yandex.metrica.YandexMetrica;

import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.euphoria.doggy.AppContext;
import ru.euphoria.doggy.api.model.Audio;
import ru.euphoria.doggy.common.DebugLog;
import ru.euphoria.doggy.common.FileDownloader;
import ru.euphoria.doggy.db.AppDatabase;
import ru.euphoria.doggy.util.AndroidUtil;

public class TracksDownloadService extends Service {
    private static final String TAG = "TracksDownloadService";
    public static final String ACTION_DOWNLOAD = "track-downloaded";

    private ExecutorService pool = Executors.newSingleThreadExecutor();
    private LongSparseArray<Integer> downloads = new LongSparseArray<>();
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (id <= 0) {
                return;
            }

            Audio audio = AppDatabase.database().audios().byId(downloads.get(id));
            if (audio == null) {
                return;
            }

            updateTags(audio);
            downloads.remove(id);

            Intent track = new Intent(ACTION_DOWNLOAD);
            track.putExtra("audio_id", audio);
            LocalBroadcastManager.getInstance(AppContext.context).sendBroadcast(track);

            if (downloads.size() == 0) {
                stopSelf();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        DebugLog.w(TAG, "onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        pool.shutdown();

        DebugLog.w(TAG, "onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        DebugLog.w(TAG, "onStartCommand: " + intent);

        if (intent != null) {
            int audioId = intent.getIntExtra("audio_id", -1);
            if (audioId != -1) {
                pool.execute(() -> {
                    Audio audio = AppDatabase.database().audios().byId(audioId);
                    if (audio != null && !TextUtils.isEmpty(audio.url)
                            && audio.url.startsWith("http")) {
                        long referenceId = AndroidUtil.download(this, audio);
                        if (referenceId >= 0) {
                            downloads.append(referenceId, audio.id);
                            YandexMetrica.reportEvent("Скачивание трека");
                        }
                    }
                });
            }
        }
        return START_NOT_STICKY;
    }

    private void updateTags(Audio audio) {
        DebugLog.w(TAG, "updateTags: " + audio);

        File mp3 = FileDownloader.makeFile(Environment.DIRECTORY_MUSIC,
                audio.toString(), "mp3");
        if (!mp3.exists()) {
            return;
        }

        new Thread(() -> {
            try {
                MusicMetadataSet set = new MyID3().read(mp3);
                MusicMetadata meta = new MusicMetadata(audio.toString());
                meta.setArtist(audio.artist);
                meta.setSongTitle(audio.title);

                new MyID3().update(mp3, set, meta);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}

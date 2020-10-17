package ru.euphoria.doggy;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.yandex.metrica.YandexMetrica;

import java.util.ArrayList;

import butterknife.BindView;
import ru.euphoria.doggy.api.model.Album;
import ru.euphoria.doggy.api.model.Audio;
import ru.euphoria.doggy.common.DebugLog;
import ru.euphoria.doggy.service.AudioPlayerService;
import ru.euphoria.doggy.util.AudioUtil;
import ru.euphoria.doggy.util.ViewUtil;

public class PlayerActivity extends BaseActivity implements Player.EventListener, View.OnClickListener {
    private static final String TAG = "PlayerActivity";
    private static final int REQUEST_CODE_STORAGE = 600;

    @BindView(R.id.player) PlayerView playerView;
    @BindView(R.id.exo_artwork) ImageView artwork;
    @BindView(R.id.player_title) TextView title;
    @BindView(R.id.player_subtitle) TextView subtitle;

    private DefaultTrackSelector trackSelector;
    private ConcatenatingMediaSource sources;
    private ServiceConnection connection;
    private ArrayList<Audio> audios;
    private Player player;
    private boolean bound;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        YandexMetrica.reportEvent("Запуск плеера (activity)");

        playerView.showController();
        playerView.setControllerHideOnTouch(false);

        bindPlayerService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bound) {
            unbindService(connection);
            bound = false;
            player.removeListener(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // some
            }
        }
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        DebugLog.w(TAG, "onTracksChanged: " + player.getCurrentWindowIndex());

        Audio song = audios.get(player.getCurrentWindowIndex());
        refreshTrackInfo(song);
    }

    public void onConnected() {
        playerView.setPlayer(player);
        player.addListener(this);

        Audio song = audios.get(player.getCurrentWindowIndex());
        refreshTrackInfo(song);
    }

    private void refreshTrackInfo(Audio song) {
        title.setText(song.title);
        subtitle.setText(song.artist);

        artwork.setOutlineProvider(ViewUtil.ALBUM_OUTLINE_WITH_MARGIN);
        artwork.setClipToOutline(true);

        String cover = AudioUtil.getCover(song);
        if (Album.NO_IMAGE.equals(cover)) {
            artwork.setImageResource(R.drawable.audio_placeholder);
            return;
        }
        if (!TextUtils.isEmpty(cover)) {
            Picasso.get()
                    .load(cover)
                    .config(Bitmap.Config.ARGB_8888)
                    .placeholder(artwork.getDrawable())
                    .into(artwork, new Callback.EmptyCallback() {
                        @Override
                        public void onSuccess() {
                            Picasso.get()
                                    .load(AudioUtil.gteMediumCover(song))
                                    .config(Bitmap.Config.ARGB_8888)
                                    .placeholder(artwork.getDrawable())
                                    .into(artwork);
                        }
                    });
        }
    }

    private void bindPlayerService() {
        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                AudioPlayerService.LocalBinder binder = (AudioPlayerService.LocalBinder) service;
                player = binder.getService().getPlayer();
                sources = binder.getService().getSources();
                trackSelector = binder.getService().getTrackSelector();
                audios = binder.getService().getAudios();
                onConnected();
                bound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                bound = true;
                finish();
            }
        };
        bindService(new Intent(this, AudioPlayerService.class), connection, 0);
    }

}

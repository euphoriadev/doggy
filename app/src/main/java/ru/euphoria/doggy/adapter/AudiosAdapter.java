package ru.euphoria.doggy.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.euphoria.doggy.R;
import ru.euphoria.doggy.api.model.Album;
import ru.euphoria.doggy.api.model.Audio;
import ru.euphoria.doggy.data.SettingsStore;
import ru.euphoria.doggy.util.AudioUtil;
import ru.euphoria.doggy.util.TimeUtil;
import ru.euphoria.doggy.util.ViewUtil;

public class AudiosAdapter extends BaseAdapter<AudiosAdapter.ViewHolder, Audio> {
    public static final int TYPE_DOWNLOADING = 1;
    public static final int TYPE_DOWNLOADED = 2;
    public static final int TYPE_NONE = 3;

    private Handler handler = new Handler(Looper.getMainLooper());
    private HashSet<Integer> ignore = new HashSet<>();
    private String musicFolder = SettingsStore.getMusicFolder();

    public AudiosAdapter(Context context, List<Audio> values) {
        super(context, values);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.list_item_audio,
                parent, false);

        v.setOnClickListener(this);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        holder.overflow.setOnClickListener(v -> {
            if (overflowClickListener != null) {
                overflowClickListener.onClick(holder.overflow, position);
            }
        });

        Audio item = getItem(position);
        String duration = TimeUtil.formatSeconds(item.duration);

        holder.title.setText(item.title);
        holder.summary.setText(MessageFormat.format("{0} • {1}", item.artist, duration));
        holder.check.setVisibility(View.GONE);
        holder.lyrics.setVisibility(item.lyrics_id > 0 ? View.VISIBLE : View.GONE);

        int type = getDownloadType(item);
        switch (type) {
            case TYPE_NONE:
            case TYPE_DOWNLOADING:
                holder.check.setVisibility(View.GONE);
                break;

            case TYPE_DOWNLOADED:
                holder.check.setVisibility(View.VISIBLE);
                break;
        }

        String cover = AudioUtil.getCover(item);
        if (Album.NO_IMAGE.equals(cover)) {
            holder.album.setImageResource(R.drawable.audio_placeholder);
            return;
        }
        if (!TextUtils.isEmpty(cover)) {
            Picasso.get()
                    .load(cover)
                    .config(Bitmap.Config.ARGB_8888)
                    .placeholder(R.drawable.audio_placeholder)
                    .into(holder.album);
        } else {
            holder.album.setImageResource(R.drawable.audio_placeholder);
            if (ignore.contains(item.id)) {
                return;
            }
            ignore.add(item.id);
            AudioUtil.searchAlbum(item, (success) -> {
                ignore.remove(item.id);
                if (success) {
                    handler.post(() -> notifyItemChanged(position));
                }
            });
        }
    }

    @Override
    public boolean onSearchItem(String query, Audio item, int position) {
        String s = item.artist.concat(" ").concat(item.title).toLowerCase();
        return s.contains(query);
    }

    public int getDownloadType(Audio audio) {
        File file = new File(musicFolder, audio.toString() + ".mp3");
        return file.exists() ? TYPE_DOWNLOADED : TYPE_NONE;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.audio_title) TextView title;
        @BindView(R.id.audio_summary) TextView summary;
        @BindView(R.id.overflow) ImageView overflow;
        @BindView(R.id.audio_check) ImageView check;
        @BindView(R.id.audio_lyrics) ImageView lyrics;
        @BindView(R.id.audio_album) ImageView album;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);

            this.album.setOutlineProvider(ViewUtil.ALBUM_OUTLINE);
            this.album.setClipToOutline(true);
        }
    }
}

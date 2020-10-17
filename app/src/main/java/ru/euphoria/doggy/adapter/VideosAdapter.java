package ru.euphoria.doggy.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.MessageFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.euphoria.doggy.R;
import ru.euphoria.doggy.api.model.Video;
import ru.euphoria.doggy.util.TimeUtil;

public class VideosAdapter extends BaseAdapter<VideosAdapter.ViewHolder, Video> {

    public VideosAdapter(Context context, List<Video> values) {
        super(context, values);
    }


    @NonNull
    @Override
    public VideosAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.list_item_video,
                parent, false);

        v.setOnClickListener(this);
        v.setOnLongClickListener(this);
        return new VideosAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        Video item = getItem(position);

        holder.title.setText(item.title);
        holder.views.setText(MessageFormat.format("Views: {0}", item.views));
        holder.duration.setText(TimeUtil.formatSeconds(item.duration));

        if (!TextUtils.isEmpty(item.photo_320)) {
            Picasso.get()
                    .load(item.photo_320)
                    .config(Bitmap.Config.RGB_565)
                    .placeholder(R.drawable.empty_avatar)
                    .into(holder.preview);
        } else {
            holder.preview.setImageDrawable(null);
        }

        holder.overflow.setOnClickListener(v -> {
            if (overflowClickListener != null) {
                overflowClickListener.onClick(holder.overflow, position);
            }
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.video_title) TextView title;
        @BindView(R.id.video_views) TextView views;
        @BindView(R.id.video_duration) TextView duration;
        @BindView(R.id.video_preview) ImageView preview;
        @BindView(R.id.overflow) ImageView overflow;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}

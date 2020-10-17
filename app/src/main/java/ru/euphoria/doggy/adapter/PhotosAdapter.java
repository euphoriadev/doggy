package ru.euphoria.doggy.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import ru.euphoria.doggy.R;
import ru.euphoria.doggy.api.model.Photo;
import ru.euphoria.doggy.api.model.PhotoSizes;
import ru.euphoria.doggy.data.SettingsStore;
import ru.euphoria.doggy.image.BlurTransformation;
import ru.euphoria.doggy.util.AndroidUtil;
import ru.euphoria.doggy.util.ArrayUtil;

/**
 * Created by admin on 24.04.18.
 */

public class PhotosAdapter extends BaseAdapter<PhotosAdapter.ViewHolder, Photo> {
    private ColorDrawable placeholder;
    private int width;
    private int rows;

    public PhotosAdapter(Context context, List<Photo> photos, int rows) {
        super(context, photos);

        this.rows = rows;
        this.width = AndroidUtil.getDisplayWidth(context);
        this.placeholder = new ColorDrawable(SettingsStore.nightMode()
                ? Color.GRAY : Color.LTGRAY);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = getInflater().inflate(R.layout.list_item_photo, parent, false);
        v.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (width / rows)
        ));

        v.setOnClickListener(this);
        v.setOnLongClickListener(this);
        return new ViewHolder(v);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).id;
    }

    public Photo find(long id) {
        for (Photo value : getValues()) {
            if (value.id == id) {
                return value;
            }
        }
        return null;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Photo item = getItem(position);
        if (item == null) return;

        String largeUrl = ArrayUtil.firstNotEmpty(
                item.sizes.of(PhotoSizes.PhotoSize.Z),
                item.sizes.of(PhotoSizes.PhotoSize.YY),
                item.sizes.of(PhotoSizes.PhotoSize.X),
                item.sizes.of(PhotoSizes.PhotoSize.M),
                item.sizes.of(PhotoSizes.PhotoSize.S)
        ).src;


        Picasso.get()
                .load(item.sizes.small().src)
                .placeholder(placeholder)
                .fit()
                .priority(Picasso.Priority.LOW)
                .centerCrop()
                .transform(new BlurTransformation(12))
                .into(holder.image, new Callback.EmptyCallback() {
                    @Override
                    public void onSuccess() {
                        Picasso.get()
                                .load(largeUrl)
                                .priority(Picasso.Priority.NORMAL)
                                .fit()
                                .config(Bitmap.Config.RGB_565)
                                .centerCrop()
                                .placeholder(holder.image.getDrawable())
                                .into(holder.image);
                    }
                });

        holder.overlay.setVisibility(isChecked(position) ? View.VISIBLE : View.GONE);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView image;
        private ImageView overlay;

        public ViewHolder(View v) {
            super(v);
            this.image = v.findViewById(R.id.image);
            this.overlay = v.findViewById(R.id.overlay);
        }
    }
}
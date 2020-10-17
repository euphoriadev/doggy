package ru.euphoria.doggy;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

import androidx.annotation.Nullable;
import androidx.core.widget.ContentLoadingProgressBar;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import io.reactivex.Flowable;
import ru.euphoria.doggy.api.model.Photo;
import ru.euphoria.doggy.api.model.PhotoSizes;
import ru.euphoria.doggy.common.DataHolder;
import ru.euphoria.doggy.image.BlurTransformation;
import ru.euphoria.doggy.photoview.PhotoView;
import ru.euphoria.doggy.util.AndroidUtil;
import ru.euphoria.doggy.util.ArrayUtil;

public class PhotoViewerActivity extends BaseActivity {
    @BindView(R.id.photo) PhotoView view;
    @BindView(R.id.progress) ContentLoadingProgressBar progress;

    private List<PhotoSizes.PhotoSize> sortedSizes;
    private Photo photo;
    private String url;
    private int position;

    public static void start(Context context, Photo photo) {
        Intent starter = new Intent(context, PhotoViewerActivity.class);
        starter.putExtra("photo", photo);
        context.startActivity(starter);
    }

    public static void start(Context context, Photo photo, int position) {
        Intent starter = new Intent(context, PhotoViewerActivity.class);
        starter.putExtra("photo", photo);
        starter.putExtra("position", position);
        context.startActivity(starter);
    }

    public static void start(Context context, String url) {
        Intent starter = new Intent(context, PhotoViewerActivity.class);
        starter.putExtra("url", url);
        context.startActivity(starter);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        photo = getIntent().getParcelableExtra("photo");
        url = getIntent().getStringExtra("url");
        position = getIntent().getIntExtra("position", 0);
        if (DataHolder.getObject() instanceof ArrayList) {
            int max = ((ArrayList<Photo>) DataHolder.getObject()).size();
            getSupportActionBar().setTitle(position + " из " + max);
        }

        progress.show();

        if (photo != null && photo.sizes != null) {
            url = ArrayUtil.firstNotEmpty(
                    photo.sizes.of(PhotoSizes.PhotoSize.W),
                    photo.sizes.of(PhotoSizes.PhotoSize.Z),
                    photo.sizes.of(PhotoSizes.PhotoSize.YY),
                    photo.sizes.of(PhotoSizes.PhotoSize.X),
                    photo.sizes.of(PhotoSizes.PhotoSize.M),
                    photo.sizes.of(PhotoSizes.PhotoSize.S)
            ).src;
            sortedSizes = Flowable.fromIterable(photo.sizes)
                    .sorted((o1, o2) -> Long.compare(o2.width + o2.height, o1.width + o1.height))
                    .toList().blockingGet();

            Bitmap bitmap = AppContext.imageCache.get(url);
            if (bitmap != null) {
                view.setImageBitmap(bitmap);
                progress.hide();
                return;
            }

            String smallUrl = ArrayUtil.last(sortedSizes).src;
            Picasso.get()
                    .load(smallUrl)
                    .transform(new BlurTransformation(2))
                    .into(view, new Callback.EmptyCallback() {
                        @Override
                        public void onSuccess() {
                            Picasso.get()
                                    .load(url)
                                    .placeholder(view.getDrawable())
                                    .priority(Picasso.Priority.HIGH)
                                    .config(Bitmap.Config.ARGB_8888)
                                    .into(view, new EmptyCallback() {
                                        @Override
                                        public void onSuccess() {
                                            progress.hide();
                                        }
                                    });
                        }
                    });
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_photo_viewer, menu);

        MenuItem item = menu.findItem(R.id.item_download);

        if (!photo.has_geo) {
            menu.findItem(R.id.item_open_on_map).setVisible(false);
        }

        if (photo != null) {
            SubMenu subMenu = item.getSubMenu();
            for (PhotoSizes.PhotoSize size : sortedSizes) {
                subMenu.add(Menu.NONE, size.src.hashCode(), 0,
                        size.width + " x " + size.height)
                        .setOnMenuItemClickListener(item1 -> {
                            AndroidUtil.download(this, size);
                            return true;
                        });
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_copy_link:
                AndroidUtil.copyText(this, AndroidUtil.link(photo));
                break;

            case R.id.item_open_on_map:
                AndroidUtil.browse(this, photo.getPosition());

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onChangeTheme() {
        // empty
    }


}

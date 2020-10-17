package ru.euphoria.doggy;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Flowable;
import ru.euphoria.doggy.api.model.Photo;
import ru.euphoria.doggy.common.DataHolder;

public class ClusterPhotosActivity extends BaseActivity {
    private static final String KEY_HOLDER = "photos";

    private int photosSize;

    public static void start(Context context, ArrayList<Photo> photos) {
        DataHolder.setObject(KEY_HOLDER, photos);

        Intent starter = new Intent(context, ClusterPhotosActivity.class);
        starter.putExtra("size", photos.size());
        context.startActivity(starter);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cluster_photos);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        photosSize = getIntent().getIntExtra("size", 0);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, ClusterPhotosFragment.newInstance())
                .commit();

        appendCount();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataHolder.setObject(KEY_HOLDER, null);
    }

    private void appendCount() {
        String title = getResources().getString(R.string.title_photos);
        title += " (" + photosSize + ")";
        getSupportActionBar().setTitle(title);
    }

    @SuppressWarnings("unchecked")
    public static class ClusterPhotosFragment extends PhotosFragment {
        public static ClusterPhotosFragment newInstance() {
            Bundle args = new Bundle();

            ClusterPhotosFragment fragment = new ClusterPhotosFragment();
            fragment.setArguments(args);
            return fragment;
        }

        public ClusterPhotosFragment() {

        }

        @Override
        public Flowable<List<Photo>> getFlowable() {
            ArrayList<Photo> photos = (ArrayList<Photo>) DataHolder.getObject(KEY_HOLDER);
            Collections.sort(photos, Photo.DEFAULT_COMPARATOR);

            return Flowable.just(photos);
        }
    }
}

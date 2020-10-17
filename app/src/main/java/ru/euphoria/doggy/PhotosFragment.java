package ru.euphoria.doggy;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.yandex.metrica.YandexMetrica;

import java.io.File;
import java.util.Collections;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ru.euphoria.doggy.adapter.PhotosAdapter;
import ru.euphoria.doggy.api.model.Photo;
import ru.euphoria.doggy.common.BaseActionMode;
import ru.euphoria.doggy.data.SettingsStore;
import ru.euphoria.doggy.db.AppDatabase;
import ru.euphoria.doggy.util.AndroidUtil;
import ru.euphoria.doggy.util.AttachSaver;
import ru.euphoria.doggy.util.SpacesItemDecoration;

/**
 * Created by admin on 24.04.18.
 */

@SuppressLint("CheckResult")
public class PhotosFragment extends BaseAttachmentsFragment<Photo> {
    private RecyclerView recycler;
    private PhotosAdapter adapter;
    private int quality;
    private BaseActionMode actionMode;

    public static PhotosFragment newInstance(int peer) {
        Bundle args = new Bundle();
        args.putInt("peer", peer);

        PhotosFragment fragment = new PhotosFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_photos, container, false);

        actionMode = new AttachmentActionMode((AppCompatActivity) getActivity());

        int orientation = getResources().getConfiguration().orientation;
        int rows = orientation == Configuration.ORIENTATION_LANDSCAPE ? 6 : 3;

        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), rows);
        SpacesItemDecoration decor = new SpacesItemDecoration(Math.round(AndroidUtil.px(getActivity(), 2)), layoutManager.getSpanCount());

        recycler = root.findViewById(R.id.recycler_view);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(layoutManager);
        recycler.addItemDecoration(decor);

        Disposable subscribe = flowable.subscribe(photos -> {
            adapter = new PhotosAdapter(getActivity(), photos, rows);
            recycler.setAdapter(adapter);

            adapter.setOnClickListener(v -> {
                int position = recycler.getChildAdapterPosition(v);
                actionMode.click(position);

                if (!actionMode.isShowed()) {
                    Photo item = adapter.getItem(position);
                    PhotoViewerActivity.start(getActivity(), item);
                }
            });
            adapter.setLongClickListener(v -> {
                int position = recycler.getChildAdapterPosition(v);
                actionMode.longClick(position);
                return true;
            });
        });
        disposable.add(subscribe);

        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_photos_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.isCheckable()) {
            item.setChecked(true);
        }

        switch (item.getItemId()) {
            case R.id.item_sort_by_default:
                Collections.sort(adapter.getValues(), Photo.DEFAULT_COMPARATOR);
                adapter.notifyDataSetChanged();
                break;

            case R.id.item_sort_by_oldest_date:
                Collections.sort(adapter.getValues(), Photo.DEFAULT_COMPARATOR_REVERSE);
                adapter.notifyDataSetChanged();
                break;

            case R.id.item_filter_all:
                adapter.filter(photo -> true);
                break;

            case R.id.item_filter_my:
                int currentUserId = SettingsStore.getUserId();
                adapter.filter(photo -> photo.owner_id == currentUserId);
                break;

            case R.id.item_filter_geo:
                adapter.filter(photo -> photo.has_geo);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            downloadPhotos(quality);
        }
    }

    @Override
    public Flowable<List<Photo>> getFlowable() {
        return AppDatabase.database().photos()
                .byPeer(getPeerId())
                .observeOn(AndroidSchedulers.mainThread())
                .cache();
    }

    private void snackbarSuccess() {
        File dir = new File(AttachSaver.PHOTOS_DIR.getAbsolutePath(), String.valueOf(getPeerId()));

        Snackbar snackbar = AndroidUtil.snackbar(getActivity(), dir.toString());
        snackbar.setAction(R.string.show, v -> {
            AndroidUtil.openFolder(getActivity(), dir);
        });
        snackbar.show();
    }

    @SuppressLint("ShowToast")
    private void alertChoiceQuality() {
        if (adapter.getItemCount() == 0) {
            getActivity().runOnUiThread(Toast.makeText(getActivity(), R.string.no_photos, Toast.LENGTH_SHORT)::show);
            return;
        }

        String[] items = new String[]{
                "2560 x 2048",
                "1280 x 1024",
                "807 x 807",
                "604 x 604",
                "130 x 130",
                "75 x 75 (Шакалы)",
        };

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        builder.setTitle(R.string.choice_quality);
        builder.setItems(items, (dialog, which) -> downloadPhotos(quality = which));
        builder.show();
    }

    private void downloadPhotos(int quality) {
        if (AndroidUtil.checkStoragePermissions(getActivity())) {
            List<Long> checked = adapter.getChecked();

            ProgressDialog dialog = new ProgressDialog(getActivity());
            dialog.setTitle(R.string.downloading_photos);
            dialog.setMax(checked.size());
            dialog.setProgress(0);
            dialog.setCancelable(false);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.show();

            Flowable.fromIterable(checked)
                    .parallel()
                    .runOn(Schedulers.computation())
                    .flatMap(id -> {
                        Photo photo = adapter.find(id);
                        AttachSaver.save(String.valueOf(getPeerId()), photo, quality);
                        return Flowable.just(photo);
                    })
                    .sequential()
                    .observeOn(AndroidSchedulers.mainThread())
                    .doFinally(() -> {
                        dialog.dismiss();
                        snackbarSuccess();
                        actionMode.finish();

                        YandexMetrica.reportEvent("Скачивание изображений");
                    })
                    .subscribe(photo -> dialog.incrementProgressBy(1), AndroidUtil.handleError(getActivity()));
        }
    }

    private class AttachmentActionMode extends BaseActionMode {
        public AttachmentActionMode(AppCompatActivity activity) {
            super(activity);
        }

        @Override
        public boolean onCreateActionMode(androidx.appcompat.view.ActionMode mode, Menu menu) {
            getActivity().getMenuInflater().inflate(R.menu.action_mode_photos, menu);
            return true;
        }

        @Override
        public boolean onActionItemClicked(androidx.appcompat.view.ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.item_select_all:
                    adapter.checkAll();
                    updateTitle();
                    break;

                case R.id.item_download:
                    alertChoiceQuality();
            }
            return true;
        }

        @Override
        public int getCheckedCount() {
            return adapter.getCheckedCount();
        }

        @Override
        public void onDestroyActionMode(androidx.appcompat.view.ActionMode mode) {
            super.onDestroyActionMode(mode);

            adapter.getChecked().clear();
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onToggleChecked(int position) {
            adapter.toggleChecked(position);
            super.onToggleChecked(position);

        }
    }
}

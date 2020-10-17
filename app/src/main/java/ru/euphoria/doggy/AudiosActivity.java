package ru.euphoria.doggy;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.yandex.metrica.YandexMetrica;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import ru.euphoria.doggy.adapter.AudiosAdapter;
import ru.euphoria.doggy.adapter.WrapLinearLayoutManager;
import ru.euphoria.doggy.api.model.Audio;
import ru.euphoria.doggy.common.HashMultiset;
import ru.euphoria.doggy.common.Pair;
import ru.euphoria.doggy.data.SettingsStore;
import ru.euphoria.doggy.db.AppDatabase;
import ru.euphoria.doggy.service.AudioPlayerService;
import ru.euphoria.doggy.service.TracksDownloadService;
import ru.euphoria.doggy.util.AndroidUtil;
import ru.euphoria.doggy.util.AudioUtil;
import ru.euphoria.doggy.util.ViewUtil;

import static ru.euphoria.doggy.AppContext.context;

@SuppressLint("CheckResult")
public class AudiosActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {
    public static final int REQUEST_CODE_SD_CARD = 100;
    private String api = AndroidUtil.getKateVersionApi();

    private RecyclerView recycler;
    private AudiosAdapter adapter;
    private SwipeRefreshLayout refreshLayout;
    private BroadcastReceiver receiver;
    private BroadcastReceiver progressReceiver;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audios);
        getSupportActionBar().setTitle(R.string.item_audios);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        recycler = findViewById(R.id.recycler_view);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(new WrapLinearLayoutManager(this));

        refreshLayout = findViewById(R.id.refresh_layout);
        refreshLayout.setColorSchemeColors(ViewUtil.swipeRefreshColors(this));
        refreshLayout.setOnRefreshListener(this);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                adapter.notifyDataSetChanged();
            }
        };
        IntentFilter filter = new IntentFilter(TracksDownloadService.ACTION_DOWNLOAD);
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, filter);

        setSubtitle();
        getAudios();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
        LocalBroadcastManager.getInstance(context).unregisterReceiver(progressReceiver);
    }

    @Override
    public void onRefresh() {
        getAudios();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_audios, menu);
        menu.findItem(R.id.item_storage_path).setVisible(false);

        MenuItem itemSearch = menu.findItem(R.id.item_search);

        searchView = (SearchView) itemSearch.getActionView();
        searchView.setQueryHint(getString(R.string.search_audio));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (query == null) {
                    return false;
                }
                if (adapter != null) {
                    adapter.search(query);
                }
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_storage_path:
                showDocumentTreeDialog();
                break;

            case R.id.item_download_more:
                createBulkDownloadDialog();
                break;

            case R.id.item_find_lyrics:
                findLyricsAlert();
                break;

            case R.id.item_audios_size:
                createAudiosSizeDialog();
                break;

            case R.id.item_audio_shuffle:
                Collections.shuffle(adapter.getValues());
                adapter.notifyDataSetChanged();
                break;


            case R.id.item_audio_scroll:
                recycler.scrollToPosition(adapter.getItemCount() - 1);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
            searchView.onActionViewCollapsed();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SD_CARD && resultCode == RESULT_OK
                && takePermission(getApplicationContext(), data.getData())) {
            //do your stuff
        }

    }

    private boolean takePermission(Context context, Uri treeUri) {
        try {
            if (treeUri == null) {
                return false;
            }
            context.getContentResolver().takePersistableUriPermission(treeUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            SettingsStore.putValue("music_folder", treeUri.toString());
            System.out.println("tree: " + treeUri.toString());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    private void showDocumentTreeDialog() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(Intent.createChooser(intent,
                getString(R.string.music_folder)), REQUEST_CODE_SD_CARD);
    }

    private void createBulkDownloadDialog() {
        if (!AndroidUtil.checkStoragePermissions(this)
                || adapter.getValues().isEmpty()) {
            return;
        }

        HashMultiset<String> stats = new HashMultiset<>();
        for (Audio a : adapter.getValues()) {
            stats.add(a.artist);
        }

        List<Pair<String, Integer>> artists = stats.copyByCount();

        ArrayList<String> items = new ArrayList<>();
        items.add("Все треки (" + adapter.getValues().size() + ")");
        for (int i = 0; i < artists.size(); i++) {
            Pair<String, Integer> pair = artists.get(i);
            items.add(pair.first + " (" + pair.second + ") ");
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(R.string.bulk_download);
        builder.setItems(items.toArray(new String[]{}), (dialog, which) -> {
            if (which == 0) {
                bulkDownload(adapter.getValues().size(), audio -> true);
                if (AppContext.ads) {
                    AndroidUtil.loadInterstitialAds(this);
                }
            } else {
                Pair<String, Integer> pair = artists.get(which - 1);
                bulkDownload(pair.second, audio -> audio.artist.toLowerCase()
                        .contains(pair.first.toLowerCase()));
            }
        });
        builder.show();
    }

    private void findLyricsAlert() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(R.string.find_lyrics);
        builder.setMessage(R.string.find_lyrics_message);
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> findLyrics());
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }

    private void findLyrics() {
        ExecutorService executor = Executors.newFixedThreadPool(3);

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle(getString(R.string.finding_lyrics));
        dialog.setMax(adapter.getValues().size());
        dialog.setProgress(0);
        dialog.setCancelable(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setOnDismissListener(d -> executor.shutdown());
        dialog.show();

        for (Audio audio : adapter.getValues()) {
            executor.submit(() -> {
                updateProgress(dialog);
                if (audio.lyrics_id <= 0) {
                    return;
                }

                String text = AudioUtil.findLyrics(audio).blockingGet();
                if (!TextUtils.isEmpty(text)) {
                    AudioUtil.editLyrics(audio, text);
                }
            });
        }
    }


    private void createPickStorageDialog() {
        String[] paths = getResources().getStringArray(R.array.storage_location);
        paths[0] = String.format(paths[0], AndroidUtil.getInternalStorageFreeSize());
        paths[1] = String.format(paths[1], AndroidUtil.getExternalStorageFreeSize());

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(R.string.storage_location);
        builder.setSingleChoiceItems(paths, 0, (dialog, which) -> {

        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.show();
    }

    private void bulkDownload(int count, Predicate<Audio> predicate) {
        ProgressDialog dialog = createProgressDialog(count);
        execute(() -> {
            for (int i = 0; i < adapter.getValues().size(); i++) {
                Audio audio = adapter.getItem(i);
                if (audio == null) {
                    continue;
                }

                try {
                    if (predicate.test(audio) && isCanDownload(audio)) {
                        onDownloadClick(audio);
                        updateProgress(dialog);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    AndroidUtil.toast(this, e.getMessage());
                }
            }
            runOnUiThread(dialog::dismiss);
            YandexMetrica.reportEvent("Массовое скачивание треков");
        });
    }

    private boolean isCanDownload(Audio audio) {
        return !TextUtils.isEmpty(audio.url) && audio.url.startsWith("http")
                && (adapter.getDownloadType(audio) == AudiosAdapter.TYPE_NONE);

    }

    private ProgressDialog createProgressDialog(int max) {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle(getString(R.string.download_enqueue));
        dialog.setMax(max);
        dialog.setProgress(0);
        dialog.setCancelable(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.show();

        return dialog;
    }

    private synchronized void updateProgress(ProgressDialog dialog) {
        runOnUiThread(() -> {
            int progress = dialog.getProgress() + 1;
            if (progress >= dialog.getMax()) {
                dialog.dismiss();
            } else {
                dialog.setProgress(progress);
            }
        });
    }

    private void setSubtitle() {
        int size = 0;
        if (adapter != null && !adapter.getValues().isEmpty()) {
            size = adapter.getValues().size();
        }

        String hours = AndroidUtil.formatSeconds(this, getTotalSeconds());
        String tracks = getResources().getQuantityString(R.plurals.tracks, size, size);
        toolbar.setSubtitle(getString(R.string.audios_subtitle, tracks, hours));
    }

    private int getTotalSeconds() {
        if (adapter == null || adapter.getValues().isEmpty()) {
            return 0;
        }

        int seconds = 0;
        for (int i = 0; i < adapter.getValues().size(); i++) {
            seconds += adapter.getValues().get(i).duration;
        }
        return seconds;
    }

    int current = 0;
    int totalBitrate = 0;
    Disposable subscribe;

    private void createAudiosSizeDialog() {
        if (refreshLayout.isRefreshing()) {
            AndroidUtil.toast(this, "Wait for loading list  ");
            return;
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(R.string.get_audios_size);
        builder.setMessage(getString(R.string.get_audios_size_message,
                0, adapter.getItemCount(), 0));
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setOnDismissListener(dialog -> subscribe.dispose());
        AlertDialog dialog = builder.create();
        dialog.show();

        int seconds = getTotalSeconds();

        subscribe = Flowable.fromIterable(adapter.getValues())
                .flatMap(audio -> AudioUtil.getBitrate(audio).toFlowable())
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bitrate -> {
                    current++;
                    totalBitrate += bitrate;
                    int avg = (totalBitrate / 1000) / current;
                    long result = avg * seconds;
                    String msg = Formatter.formatFileSize(this, result * 128);

                    dialog.setMessage(getString(R.string.get_audios_size_message,
                            current, adapter.getItemCount(), msg));
                }, AndroidUtil.handleError(this));
    }


    @SuppressLint("CheckResult")
    private void getAudios() {
        AppDatabase.database().audios()
                .byOwner(SettingsStore.getUserId())
                .observe(this, this::createAdapter);

        if (!AndroidUtil.hasConnection()
                || SettingsStore.getAudioAccessToken().isEmpty()) {
            return;
        }

        refreshLayout.setRefreshing(true);
        Disposable subscribe = AudioUtil.getAudios()
                .flatMap(audios -> {
                    AppDatabase.database().audios().insert(audios);
                    return Single.just(audios);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(audios -> refreshLayout.setRefreshing(false), AndroidUtil.handleError(this));
        composite.add(subscribe);
    }

    private void onOverflowClick(MenuItem item, Audio audio) {
        switch (item.getItemId()) {
            case R.id.item_download:
                onDownloadClick(audio);
                break;
            case R.id.item_copy_link:
                AndroidUtil.copyText(this, audio.url);
                break;

            case R.id.item_lyrics:
            case R.id.item_find_lyrics:
                startActivity(new Intent(this, LyricsActivity.class)
                        .putExtra("audio", audio));
                break;

            case R.id.item_audios_delete:
                deleteAudio(audio);
                break;

            case R.id.item_bitrate:
                AudioUtil.getBitrate(audio)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(integer -> {
                            String bitrate = getString(R.string.bitrate_per_sec, integer / 1000);
                            String size = Formatter.formatFileSize(this, AudioUtil.getSize(audio.duration, integer));

                            Snackbar snackbar = AndroidUtil.snackbar(this, String.format("%s | %s", bitrate, size));
                            snackbar.setAction(R.string.download, v -> onDownloadClick(audio));
                        }, AndroidUtil.handleError(this));
                break;
        }
    }

    private void deleteAudio(Audio audio) {
        AudioUtil.remove(audio)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> {
                    System.out.println("success: " + success);
                    if (success) {
                        toast("Успешно удалено");
                        AppDatabase.database().audios().delete(audio);
                    }
                }, AndroidUtil.handleError(this));
    }

    private void createAdapter(List<Audio> audios) {
        if (adapter != null) {
            if (audios.size() > adapter.getValues().size()) {
                // добавил новый трек в свой список
                // DiffUtil не смещает скролл на первую позицию,
                // поэтому юзаем notifyDataSetChanged
                adapter.getValues().clear();
                adapter.getValues().addAll(audios);
                adapter.notifyDataSetChanged();
            } else {
                AudiosDiffCallback callback = new AudiosDiffCallback(adapter.getValues(), audios);
                DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback, false);

                adapter.getValues().clear();
                adapter.getValues().addAll(audios);

                result.dispatchUpdatesTo(adapter);
            }
        } else {
            adapter = new AudiosAdapter(this, audios);
            adapter.setOverflowClickListener((v, position) -> {
                Audio audio = adapter.getItem(position);

                createOverflowMenu(v, audio);
            });
            adapter.setOnClickListener(v -> {
                int position = recycler.getChildAdapterPosition(v);
                Audio audio = adapter.getItem(position);

                AudioPlayerService.play(this,
                        (ArrayList<? extends Audio>) adapter.getValues(), position);
            });
            recycler.setAdapter(adapter);
        }
        setSubtitle();
    }

    private void createOverflowMenu(View v, Audio audio) {
        PopupMenu menu = new PopupMenu(this, v);
        menu.inflate(R.menu.menu_audio_overflow);
        menu.getMenu().findItem(R.id.item_lyrics).setVisible(audio.lyrics_id > 0);
        menu.getMenu().findItem(R.id.item_find_lyrics).setVisible(audio.lyrics_id <= 0);
        menu.setOnMenuItemClickListener(item -> {
            onOverflowClick(item, audio);
            return true;
        });
        menu.show();
    }

    private void onDownloadClick(Audio audio) {
        if (!AndroidUtil.checkStoragePermissions(this)) {
            return;
        }

        Intent intent = new Intent(this, TracksDownloadService.class);
        intent.putExtra("audio_id", audio.id);
        startService(intent);
    }

    private static class AudiosDiffCallback extends DiffUtil.Callback {
        private List<Audio> oldList;
        private List<Audio> newList;

        public AudiosDiffCallback(List<Audio> oldList, List<Audio> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            Audio l = oldList.get(oldItemPosition);
            Audio r = newList.get(newItemPosition);
            return l.id == r.id;
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Audio l = oldList.get(oldItemPosition);
            Audio r = newList.get(newItemPosition);
            return l.hashCode() == r.hashCode();
        }
    }
}

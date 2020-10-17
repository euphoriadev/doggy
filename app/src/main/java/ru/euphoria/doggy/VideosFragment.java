package ru.euphoria.doggy;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.PopupMenu;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.HttpUrl;
import ru.euphoria.doggy.adapter.VideosAdapter;
import ru.euphoria.doggy.api.model.Attachments;
import ru.euphoria.doggy.api.model.Video;
import ru.euphoria.doggy.common.Pair;
import ru.euphoria.doggy.db.AppDatabase;
import ru.euphoria.doggy.util.AndroidUtil;
import ru.euphoria.doggy.util.VideoUtil;

@SuppressLint("CheckResult")
public class VideosFragment extends BaseAttachmentsFragment<Video> {
    private static final int ACTION_TYPE_DOWNLOAD = 0;
    private static final int ACTION_TYPE_COPY_LINK = 1;
    private static final int ACTION_TYPE_OPEN = 2;

    private VideosAdapter adapter;

    public static VideosFragment newInstance(int peer) {
        Bundle args = new Bundle();
        args.putInt("peer", peer);

        VideosFragment fragment = new VideosFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        Disposable subscribe = flowable.subscribe(videos -> {
            adapter = new VideosAdapter(getActivity(), videos);
            recycler.setAdapter(adapter);

            adapter.setOnClickListener(v -> {
                int position = recycler.getChildAdapterPosition(v);
                Video item = adapter.getItem(position);
                makeResolutionsDialog(item, ACTION_TYPE_OPEN);
            });

            adapter.setOverflowClickListener((v, position) -> {
                Video item = adapter.getItem(position);
                createOverflowMenu(v, item);
            });
        });
        disposable.add(subscribe);

        return root;
    }

    @Override
    public Flowable<List<Video>> getFlowable() {
        return AppDatabase.database().videos()
                .byPeer(getPeerId())
                .observeOn(AndroidSchedulers.mainThread())
                .cache();
    }

    private void createOverflowMenu(View v, Video video) {
        PopupMenu menu = new PopupMenu(getActivity(), v);
        menu.inflate(R.menu.menu_doc_overflow);
        menu.setOnMenuItemClickListener(item -> onOverflowClick(item, video));
        menu.show();
    }

    public boolean onOverflowClick(MenuItem item, Video video) {
        switch (item.getItemId()) {
            case R.id.item_download:
                makeResolutionsDialog(video, ACTION_TYPE_DOWNLOAD);
                break;

            case R.id.item_copy_link:
                makeResolutionsDialog(video, ACTION_TYPE_COPY_LINK);
                break;
        }
        return true;
    }

    private Single<Video> getVideo(Video source) {
        return AppContext.videos
                .get(source.toAttachmentString()
                        .replace(Attachments.TYPE_VIDEO, ""), 1, 0)
                .map(videos -> videos.get(0))
                .observeOn(AndroidSchedulers.mainThread());
    }

    private void addResolution(ArrayList<Pair<String, String>> list, String name, String resolution) {
        if (!TextUtils.isEmpty(resolution)) {
            if ("external".equals(name)) {
                // превращаем "external" в домен сайта (www.youtube.com)
                HttpUrl url = HttpUrl.parse(resolution);
                if (url != null) {
                    name = url.host();
                }
            }
            list.add(Pair.create(name, resolution));
        }
    }

    private boolean isExternal(Pair<String, String> pair) {
        return !TextUtils.isDigitsOnly(pair.first);
    }

    private void makeResolutionsDialog(Video source, int actionType) {
        getVideo(source).subscribe(video -> {
            ArrayList<Pair<String, String>> list = new ArrayList<>();
            addResolution(list, "240", video.mp4_240);
            addResolution(list, "360", video.mp4_360);
            addResolution(list, "480", video.mp4_480);
            addResolution(list, "720", video.mp4_720);
            addResolution(list, "1080", video.mp4_1080);
            addResolution(list, "external", video.external);
            String[] items = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                Pair<String, String> pair = list.get(i);
                // если external или хост с другого сайта - не вычисляем размер
                // ибо код не сработает, ы
                // 720 - ок, www.youtube.com - не ок
                items[i] = pair.first + (isExternal(pair) ? "" : " • ~");
            }

            AlertDialog dialog = new MaterialAlertDialogBuilder(getActivity())
                    .setTitle(R.string.video_quality)
                    .setItems(items, (delegate, which) -> {
                        Pair<String, String> pair = list.get(which);
                        switch (actionType) {
                            case ACTION_TYPE_OPEN:
                                AndroidUtil.browse(getContext(), pair.second, isExternal(pair) ? "" : "mp4");
                                break;

                            case ACTION_TYPE_DOWNLOAD:
                                AndroidUtil.download(getContext(), pair.second, video.title, "mp4");
                                break;

                            case ACTION_TYPE_COPY_LINK:
                                AndroidUtil.copyText(getContext(), pair.second);
                                break;
                        }
                    })
                    .show();

            for (int i = 0; i < list.size(); i++) {
                Pair<String, String> pair = list.get(i);
                if (isExternal(pair)) {
                    // ссылка ведет не внешний ресурс
                    // вычислять размер нет нужды
                    continue;
                }
                int finalI = i;
                VideoUtil.getVideoBitrate(pair.second)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(bitrate -> {
                                    int size = bitrate / 8 * video.duration;
                                    String formatSize = AndroidUtil.formatSize(size);

                                    items[finalI] = pair.first + " • ~" + formatSize;
                                    ((ArrayAdapter<?>) dialog.getListView().getAdapter()).notifyDataSetChanged();
                                },
                                Throwable::printStackTrace);
            }
        });
    }
}

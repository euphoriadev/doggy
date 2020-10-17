package ru.euphoria.doggy;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.ads.AdView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.yandex.metrica.YandexMetrica;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;
import ru.euphoria.doggy.adapter.DaysMoreAdapter;
import ru.euphoria.doggy.adapter.MembersMoreAdapter;
import ru.euphoria.doggy.adapter.MoreAdapter;
import ru.euphoria.doggy.adapter.WrapLinearLayoutManager;
import ru.euphoria.doggy.ads.AdsManager;
import ru.euphoria.doggy.api.Identifiers;
import ru.euphoria.doggy.api.VKApi;
import ru.euphoria.doggy.api.model.AudioMessage;
import ru.euphoria.doggy.api.model.Chat;
import ru.euphoria.doggy.api.model.Community;
import ru.euphoria.doggy.api.model.Conversation;
import ru.euphoria.doggy.api.model.Message;
import ru.euphoria.doggy.api.model.User;
import ru.euphoria.doggy.common.DataHolder;
import ru.euphoria.doggy.common.DebugLog;
import ru.euphoria.doggy.common.FileChooser;
import ru.euphoria.doggy.common.HashMultiset;
import ru.euphoria.doggy.common.Pair;
import ru.euphoria.doggy.data.SettingsStore;
import ru.euphoria.doggy.db.AppDatabase;
import ru.euphoria.doggy.util.AndroidUtil;
import ru.euphoria.doggy.util.ArrayUtil;
import ru.euphoria.doggy.util.GroupUtil;
import ru.euphoria.doggy.util.MessageStats;
import ru.euphoria.doggy.util.MessageUtil;
import ru.euphoria.doggy.util.UserUtil;
import ru.euphoria.doggy.util.VKUploader;
import ru.euphoria.doggy.util.VKUtil;
import ru.euphoria.doggy.util.ViewUtil;

import static ru.euphoria.doggy.adapter.UsersAdapter.getOnlineIndicatorResource;

@SuppressLint("CheckResult")
public class MessageStatsActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {
    public static final String KEY_HOLDER_STATS = "stats";
    public static final String KEY_EXPANDED_INFO = "stats_expand_info";

    private static final String TAG = "MessageStatsActivity";
    private static final String TAG_MORE = "more";
    private static final int REQUEST_CODE_PICK_VOICE = 100;

    private static final int COUNT_LIMIT = 20;
    private static final int FILE_TYPE_JSON = 0;
    private static final int FILE_TYPE_TEXT = 1;
    private static final int FILE_TYPE_TEXT_EXPANDED = 2;

    @BindView(R.id.user_fullname) TextView userName;
    @BindView(R.id.user_summary) TextView userScreenName;
    @BindView(R.id.user_avatar) ImageView userImage;
    @BindView(R.id.user_online_second) ImageView userOnline;
    @BindView(R.id.user_download) ImageView imageDownload;

    @BindView(R.id.refresh_layout) SwipeRefreshLayout refreshLayout;
    @BindView(R.id.lastOnlineContainer) LinearLayout lastOnlineContainer;
    @BindView(R.id.progressBar) ProgressBar progressBar;
    @BindView(R.id.stats_info) TextView info;

    @BindView(R.id.stats_card_members) CardView cardMembers;
    @BindView(R.id.ststs_table_words) RecyclerView words;
    @BindView(R.id.ststs_table_members) RecyclerView members;
    @BindView(R.id.ststs_table_days) RecyclerView days;
    @BindView(R.id.ststs_table_emoji) RecyclerView smiles;
    @BindView(R.id.adView) AdView adView;

    private int peer, offset;
    private CompositeDisposable disposable = new CompositeDisposable();
    private boolean expandedInfo = SettingsStore.getBoolean(KEY_EXPANDED_INFO);
    private Conversation conversation;
    private MessageStats stats;
    private int[] chatMembers;
    private boolean useDateRange;
    private long rangeStartTime, rangeEndTime;

    private ArrayList<Pair<String, Integer>> sortedWords = new ArrayList<>();
    private ArrayList<Pair<String, Integer>> sortedSmiles = new ArrayList<>();
    private ArrayList<Pair<Integer, Integer>> sortedMembers = new ArrayList<>();
    private ArrayList<Pair<Long, Integer>> sortedDays = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages_analyse);
        getSupportActionBar().setTitle(R.string.messages_analyze);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar.setVisibility(View.VISIBLE);
        refreshLayout.setColorSchemeColors(ViewUtil.swipeRefreshColors(this));
        refreshLayout.setOnRefreshListener(this);

        getIntentData();
        displayHeader();
        getChatMembers();
        analyze();
        loadAds();
    }

    @Override
    public void onRefresh() {
        if (progressBar.getVisibility() == View.GONE) {
            analyze();
        } else {
            refreshLayout.setRefreshing(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_analyze, menu);
        if (peer < VKApi.PEER_OFFSET) {
            menu.findItem(R.id.item_members).setVisible(false);
        }

        if (!BuildConfig.DEBUG) {
            menu.findItem(R.id.item_test).setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if ((item.getItemId() != R.id.item_members)
                && progressBar.getVisibility() == View.VISIBLE) {
            Toast.makeText(this, R.string.error_analyze_processing, Toast.LENGTH_LONG)
                    .show();
            return super.onOptionsItemSelected(item);
        }
        Intent intent = null;
        switch (item.getItemId()) {
            case R.id.item_attachments:
                intent = new Intent(this, AttachmentsActivity.class);
                break;

            case R.id.item_chart:
                intent = new Intent(this, MessageGraphActivity.class);
                DataHolder.setObject(KEY_HOLDER_STATS, stats);
                break;

            case R.id.item_members:
                intent = new Intent(this, MembersActivity.class);
                break;

            case R.id.item_ignore_list:
                intent = new Intent(this, MessageIgnoreList.class);
                break;

            case R.id.item_test:
                intent = new Intent(this, TestActivity.class);
                break;

            case R.id.item_share:
                createShareStatsDialog();
                break;

            case R.id.item_pick_range:
                createDateRangeDialog();
                break;

            case R.id.item_send_voice:
                if (AndroidUtil.checkStoragePermissions(this)) {
                    AndroidUtil.pickAudio(this, REQUEST_CODE_PICK_VOICE);
                }
                break;
        }
        if (intent != null) {
            intent.putExtra("peer", peer);
            intent.putExtra("title", userName.getText().toString());
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_PICK_VOICE) {
            String path = FileChooser.getPath(this, data.getData());
            DebugLog.w(TAG, "path: " + path);

            if (!TextUtils.isEmpty(path)) {
                File file = new File(path);
                if (file.exists()) {
                    sendVoice(file);
                } else {
                    toast("Файл не найден");
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stats.clear();
        disposable.dispose();
        DataHolder.setObject(KEY_HOLDER_STATS, null);
        System.gc();
    }


    @OnClick(R.id.img_expand) void onExpandClick(View v) {
        expandedInfo = !expandedInfo;
        updateInfo(false);

        SettingsStore.putValue(KEY_EXPANDED_INFO, expandedInfo);
    }

    @OnClick(R.id.user_download) void createDownloadDialog(View v) {
        if (!AndroidUtil.checkStoragePermissions(this)) {
            return;
        }
        String[] items = getResources().getStringArray(R.array.dialog_extensions);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(R.string.save_chat);
        builder.setItems(items, (dialog, which) -> makeHistoryFile(which));
        builder.show();
    }


    private void analyze() {
        stats = AppDatabase.database().messageStats().byPeer(peer);
        if (stats == null) {
            stats = new MessageStats(peer);
        }
        offset = stats.processed;
        updateSortedLists();
        updateInfo(true);

        analyzeCached();
    }

    private void analyzeCached() {
        Disposable subscribe = cachedMessages()
                .subscribeOn(Schedulers.io())
                .buffer(10_000)
                .parallel()
                .runOn(Schedulers.computation())
                .doOnNext(messages -> {
                    DebugLog.i(TAG, "cachedMessages.doOnNext: messages size " + messages.size());
                    stats.addWords(messages, false);
                    updateSortedLists();
                })
                .sequential()
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(this::analyzeNetwork)
                .subscribe(messages -> {
                    DebugLog.i(TAG, "cachedMessages.subscribe: messages size " + messages.size());
                    updateInfo(true);
                });
        disposable.add(subscribe);
    }

    private void analyzeNetwork() {
        Action actionFinally = () -> {
            refreshLayout.setRefreshing(false);
            progressBar.setVisibility(View.GONE);
            imageDownload.setVisibility(View.VISIBLE);
            addButtonsMore();
            YandexMetrica.reportEvent("Анализ беседы");
        };

        if (!AndroidUtil.hasConnection()) {
            AndroidUtil.toastErrorConnection(this);
            try {
                actionFinally.run();
            } catch (Exception ignored) {}
            return;
        }

        MessageUtil.getHistoryCount(peer)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(count -> {
                    stats.total = count;
                    updateInfo(true);

                    getHistory(false, actionFinally);
                }, AndroidUtil.handleError(this));

    }

    private void getHistory(boolean slow, Action actionFinally) {
        Flowable<ArrayList<Message>> history = slow
                ? VKUtil.getAllSlowHistory(peer, offset)
                : VKUtil.getAllHistory(peer, offset);

        Disposable subscribe = history
                .subscribeOn(Schedulers.io())
                .parallel()
                .runOn(Schedulers.computation())
                .flatMap(messages -> {
                    DebugLog.i(TAG, "networkMessages.flatMap: messages size " + messages.size());
                    saveUsersAndGroups(messages);
                    stats.add(messages, true);
                    updateSortedLists();
                    return Flowable.just(messages);
                })
                .doAfterNext(messages -> {
                    DebugLog.i(TAG, "networkMessages.doAfterNext: messages size " + messages.size());

                    AppDatabase.database().messages().insertWithAttachments(messages, peer);
                    AppDatabase.database().messageStats().insert(stats);
                })
                .sequential()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(actionFinally)
                .subscribe(messages -> {

                    DebugLog.i(TAG, "networkMessages.subscribe: messages size " + messages.size());
                    updateInfo(true);
                }, error -> {
                    if (error.getMessage().contains("response size is too big")) {
                        AndroidUtil.toast(MessageStatsActivity.this,
                                "Размер ответа слишком большой, пробуем грузить маленькими пачками");
                        getHistory(true, actionFinally);
                    } else {
                        AndroidUtil.handleError(this, error);
                    }
                });
        disposable.add(subscribe);
    }

    private void getIntentData() {
        conversation = getIntent().getParcelableExtra("conversation");
        peer = getIntent().getIntExtra("peer_id", 0);
    }

    private void updateInfo(boolean refreshLines) {
        String text;
        DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        String firstTime = format.format(stats.firstTime * 1000);
        String lastTime = format.format(stats.lastTime * 1000);

        if (expandedInfo) {
            int pTotal = 100;
            float pOut = (float) stats.out / stats.processed * 100;
            float pIn = (float) stats.in / stats.processed * 100;
            pOut = Float.isNaN(pOut) ? 0 : pOut;
            pIn = Float.isNaN(pIn) ? 0 : pIn;

            String docsSize = AndroidUtil.formatSize(stats.docsSize);
            String audiosDuration = AndroidUtil.formatSeconds(this, stats.audiosDuration);
            String voicesDuration = AndroidUtil.formatSeconds(this, stats.voicesDuration);
            String callsDuration = AndroidUtil.formatSeconds(this, stats.callsDuration);

            text = getString(R.string.messages_stats_info_expanded,
                    stats.processed, stats.total, pTotal,
                    firstTime, lastTime,
                    stats.out, pOut,
                    stats.in, pIn,
                    stats.forwards, stats.countWords, stats.countChars,
                    stats.fucks,

                    stats.attachments,
                    stats.photos, stats.geoPoints,
                    stats.audios, audiosDuration,
                    stats.videos,
                    stats.docs, docsSize,
                    stats.voices, voicesDuration,
                    stats.calls, callsDuration,
                    stats.walls, stats.stickers, stats.gifts,
                    stats.links);
        } else {
            text = getString(R.string.messages_stats_info,
                    stats.processed, stats.total,
                    firstTime, lastTime,
                    stats.out, stats.in, stats.forwards, stats.countWords, stats.countChars,
                    stats.fucks,

                    stats.attachments, stats.photos, stats.audios, stats.videos,
                    stats.docs, stats.voices, stats.calls, stats.walls, stats.stickers, stats.gifts,
                    stats.links);
        }
        info.setText(Html.fromHtml(text));

        if (stats != null && refreshLines) {
            displayAdapters();
        }
    }

    private void displayHeader() {
        String summary = "@id" + peer;
        userScreenName.setText(summary);
        lastOnlineContainer.setVisibility(View.GONE);

        switch (conversation.peer.type) {
            case Conversation.Peer.TYPE_USER:
                User user = UserUtil.getCachedUser(conversation.peer.local_id);
                userName.setText(user.toString());
                AndroidUtil.loadImage(userImage, user.photo_50);

                cardMembers.setVisibility(View.GONE);
                if (user.online) {
                    userOnline.setVisibility(View.VISIBLE);
                    userOnline.setImageResource(getOnlineIndicatorResource(user));
                    userOnline.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.online)));
                } else {
                    userOnline.setVisibility(View.GONE);
                }

                if (user.online_mobile) {
                    String app = Identifiers.toString(user.online_app);
                    if (!app.isEmpty()) {
                        userScreenName.append(MessageFormat.format(" \t({0})", app));
                    } else if (AndroidUtil.hasConnection()){
                        AppContext.apps.get(user.online_app)
                                .observeOn(AndroidSchedulers.mainThread())
                                .map(apps -> apps.get(0))
                                .subscribe(value -> {
                                    userScreenName.append(MessageFormat
                                            .format(" \t({0})", value.title));
                                });
                    }
                }
                break;
            case Conversation.Peer.TYPE_CHAT:
                userName.setText(conversation.chat_settings.title);
                AndroidUtil.loadImage(userImage, conversation.chat_settings.photo.photo_50);
                break;
            case Conversation.Peer.TYPE_GROUP:
                Community community = AppDatabase.database().groups().byId(conversation.peer.local_id);
                userName.setText(community.name);
                AndroidUtil.loadImage(userImage, community.photo_50);
                break;
        }
        ((ViewGroup) userScreenName.getParent()).setOnClickListener(this::createHeaderOptionsDialog);
    }

    private void createDateRangeDialog() {
        androidx.core.util.Pair<Long, Long> range = androidx.core.util.Pair.create(
                stats.firstTime * 1000L,
                stats.lastTime * 1000L
        );

        MaterialDatePicker<androidx.core.util.Pair<Long, Long>> picker =
                MaterialDatePicker.Builder
                        .dateRangePicker()
                        .setTheme(R.style.MaterialCalendarTheme)
                        .setTitleText("Выберите диапазон дат")
                        .setSelection(range)
                        .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            useDateRange = true;
            rangeStartTime = selection.first;
            rangeEndTime = selection.second;
            analyze();
        });
        picker.show(getSupportFragmentManager(), picker.toString());
    }

    private void createShareStatsDialog() {
        String[] items = new String[]{
                getString(R.string.messages_stats),
                getString(R.string.messages_members),
                getString(R.string.messages_top_words),
                getString(R.string.messages_top_days)
        };
        boolean[] checked = new boolean[4];
        Arrays.fill(checked, true);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(R.string.stats_share);
        builder.setMultiChoiceItems(items, checked, (dialog, which, isChecked)
                -> checked[which] = isChecked);
        builder.setPositiveButton(android.R.string.ok, (dialog, which)
                -> shareStats(items, checked));
        builder.show();
    }

    private void createHeaderOptionsDialog(View v) {
        String[] items = getResources().getStringArray(R.array.dialog_header);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setItems(items, (dialog, which) -> {
            if (which == 0) {
                AndroidUtil.copyText(this, "https://vk.com/id" + peer);
            }
        });
        builder.show();
    }

    @SuppressWarnings("unchecked")
    private void createMoreAdapter(RecyclerView recycler) {
        if (recycler.getAdapter() == null) {
            LinearLayoutManager layoutManager = new WrapLinearLayoutManager(this);
            layoutManager.setStackFromEnd(true);

            recycler.setNestedScrollingEnabled(false);
            recycler.setHasFixedSize(true);
            recycler.setLayoutManager(layoutManager);
        }

        MoreAdapter adapter = null;
        if (recycler == days) {
            adapter = new DaysMoreAdapter(this, sortedDays);
        } else if (recycler == members) {
            adapter = new MembersMoreAdapter(this, sortedMembers, chatMembers);
        } else if (recycler == words) {
            adapter = new MoreAdapter(this, sortedWords);
        } else if (recycler == smiles) {
            adapter = new MoreAdapter(this, sortedSmiles);
        }

        recycler.setAdapter(adapter);
    }

    private void displayAdapters() {
        createMoreAdapter(words);
        createMoreAdapter(smiles);
        createMoreAdapter(members);
        createMoreAdapter(days);
    }

    private void updateSortedLists() {
        sortedWords = ArrayUtil.limit(stats.words().copyByCount(), COUNT_LIMIT);
        sortedSmiles = ArrayUtil.limit(stats.smiles().copyByCount(), COUNT_LIMIT);
        sortedMembers = ArrayUtil.limit(stats.members().copyByCount(), COUNT_LIMIT);
        sortedDays = ArrayUtil.limit(stats.days().copyByCount(), COUNT_LIMIT);
    }

    private void addButtonsMore() {
        addButtonMore(stats.words(), words, MessageStats.TYPE_WORDS);
        addButtonMore(stats.smiles(), smiles, MessageStats.TYPE_SMILES);
        addButtonMore(stats.members(), members, MessageStats.TYPE_MEMBERS);
        addButtonMore(stats.days(), days, MessageStats.TYPE_DAYS);
    }

    private void addButtonMore(HashMultiset data, RecyclerView recycler, String type) {
        if (data.size() > COUNT_LIMIT) {
            addButtonMore(recycler, type);
        }
        ((ViewGroup )recycler.getParent()).setOnClickListener(v -> onMoreClick(type));
    }

    private void addButtonMore(ViewGroup container, String type) {
        ViewGroup parent = (ViewGroup) container.getParent();
        Button child = createButtonMore(container, type);
        if (parent.findViewWithTag(TAG_MORE) == null) {
            parent.addView(child);
        }
    }

    private Button createButtonMore(ViewGroup root, String type) {
        View button = LayoutInflater.from(this).inflate(R.layout.button_more, root,
                false);
        button.setTag(TAG_MORE);

        button.setOnClickListener(v -> onMoreClick(type));
        return (Button) button;
    }

    void onMoreClick(String type) {
        Intent intent = new Intent(this, MoreActivity.class);
        intent.putExtra("type", type);
        intent.putExtra("peer", peer);

        switch (type) {
            case MessageStats.TYPE_WORDS:
                DataHolder.setObject(stats.words().copyByCount());
                intent.putExtra("title", getString(R.string.messages_top_words));
                break;
            case MessageStats.TYPE_MEMBERS:
                DataHolder.setObject(stats.members().copyByCount());
                intent.putExtra("title", getString(R.string.messages_members));
                intent.putExtra("active_members", chatMembers);
                break;
            case MessageStats.TYPE_SMILES:
                DataHolder.setObject(stats.smiles().copyByCount());
                intent.putExtra("title", getString(R.string.msg_top_emoji));
                break;
            case MessageStats.TYPE_DAYS:
                DataHolder.setObject(stats.days().copyByCount());
                intent.putExtra("title", getString(R.string.messages_top_days));
                break;
        }
        startActivity(intent);
    }


    private void saveUsersAndGroups(List<Message> messages) {
        if (peer < VKApi.PEER_OFFSET) return;

        HashSet<Integer> userIds = new HashSet<>();
        HashSet<Integer> groupIds = new HashSet<>();

        for (Message msg : messages) {
            if (msg.from_id > 0) {
                User user = UserUtil.getCachedUser(msg.from_id);
                if (user == null) {
                    userIds.add(msg.from_id);
                }
            } else {
                Community group = GroupUtil.getCachedGroup(Math.abs(msg.from_id));
                if (group == null) {
                    groupIds.add(Math.abs(msg.from_id));
                }
            }
        }

        if (!userIds.isEmpty()) {
            int[] ids = ArrayUtil.toInts(userIds);
            ArrayList<User> users = UserUtil.getUsers(ids).blockingGet();
            AppDatabase.database().users().insert(users);
        }

        if (!groupIds.isEmpty()) {
            int[] ids = ArrayUtil.toInts(groupIds);
            ArrayList<Community> groups = GroupUtil.getGroups(ids).blockingGet();
            AppDatabase.database().groups().insert(groups);
        }
    }

    private void getChatMembers() {
        if (peer < VKApi.PEER_OFFSET) return;

        VKApi.messages()
                .getChat()
                .chatId(peer - VKApi.PEER_OFFSET)
                .asyncSingle(Chat.class)
                .filter(chat -> AndroidUtil.nonNull(chat.users))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(chat -> {
                    chatMembers = chat.users;
                    RecyclerView.Adapter adapter = members.getAdapter();
                    if (adapter instanceof MembersMoreAdapter) {
                        ((MembersMoreAdapter) adapter).setActiveMembers(chatMembers);
                    }
                });

    }

    private Cursor cachedMessagesCursor() {
        if (useDateRange) {
            return AppDatabase.database().messages().cursorByPeer(peer,
                    rangeStartTime / 1000, rangeEndTime / 1000);
        }
        return AppDatabase.database().messages().cursorByPeer(peer);
    }

    private Flowable<Message> cachedMessages() {
        return Flowable.create(emitter -> {
            try (Cursor cursor = cachedMessagesCursor()) {
                int fromId = cursor.getColumnIndex("id");
                int fromIndex = cursor.getColumnIndex("from_id");
                int peerIndex = cursor.getColumnIndex("peer_id");
                int bodyIndex = cursor.getColumnIndex("text");
                int dateIndex = cursor.getColumnIndex("date");
                int emojiIndex = cursor.getColumnIndex("emoji");

                while (cursor.moveToNext()) {
                    Message msg = new Message();
                    msg.id = cursor.getInt(fromId);
                    msg.from_id = cursor.getInt(fromIndex);
                    msg.peer_id = cursor.getInt(peerIndex);
                    msg.date = cursor.getInt(dateIndex);
                    msg.text = cursor.getString(bodyIndex);
                    msg.emoji = cursor.getInt(emojiIndex) == 1;

                    emitter.onNext(msg);
                }
            } catch (Exception e) {
                emitter.onError(e);
            } finally {
                emitter.onComplete();
            }
        }, BackpressureStrategy.BUFFER);
    }

    private void loadAds() {
        if (AppContext.ads) {
            AdsManager.showBanner(adView);
        } else {
            adView.setVisibility(View.GONE);
            adView.destroy();
        }
    }

    private void sendVoice(File file) {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle(file.getName());
        dialog.setCancelable(false);
        dialog.setMessage(getString(R.string.uploading_file));
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();

        Random random = new Random();
        VKUploader.audioMessage(file, peer)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(throwable -> dialog.dismiss())
                .subscribe(response -> {
                    dialog.setMessage(getString(R.string.sending));
                    AudioMessage msg = VKUtil.from(AudioMessage.class, response).get(0);

                    AppContext.messages.send(peer, random.nextInt(), "", msg.toAttachmentString())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doFinally(dialog::dismiss)
                            .subscribe(integer -> {
                                        YandexMetrica.reportEvent("Отправка аудио как голос");
                                        toast("Голосовое сообщение отправлено");
                                    },
                                    AndroidUtil.handleError(this));
                }, AndroidUtil.handleError(this));
    }

    private void makeHistoryFile(int type) {
        MessageStatsActivity.MessageSaver msgSaver = null;
        switch (type) {
            case FILE_TYPE_TEXT:
                msgSaver = new MessageStatsActivity.TextMessageSaver();
                break;
            case FILE_TYPE_TEXT_EXPANDED:
                msgSaver = new MessageStatsActivity.ExpandedTextMessageSaver();
                break;
            case FILE_TYPE_JSON:
                msgSaver = new MessageStatsActivity.JsonMessageSaver();
                break;
        }
        Objects.requireNonNull(msgSaver).save();
    }

    @SuppressLint("CheckResult")
    private void shareStats(String[] items, boolean[] checked) {
        StringBuilder message = new StringBuilder();

        // Message total stats
        if (checked[0]) {
            message.append(info.getText().toString());
            message.append("\n\n");
        }

        // Members
        if (checked[1]) {
            message.append(items[1]);
            message.append("\n");
            List<Pair<Integer, Integer>> members = stats.members().copyByCount();
            for (int i = 0; i < Math.min(COUNT_LIMIT, members.size()); i++) {
                Pair<Integer, Integer> member = members.get(i);
                String name;
                name = member.first < 0
                        ? AppDatabase.database().groups().byId(Math.abs(member.first)).name
                        : UserUtil.getCachedUser(member.first).toString();

                message.append(i + 1).append(". ");
                message.append(name).append(": ");
                message.append(member.second);
                message.append("\n");
            }
            message.append("\n\n");
        }

        // Top Words
        if (checked[2]) {
            message.append(items[2]);
            message.append("\n");
            List<Pair<String, Integer>> words = stats.words().copyByCount();
            for (int i = 0; i < Math.min(COUNT_LIMIT, words.size()); i++) {
                Pair<String, Integer> word = words.get(i);
                message.append(i + 1).append(". ");
                message.append(word.first).append(": ");
                message.append(word.second);
                message.append("\n");
            }
            message.append("\n\n");
        }

        // Top Days
        if (checked[3]) {
            message.append(items[3]);
            message.append("\n");
            DateFormat sdf = DateFormat.getDateInstance();
            List<Pair<Long, Integer>> days = stats.days().copyByCount();
            for (int i = 0; i < Math.min(COUNT_LIMIT, days.size()); i++) {
                Pair<Long, Integer> day = days.get(i);
                message.append(i + 1).append(". ");
                message.append(sdf.format(TimeUnit.DAYS.toMillis(day.first))).append(": ");
                message.append(day.second);
                message.append("\n");
            }
            message.append("\n\n");
        }

        if (AppContext.ads) {
            message.append("\nPowered by Doggy Scripts");
        }

        MessageUtil.sendMessage(peer, message.toString())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    Toast.makeText(this, R.string.success, Toast.LENGTH_LONG)
                            .show();

                    YandexMetrica.reportEvent("Отправка анализа в беседу");
                }, AndroidUtil.handleError(this));
    }

    public abstract class MessageSaver {
        public abstract String ext();

        public abstract void write(Writer writer, Message msg) throws Exception;

        public File file() {
            return new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS),
                    userName.getText()
                            .toString()
                            .replaceAll("//", "")
                            + ext());
        }

        public Writer writer() {
            try {
                return new BufferedWriter(new FileWriter(file()), 1024 * 32);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        public ProgressDialog dialog() {
            ProgressDialog dialog = new ProgressDialog(MessageStatsActivity.this);
            dialog.setTitle(R.string.save_chat);
            dialog.setMax(stats.total);
            dialog.setProgress(0);
            dialog.setCancelable(false);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.show();
            return dialog;
        }

        public void save() {
            ProgressDialog dialog = dialog();
            Writer writer = writer();
            if (writer == null) {
                return;
            }

            cachedMessages()
                    .subscribeOn(Schedulers.io())
                    .doOnNext(msg -> {
                        write(writer, msg);
                    })
                    .doOnComplete(() -> {
                        dialog.dismiss();

                        Snackbar snackbar = AndroidUtil.snackbar(MessageStatsActivity.this, getString(R.string.file_saved_in) + file().toString(), Snackbar.LENGTH_INDEFINITE);
                        snackbar.setAction(R.string.show, v -> {
                            AndroidUtil.openFolder(MessageStatsActivity.this, file().getParentFile());
                        }).show();

                        writer.close();

                    })
                    .buffer(100)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(messages -> dialog.incrementProgressBy(100), AndroidUtil.handleError(MessageStatsActivity.this));
        }
    }

    public class ExpandedTextMessageSaver extends MessageSaver {
        private DateFormat dateFormat = DateFormat.getDateTimeInstance();
        private String me = UserUtil.me().toString();

        @Override
        public String ext() {
            return " (exp).txt";
        }

        @Override
        public void write(Writer writer, Message msg) throws Exception {
            String owner = msg.from_id < 0
                    ? GroupUtil.getCachedGroup(Math.abs(msg.from_id)).toString()
                    : UserUtil.getCachedUser(msg.from_id).toString();

            writer.append(msg.is_out ? me : owner);
            writer.append(" (")
                    .append(dateFormat.format(msg.date * 1000L))
                    .append(") \n");
            writer.append(msg.text);
            writer.append("\n\n");
        }
    }

    public class TextMessageSaver extends MessageSaver {
        @Override
        public String ext() {
            return ".txt";
        }

        @Override
        public void write(Writer writer, Message msg) throws Exception {
            if (msg.is_out) {
                writer.append("Я: ").append(msg.text);
            } else {
                String owner = msg.from_id < 0
                        ? GroupUtil.getCachedGroup(Math.abs(msg.from_id)).toString()
                        : UserUtil.getCachedUser(msg.from_id).toString();

                writer.append(owner)
                        .append(": ")
                        .append(msg.text);
            }
            writer.append("\n");
        }
    }

    public class JsonMessageSaver extends MessageSaver {

        @Override
        public String ext() {
            return ".json";
        }

        @Override
        public void write(Writer writer, Message msg) throws Exception {
            String json = msg.toJson().toString(4);
            writer.append(json);
            writer.append(",\n ");
        }
    }


}


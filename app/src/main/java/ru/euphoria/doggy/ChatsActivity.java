package ru.euphoria.doggy;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import ru.euphoria.doggy.adapter.ChatsPagerAdapter;
import ru.euphoria.doggy.api.VKException;
import ru.euphoria.doggy.api.model.Chat;
import ru.euphoria.doggy.api.model.Conversation;
import ru.euphoria.doggy.data.SettingsStore;
import ru.euphoria.doggy.util.ArrayUtil;
import ru.euphoria.doggy.util.MessageUtil;

@SuppressWarnings("CheckResult")
public class ChatsActivity extends BaseActivity {
    private ChatsPagerAdapter adapter;
    private TabLayout tabs;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);
        getSupportActionBar().setTitle(R.string.item_chats);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adapter = new ChatsPagerAdapter(getSupportFragmentManager());
        ViewPager pager = findViewById(R.id.pager);
        pager.setAdapter(adapter);

        tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(pager);

        getChats(1000);
        alertInfo();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem itemSearch = menu.findItem(R.id.item_search);

        searchView = (SearchView) itemSearch.getActionView();
        searchView.setQueryHint("Search Chats");
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

    private int[] generateIds(int max) {
        int[] array = new int[max];
        for (int i = 0; i < max; i++) {
            array[i] = i + 1;
        }
        return array;
    }

    private int[] toChatIds(ArrayList<Chat> chats) {
        ArrayList<Integer> ids = new ArrayList<>(chats.size());
        for (Chat chat : chats) {
            ids.add(chat.id);
        }

        return ArrayUtil.toInts(ids);
    }

    private int[] toConversationIds(ArrayList<Conversation> conversations) {
        ArrayList<Integer> ids = new ArrayList<>(conversations.size());
        for (Conversation c : conversations) {
            ids.add(c.peer.local_id);
        }

        return ArrayUtil.toInts(ids);
    }

    private void getChats(int max) {
        MessageUtil.getChats(generateIds(max))
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(conversations -> {
                    Collections.sort(conversations, (o1, o2)
                            -> Integer.compare(o2.id, o1.id));
                    return Single.just(conversations);
                })
                .subscribe(chats -> {
                            adapter.refresh(chats);
                            String title = getResources().getQuantityString(R.plurals.tab_chats_all, chats.size(), chats.size());
                            tabs.getTabAt(0).setText(title);

                            getConversations(chats);

                        },
                        error -> {
                            if (error instanceof VKException) {
                                VKException ex = (VKException) error;
                                if (ex.code == 100 && ex.message.contains("chat_id param is incorrect")) {
                                    String[] words = ex.message.split(" ");
                                    int count = Integer.parseInt(words[words.length - 1]);
                                    getChats(count - 1);
                                }
                            }
                        });
    }

    private void getConversations(ArrayList<Chat> chats) {
        MessageUtil.getConversationsAll(true)
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(conversations -> {
                    ArrayList<Integer> deleted = new ArrayList<>();

                    int[] chatIds = toChatIds(chats);
                    int[] conversationIds = toConversationIds(conversations);
                    Arrays.sort(chatIds);
                    Arrays.sort(conversationIds);

                    for (int chat : chatIds) {
                        if (Arrays.binarySearch(conversationIds, chat) < 0) {
                            deleted.add(chat);
                        }
                    }

                    return Single.just(ArrayUtil.toInts(deleted));
                })
                .subscribe(ids -> {
                    adapter.refreshForDeleted(ids);
                    String title = getResources().getQuantityString(R.plurals.tab_chats_deleted, ids.length, ids.length);
                    tabs.getTabAt(1).setText(title);
                });
    }

    private void alertInfo() {
        boolean confirmed = SettingsStore.getBoolean("alert_chat_restore");
        if (!confirmed) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            builder.setTitle(R.string.information);
            builder.setMessage(R.string.messages_chat_restore_info);
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(dialog ->
                    SettingsStore.putValue("alert_chat_restore", true));
            builder.show();
        }
    }
}

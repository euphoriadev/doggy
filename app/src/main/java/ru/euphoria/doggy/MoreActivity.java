package ru.euphoria.doggy;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import io.reactivex.android.schedulers.AndroidSchedulers;
import ru.euphoria.doggy.adapter.DaysMoreAdapter;
import ru.euphoria.doggy.adapter.MembersMoreAdapter;
import ru.euphoria.doggy.adapter.MoreAdapter;
import ru.euphoria.doggy.adapter.SpeedyLinearLayoutManager;
import ru.euphoria.doggy.api.VKApi;
import ru.euphoria.doggy.common.DataHolder;
import ru.euphoria.doggy.common.Pair;
import ru.euphoria.doggy.util.AndroidUtil;
import ru.euphoria.doggy.util.MessageStats;
import ru.euphoria.doggy.util.MessageUtil;

/**
 * Created by admin on 04.05.18.
 */

public class MoreActivity extends BaseActivity {
    private static final int MENU_SORT = 100;
    private static final int MENU_SORT_BY_DEFAULT = 101;
    private static final int MENU_SORT_BY_DATE = 102;
    private static final int MENU_SORT_BY_ALPHABET = 103;
    private static final int MENU_SORT_BY_LENGTH = 104;
    private static final int MENU_SORT_BY_EXPLICIT = 105;

    private RecyclerView recycler;
    private LinearLayoutManager layoutManager;
    private MoreAdapter adapter;
    private String type;
    private int[] activeMembers;
    private int peer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_wrods);
        getIntentData();

        layoutManager = new SpeedyLinearLayoutManager(this);
        recycler = findViewById(R.id.recycler_view);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(layoutManager);

        findViewById(R.id.fab).setOnClickListener(v -> {
            recycler.smoothScrollToPosition(adapter.getItemCount());
        });
        createAdapter();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem itemSearch = menu.findItem(R.id.item_search);

        searchView = (SearchView) itemSearch.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                adapter.search(query);
                return true;
            }
        });

        if (type.equals(MessageStats.TYPE_DAYS) || type.equals(MessageStats.TYPE_WORDS)) {
            SubMenu sortOrder = menu.addSubMenu(Menu.NONE, MENU_SORT, Menu.NONE, R.string.sort)
                    .setIcon(R.drawable.ic_vector_sort);
            sortOrder.add(Menu.NONE, MENU_SORT_BY_DEFAULT, Menu.NONE, R.string.sort_by_default);

            switch (type) {
                case MessageStats.TYPE_DAYS:
                    sortOrder.add(Menu.NONE, MENU_SORT_BY_DATE, Menu.NONE, R.string.sort_by_date);
                    break;

                case MessageStats.TYPE_WORDS:
                    sortOrder.add(Menu.NONE, MENU_SORT_BY_ALPHABET, Menu.NONE, R.string.sort_by_alphabet);
                    sortOrder.add(Menu.NONE, MENU_SORT_BY_LENGTH, Menu.NONE, R.string.sort_by_length);
                    sortOrder.add(Menu.NONE, MENU_SORT_BY_EXPLICIT, Menu.NONE, R.string.sort_by_explicit);
                    break;
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean onOptionsItemSelected(MenuItem item) {
        adapter.setEnableArrows(false);
        switch (item.getItemId()) {
            case MENU_SORT_BY_DEFAULT:
                adapter.sort(MoreAdapter.byCount());
                break;
            case MENU_SORT_BY_DATE:
                adapter.sort(MoreAdapter.byDate());
                if (Objects.equals(type, MessageStats.TYPE_DAYS)) {
                    adapter.setEnableArrows(true);
                }
                break;

            case MENU_SORT_BY_ALPHABET:
                adapter.sort(MoreAdapter.byWords());
                break;

            case MENU_SORT_BY_LENGTH:
                adapter.sort(MoreAdapter.byLength());
                break;

            case MENU_SORT_BY_EXPLICIT:
                adapter.sort(MoreAdapter.byExplicit());
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

    @SuppressWarnings("unchecked")
    private void createAdapter() {
        String subtitle = null;
        switch (type) {
            case MessageStats.TYPE_WORDS:
                adapter = new MoreAdapter(this, (ArrayList<Pair>) DataHolder.getObject());
                adapter.setCountKeys(true);
                subtitle = getString(R.string.unique_words);
                break;

            case MessageStats.TYPE_SMILES:
                adapter = new MoreAdapter(this, (ArrayList<Pair>) DataHolder.getObject());
                subtitle = getString(R.string.unique_smiles);
                break;

            case MessageStats.TYPE_DAYS:
                adapter = new DaysMoreAdapter(this,
                        (ArrayList<Pair<Long, Integer>>) DataHolder.getObject());
                subtitle = getString(R.string.total_days);
                break;

            case MessageStats.TYPE_MEMBERS:
                adapter = new MembersMoreAdapter(this,
                        (ArrayList<Pair<Integer, Integer>>) DataHolder.getObject());
                adapter.setCountKeys(true);
                ((MembersMoreAdapter) adapter).setActiveMembers(activeMembers);

                adapter.setOnClickListener(v -> {
                    int position = recycler.getChildAdapterPosition(v);
                    Pair<Integer, Integer> item = (Pair<Integer, Integer>) adapter.getItem(position);
                    makeMemberDialog(adapter.getKey(item), item);
                });

                System.out.println("adapter count:" + adapter.getItemCount());
                System.out.println("active count:" + activeMembers.length);
                System.out.println("kicked:" + (adapter.getItemCount() - activeMembers.length));
                System.out.println(Arrays.toString(activeMembers));
                if (activeMembers != null && activeMembers.length > 0) {
                    getSupportActionBar().setSubtitle(
                            getString(R.string.members_stats_subtitle,
                                    adapter.getItemCount(),
                                    adapter.getItemCount() - activeMembers.length));
                }
                break;
        }
        adapter.setLongClickListener(v -> {
            int position = recycler.getChildAdapterPosition(v);
            Pair<?, ?> pair = (Pair<?, ?>) adapter.getItem(position);
            AndroidUtil.copyText(this, adapter.getKey(pair));

            return true;
        });

        recycler.setAdapter(adapter);
        if (subtitle != null) {
            getSupportActionBar().setSubtitle(String.format(subtitle, adapter.getItemCount()));
        }
    }

    private void makeMemberDialog(String title, Pair<Integer, Integer> item) {
        String[] items = getResources().getStringArray(R.array.chat_member_dialog);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(title);
        builder.setItems(items, (dialog, which) -> handleAction(item.first, which));
        builder.show();
    }

    private void handleAction(int peer, int which) {
        System.out.println("peer: " + peer);
        switch (which) {
            case 0: AndroidUtil.browseUserOrGroup(this, peer); break;
            case 1: kickMember(peer); break;
        }
    }

    @SuppressLint("CheckResult")
    private void kickMember(int user) {
        MessageUtil.removeChatUser(peer - VKApi.PEER_OFFSET, user)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> {
                    if (success) {
                        toast(user + " kicked from chat");

                        // Hide user from active members list
                        int i = Arrays.binarySearch(activeMembers, user);
                        if (i > 0) {
                            activeMembers[i] = -1;
                        }
                    }
                }, AndroidUtil.handleError(this));
    }

    private void getIntentData() {
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        peer = intent.getIntExtra("peer", -1);
        type = intent.getStringExtra("type");
        activeMembers = intent.getIntArrayExtra("active_members");

        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}

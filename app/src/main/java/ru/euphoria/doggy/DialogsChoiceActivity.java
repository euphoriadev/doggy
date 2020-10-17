package ru.euphoria.doggy;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import io.reactivex.Maybe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import ru.euphoria.doggy.adapter.DialogsAdapter;
import ru.euphoria.doggy.adapter.WrapLinearLayoutManager;
import ru.euphoria.doggy.api.ConversationResponse;
import ru.euphoria.doggy.api.model.Conversation;
import ru.euphoria.doggy.db.AppDatabase;
import ru.euphoria.doggy.util.AndroidUtil;
import ru.euphoria.doggy.util.MessageUtil;
import ru.euphoria.doggy.util.ViewUtil;

/**
 * Created by admin on 10.05.18.
 */

public class DialogsChoiceActivity extends BaseActivity
        implements SwipeRefreshLayout.OnRefreshListener {
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recycler;
    private DialogsAdapter adapter;
    private int offset = 0;
    private boolean empty;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice_dialog);
        getSupportActionBar().setTitle(R.string.chat_choice);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LinearLayoutManager layoutManager = new WrapLinearLayoutManager(this);

        refreshLayout = findViewById(R.id.refresh_layout);
        refreshLayout.setColorSchemeColors(ViewUtil.swipeRefreshColors(this));
        refreshLayout.setOnRefreshListener(this);

        recycler = findViewById(R.id.recycler_view);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(layoutManager);
        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!searchView.isIconified()) {
                    return;
                }
                if (!empty && dy > 0) {
                    // Scrolling up
                    int lastPosition = layoutManager.findLastVisibleItemPosition();
                    if (adapter.getItemCount() - lastPosition < 15) {
                        if (!refreshLayout.isRefreshing()) {
                            fetchChats(offset += 50);
                        }
                    }
                }
            }
        });


        fetchChats(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_friends, menu);
        menu.findItem(R.id.item_sort).setVisible(false);

        MenuItem itemSearch = menu.findItem(R.id.item_search);

        searchView = (SearchView) itemSearch.getActionView();
        searchView.setQueryHint("Search People");
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

    public void onItemClick(Conversation conversation) {
        Intent intent = getIntent();
        intent.putExtra("peer_id", conversation.peer.id);
        intent.putExtra("conversation", conversation);
        if (intent.getIntExtra("id", 0) == MainActivity.ID_MESSAGES_STATS) {
            intent.setClass(this, MessageStatsActivity.class);
        }

        startActivity(intent);
    }

    @SuppressLint("CheckResult")
    private void fetchChats(int offset) {
        if (!AndroidUtil.hasConnection()) {
            return;
        }
        refreshLayout.setRefreshing(true);

        MessageUtil.getConversations(offset)
                .filter(conversations -> conversations.count() > 0)
                .flatMap(conversations -> {
                    AppDatabase.database().users().insert(conversations.users());
                    AppDatabase.database().groups().insert(conversations.groups());
                    return Maybe.just(conversations);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(conversation -> createAdapter(conversation, offset), AndroidUtil.handleError(this));

    }

    private void createAdapter(ConversationResponse response, int offset) {
        refreshLayout.setRefreshing(false);
        if (response.count() == 0) {
            return;
        }

        if (offset > 0) {
            adapter.messages.addAll(response.lastMessages());
            adapter.conversation.addAll(response.items());
            adapter.notifyDataSetChanged();
            return;
        }

        adapter = new DialogsAdapter(this, response);
        recycler.setAdapter(adapter);

        adapter.setOnClickListener(v -> {
            int position = recycler.getChildAdapterPosition(v);
            Conversation conversation = adapter.getConversation(position);
            onItemClick(conversation);
        });
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
    public void onRefresh() {
        fetchChats(0);
    }
}

package ru.euphoria.doggy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import ru.euphoria.doggy.adapter.UsersAdapter;
import ru.euphoria.doggy.adapter.WrapLinearLayoutManager;
import ru.euphoria.doggy.api.model.User;
import ru.euphoria.doggy.util.AndroidUtil;
import ru.euphoria.doggy.util.GroupUtil;
import ru.euphoria.doggy.util.ViewUtil;

@SuppressLint("CheckResult")
public class GroupMembersActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {
    @BindView(R.id.recycler_view)
    RecyclerView recycler;
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout refreshLayout;

    private UsersAdapter adapter;

    public static void start(Context context, int group) {
        Intent starter = new Intent(context, GroupMembersActivity.class);
        starter.putExtra("group_id", group);
        context.startActivity(starter);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_members);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.members);

        recycler.setLayoutManager(new WrapLinearLayoutManager(this));
        recycler.setHasFixedSize(true);

        refreshLayout.setColorSchemeColors(ViewUtil.swipeRefreshColors(this));
        refreshLayout.setOnRefreshListener(this);

        getMembers();
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

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onRefresh() {
        getMembers();
    }

    private void createAdapter(ArrayList<User> members) {
        adapter = new UsersAdapter(this, members);
        recycler.setAdapter(adapter);

        getSupportActionBar().setSubtitle(
                String.format(Locale.ROOT,
                        "%,d", adapter.getItemCount())
        );
    }

    private void getMembers() {
        if (!AndroidUtil.hasConnection()) {
            AndroidUtil.toastErrorConnection(this);
            return;
        }
        refreshLayout.setRefreshing(true);

        GroupUtil.getMembers(this, groupId(), User.DEFAULT_FIELDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> refreshLayout.setRefreshing(false))
                .subscribe(this::createAdapter, AndroidUtil.handleError(this));
    }

    private int groupId() {
        return getIntent().getIntExtra("group_id", 0);
    }
}

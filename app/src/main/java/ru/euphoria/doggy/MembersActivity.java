package ru.euphoria.doggy;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;

import io.reactivex.Maybe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import ru.euphoria.doggy.adapter.MembersAdapter;
import ru.euphoria.doggy.adapter.WrapLinearLayoutManager;
import ru.euphoria.doggy.api.model.User;
import ru.euphoria.doggy.db.AppDatabase;
import ru.euphoria.doggy.util.AndroidUtil;
import ru.euphoria.doggy.util.ArrayUtil;
import ru.euphoria.doggy.util.MessageUtil;
import ru.euphoria.doggy.util.UserUtil;

/**
 * Created by admin on 06.06.18.
 */

@SuppressLint("CheckResult")
public class MembersActivity extends BaseActivity {
    private int peer;
    private long admin;
    private RecyclerView recycler;
    private MembersAdapter adapter;
    private int[] activeMembers;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_members);
        getSupportActionBar().setTitle(R.string.members);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LinearLayoutManager layoutManager = new WrapLinearLayoutManager(this);
        recycler = findViewById(R.id.recycler_view);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(layoutManager);

        peer = getIntent().getIntExtra("peer", 0);
        admin = getIntent().getLongExtra("admin", 0);
        getMembers();
    }

    private void createAdapter(ArrayList<User> members) {
        adapter = new MembersAdapter(this, peer, admin, members);
        recycler.setAdapter(adapter);
    }

    private int[] getInvitedByIds(ArrayList<User> members) {
        HashSet<Integer> set = new HashSet<>();
        for (int i = 0; i < members.size(); i++) {
            set.add(members.get(i).invited_by);
        }

        return ArrayUtil.toInts(set);
    }


    private void getMembers() {
        if (!AndroidUtil.hasConnection()) {
            Toast.makeText(this, R.string.error_connection, Toast.LENGTH_LONG)
                    .show();
            return;
        }

        MessageUtil.getMembers(peer)
                .filter(users -> !users.isEmpty())
                .flatMap(users -> {
                    int[] ids = getInvitedByIds(users);
                    ArrayList<User> invited = UserUtil.getUsers(ids).blockingGet();
                    AppDatabase.database().users().insert(invited);

                    return Maybe.just(users);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::createAdapter, AndroidUtil.handleError(this));
    }
}

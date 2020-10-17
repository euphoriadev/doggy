package ru.euphoria.doggy;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseArray;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.yandex.metrica.YandexMetrica;

import java.util.ArrayList;
import java.util.List;

import ru.euphoria.doggy.adapter.FriendListsAdapter;
import ru.euphoria.doggy.adapter.WrapLinearLayoutManager;
import ru.euphoria.doggy.api.model.User;
import ru.euphoria.doggy.common.Pair;
import ru.euphoria.doggy.data.SettingsStore;
import ru.euphoria.doggy.util.AndroidUtil;
import ru.euphoria.doggy.util.UserUtil;

@SuppressLint("CheckResult")
public class FriendListsActivity extends BaseActivity {
    private RecyclerView recycler;
    private FriendListsAdapter adapter;
    private SparseArray<ArrayList<Pair<Integer, String>>> lists = new SparseArray<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_lists);
        getSupportActionBar().setTitle(R.string.friends_lists);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recycler = findViewById(R.id.recycler_view);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(new WrapLinearLayoutManager(this));

        adapter = new FriendListsAdapter(this, new ArrayList<>());
        adapter.setOnClickListener(v -> {
            int position = recycler.getChildAdapterPosition(v);
            User user = adapter.getItem(position);
            AndroidUtil.browse(this, user);
        });
        recycler.setAdapter(adapter);

        getLists();
        alertInfo();
    }

    private void alertInfo() {
        if (!SettingsStore.getBoolean("friends_lists_info")) {
            SettingsStore.putValue("friends_lists_info", true);

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            builder.setTitle(R.string.friends_lists);
            builder.setMessage(R.string.friends_lists_alert_info);
            builder.setPositiveButton(android.R.string.ok, null);
            builder.show();
        }
    }

    private void getLists() {
        if (!AndroidUtil.hasConnection()) {
            Toast.makeText(this, R.string.error_connection, Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                List<User> users = UserUtil.getFriends(this).blockingGet();
                runOnUiThread(() -> toolbar.setSubtitle("0/" + users.size()));
                for (int i = 0; i < users.size(); i++) {
                    if (isDestroyed()) return;

                    int finalI = i;
                    runOnUiThread(() -> toolbar.setSubtitle(finalI + 1 + "/" + users.size()));

                    User user = users.get(i);
                    if (!TextUtils.isEmpty(user.deactivated)) {
                        continue;
                    }

                    ArrayList<Pair<Integer, String>> friendLists =
                            UserUtil.getFriendLists(this, SettingsStore.getUserId(), user.id).blockingSingle();
                    if (friendLists != null && !friendLists.isEmpty()) {
                        lists.put(user.id, friendLists);
                        adapter.setLists(lists);
                        adapter.getValues().add(user);

                        runOnUiThread(() -> adapter.notifyDataSetChanged());
                    }
                }
                YandexMetrica.reportEvent("Списки друзей");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}

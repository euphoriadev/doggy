package ru.euphoria.doggy;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.yandex.metrica.YandexMetrica;

import java.util.ArrayList;

import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.euphoria.doggy.adapter.UsersAdapter;
import ru.euphoria.doggy.adapter.WrapLinearLayoutManager;
import ru.euphoria.doggy.api.VKApi;
import ru.euphoria.doggy.api.model.User;
import ru.euphoria.doggy.common.BaseActionMode;
import ru.euphoria.doggy.data.SettingsStore;
import ru.euphoria.doggy.util.AndroidUtil;
import ru.euphoria.doggy.util.UserUtil;

public class FriendsRequestsActivity extends BaseActivity {
    private static final int TYPE_UNSUBSCRIBE = 0;
    private static final int TYPE_BAN = 1;

    private BaseActionMode actionMode;
    private RecyclerView recycler;
    private UsersAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_lists);

        getSupportActionBar().setTitle(R.string.friends_requests);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recycler = findViewById(R.id.recycler_view);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(new WrapLinearLayoutManager(this));
        actionMode = new FriendsActionMode(this);

        getRequests();
        alert();
    }

    private void process(ProgressDialog dialog, int type) {
        new Thread(() -> {
            for (int i = 0; i < adapter.getCheckedCount(); i++) {
                int id = adapter.getChecked().get(i).intValue();

                try {
                    switch (type) {
                        case TYPE_UNSUBSCRIBE:
                            unsubscribe(id);
                            break;
                        case TYPE_BAN:
                            ban(id);
                            break;
                    }
                    updateProgress(dialog);
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(()
                            -> AndroidUtil.handleError(this, e));
                }
            }
            runOnUiThread(() -> {
                dialog.dismiss();
                if (type == TYPE_UNSUBSCRIBE) {
                    YandexMetrica.reportEvent("Отписка от людей");
                }
            });
        }).start();
    }

    private void createSuccessDialog(int count) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(R.string.success);
        builder.setMessage(getString(R.string.friends_ban_message, count));
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> createBanProgressDialog());
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
            if (AppContext.ads) {
                AndroidUtil.loadInterstitialAds(this);
            }
            if (actionMode != null) {
                actionMode.finish();
            }
            adapter.getChecked().clear();
            getRequests();
        });
        builder.show();
    }

    private void createBanProgressDialog() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle(getString(R.string.blocking));
        dialog.setMax(adapter.getCheckedCount());
        dialog.setProgress(0);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setOnDismissListener(dialog1 -> {
            if (AppContext.ads) {
                AndroidUtil.loadInterstitialAds(this);
            }
            if (actionMode != null) {
                actionMode.finish();
            }
            adapter.getChecked().clear();
            getRequests();
        });
        dialog.show();

        process(dialog, TYPE_BAN);
    }

    private void createUnsubscribeProgressDialog() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle(getString(R.string.unsubscribe));
        dialog.setMax(adapter.getCheckedCount());
        dialog.setProgress(0);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setOnDismissListener(d -> createSuccessDialog(adapter.getCheckedCount()));
        dialog.show();

        process(dialog, TYPE_UNSUBSCRIBE);
    }

    private void updateProgress(ProgressDialog dialog) {
        runOnUiThread(() -> dialog.setProgress(dialog.getProgress() + 1));
    }

    private void alert() {
        if (!SettingsStore.getBoolean("friends_requests")) {
            SettingsStore.putValue("friends_requests", true);

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            builder.setTitle(R.string.friends_requests);
            builder.setMessage(R.string.friends_requests_alert);
            builder.setPositiveButton(android.R.string.ok, null);
            builder.show();
        }
    }

    private void createAdapter(ArrayList<User> users) {
        adapter = new UsersAdapter(this, users);
        adapter.setOnClickListener(v -> {
            int position = recycler.getChildAdapterPosition(v);

            actionMode.click(position);
        });
        adapter.setLongClickListener(v -> {
            int position = recycler.getChildAdapterPosition(v);
            actionMode.longClick(position);
            return true;
        });
        recycler.setAdapter(adapter);
    }

    private void ban(int id) throws Exception {
        VKApi.account().ban().ownerId(id).json();
    }

    private void unsubscribe(int id) throws Exception {
        VKApi.friends().delete().userId(id).json();
    }

    private void toastNoRequests() {
        Toast.makeText(this, "У вас нет исходящих заявок", Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("CheckResult")
    private void getRequests() {
        if (!AndroidUtil.hasConnection()) {
            Toast.makeText(this, R.string.error_connection, Toast.LENGTH_LONG).show();
            return;
        }

        Single.fromCallable(() ->
                VKApi.friends().getRequests()
                        .count(1000).json())
                .map(VKApi::optJsonArray)
                .filter(array -> array.length() > 0)
                .map(VKApi::parseArray)
                .flatMap(ids -> Maybe.just(UserUtil.getUsers(ids).blockingGet()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::createAdapter, AndroidUtil.handleError(this));

    }

    private class FriendsActionMode extends BaseActionMode {
        public FriendsActionMode(AppCompatActivity activity) {
            super(activity);
        }

        @Override
        public boolean onCreateActionMode(androidx.appcompat.view.ActionMode mode, Menu menu) {
            getMenuInflater().inflate(R.menu.menu_friends_requests, menu);
            return true;
        }

        @Override
        public boolean onActionItemClicked(androidx.appcompat.view.ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.item_select_all:
                    adapter.checkAll();
                    updateTitle();
                    break;

                case R.id.item_unsubscribe:
                    createUnsubscribeProgressDialog();
                    break;
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

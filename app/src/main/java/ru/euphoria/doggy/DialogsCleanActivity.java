package ru.euphoria.doggy;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.euphoria.doggy.adapter.DialogsAdapter;
import ru.euphoria.doggy.api.ConversationResponse;
import ru.euphoria.doggy.api.VKApi;
import ru.euphoria.doggy.api.model.Message;
import ru.euphoria.doggy.db.AppDatabase;
import ru.euphoria.doggy.util.AndroidUtil;
import ru.euphoria.doggy.util.MessageUtil;

public class DialogsCleanActivity extends BaseActivity {
    private DialogsAdapter adapter;
    private RecyclerView recycler;
    int chats, unread;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialogs_clean);
        getSupportActionBar().setTitle(R.string.dialogs_clean);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recycler = findViewById(R.id.recycler_view);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        getDialogs(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_friends_requests, menu);
        menu.findItem(R.id.item_unsubscribe).setVisible(adapter != null && adapter.getCheckedCount() > 0);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item_unsubscribe) {
            createCleanDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("CheckResult")
    private void createCleanDialog() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle(getString(R.string.cleaning));
        dialog.setMax(adapter.getCheckedCount());
        dialog.setProgress(0);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        dialog.show();

        Flowable.fromIterable(adapter.getChecked())
                .map(this::delete)
                .doFinally(() -> {
                    runOnUiThread(() -> {
                        dialog.dismiss();
                        if (AppContext.ads) {
                            AndroidUtil.loadInterstitialAds(this);
                        }
                    });
                    getDialogs(0);
                })
                .subscribeOn(Schedulers.io())
                .subscribe(success -> {
                    if (success) updateProgress(dialog);
                }, AndroidUtil.handleError(this));
    }

    private boolean delete(long peer) throws Exception {
        JSONObject json = VKApi.messages().deleteConversation().peerId(peer).json();
        return json.optInt("response") == 1;
    }

    private void updateSubtitle(ArrayList<Message> messages) {
        int total = adapter.getItemCount();

        for (Message msg : messages) {
            if (msg.peer_id > VKApi.PEER_OFFSET) {
                chats++;
            }
            if (msg.unread > 0) {
                unread++;
            }
        }

        getSupportActionBar().setSubtitle(String.format(Locale.getDefault(),
                "total: %,d | chats: %,d | unread: %,d", total, chats, unread));
    }

    private void updateProgress(ProgressDialog dialog) {
        runOnUiThread(() -> dialog.setProgress(dialog.getProgress() + 1));
    }


    private void createAdapter(ConversationResponse response, int offset) {
        if (response.count() == 0) {
            return;
        }

        if (offset > 0) {
            adapter.messages.addAll(response.lastMessages());
            adapter.conversation.addAll(response.items());
            adapter.notifyDataSetChanged();
            updateSubtitle(response.lastMessages());
            return;
        }

        adapter = new DialogsAdapter(this, response);
        recycler.setAdapter(adapter);

        adapter.setOnClickListener(v -> {
            int position = recycler.getChildAdapterPosition(v);

            adapter.toggleChecked(position);
            adapter.notifyItemChanged(position);
            if (adapter.getCheckedCount() == 3) {
                Toast.makeText(this, R.string.long_click_to_select_all, Toast.LENGTH_LONG)
                        .show();
            }
            invalidateOptionsMenu();
        });
        adapter.setLongClickListener(v -> {
            adapter.checkAll();
            adapter.notifyDataSetChanged();
            invalidateOptionsMenu();
            return true;
        });
        updateSubtitle(response.lastMessages());
    }

    @SuppressLint("CheckResult")
    private void getDialogs(int offset) {
        MessageUtil.getConversations(offset, 200, true)
                .filter(response -> response.items().size() > 0)
                .flatMap(conversations -> {
                    AppDatabase.database().users().insert(conversations.users());
                    AppDatabase.database().groups().insert(conversations.groups());
                    return Maybe.just(conversations);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    createAdapter(response, offset);
                    getDialogs(adapter.getItemCount());
                }, AndroidUtil.handleError(this));
    }
}

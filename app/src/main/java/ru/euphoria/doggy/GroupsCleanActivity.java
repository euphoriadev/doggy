package ru.euphoria.doggy;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yandex.metrica.YandexMetrica;

import org.json.JSONObject;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.euphoria.doggy.adapter.GroupsAdapter;
import ru.euphoria.doggy.adapter.WrapLinearLayoutManager;
import ru.euphoria.doggy.api.VKApi;
import ru.euphoria.doggy.api.model.Community;
import ru.euphoria.doggy.util.AndroidUtil;
import ru.euphoria.doggy.util.GroupUtil;

@SuppressLint("CheckResult")
public class GroupsCleanActivity extends BaseActivity {
    private RecyclerView recycler;
    private GroupsAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups_clean);
        getSupportActionBar().setTitle(R.string.groups_clean);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LinearLayoutManager layoutManager = new WrapLinearLayoutManager(this);

        recycler = findViewById(R.id.recycler_view);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(layoutManager);

        getGroups();
    }

    private void getGroups() {
        GroupUtil.getMyGroups()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::createAdapter,
                        AndroidUtil.handleError(this));
    }

    private void createAdapter(List<Community> groups) {
        adapter = new GroupsAdapter(this, groups);
        recycler.setAdapter(adapter);

        adapter.setOnClickListener(v -> {
            int position = recycler.getChildAdapterPosition(v);

            adapter.toggleChecked(position);
            adapter.notifyItemChanged(position);
            if (adapter.getCheckedCount() == 3) {
                Toast.makeText(this, R.string.long_click_to_select_all, Toast.LENGTH_LONG)
                        .show();
            }
        });
        adapter.setLongClickListener(v -> {
            adapter.checkAll();
            adapter.notifyDataSetChanged();
            return true;
        });

        View button = findViewById(R.id.buttonDelete);
        if (button != null) {
            button.setVisibility(adapter.getItemCount() == 0 ? View.GONE : View.VISIBLE);
            button.setOnClickListener(v -> {
                if (adapter.getCheckedCount() > 0) {
                    createDialog();
                }
            });
        }
    }

    private void updateProgress(ProgressDialog dialog) {
        runOnUiThread(() -> dialog.setProgress(dialog.getProgress() + 1));
    }

    private void createDialog() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle(getString(R.string.deleting));
        dialog.setMax(adapter.getCheckedCount());
        dialog.setProgress(0);
        dialog.setCancelable(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.show();

        Flowable.fromIterable(adapter.getChecked())
                .map(itemId -> leave(itemId.intValue()))
                .doFinally(() -> runOnUiThread(() -> {
                    dialog.dismiss();
                    YandexMetrica.reportEvent("Выход из групп");

                    if (AppContext.ads) {
                        AndroidUtil.loadInterstitialAds(this);
                    }
                    getGroups();
                }))
                .subscribeOn(Schedulers.io())
                .forEach(success -> {
                    if (success) updateProgress(dialog);
                });
    }

    private boolean leave(int id) throws Exception {
        JSONObject json = VKApi.groups().leave().groupId(id).json();
        return json.optInt("response") == 1;
    }

}

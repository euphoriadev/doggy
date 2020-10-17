package ru.euphoria.doggy;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.yandex.metrica.YandexMetrica;

import org.json.JSONObject;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import ru.euphoria.doggy.adapter.UsersAdapter;
import ru.euphoria.doggy.adapter.WrapLinearLayoutManager;
import ru.euphoria.doggy.api.VKApi;
import ru.euphoria.doggy.api.model.User;
import ru.euphoria.doggy.common.DebugLog;
import ru.euphoria.doggy.data.SettingsStore;
import ru.euphoria.doggy.db.AppDatabase;
import ru.euphoria.doggy.util.AndroidUtil;
import ru.euphoria.doggy.util.ArrayUtil;
import ru.euphoria.doggy.util.UserUtil;

/**
 * Created by admin on 07.04.18.
 */

@SuppressLint("CheckResult")
public class FriendsFragment extends Fragment {
    private View root;
    private RecyclerView recycler;
    private UsersAdapter adapter;
    private boolean dogsClean;
    private int checkedOrder = 1;

    public static FriendsFragment newInstance(boolean dogsClean) {
        Bundle args = new Bundle();
        args.putBoolean("dogs_clean", dogsClean);

        FriendsFragment fragment = new FriendsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments != null) {
            dogsClean = arguments.getBoolean("dogs_clean");
        }
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_friends, container, false);
        LinearLayoutManager layoutManager = new WrapLinearLayoutManager(getActivity());

        recycler = root.findViewById(R.id.recycler_view);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(layoutManager);
        if (dogsClean) {
            boolean confirmed = SettingsStore.getBoolean("alert_confirmed");
            if (!confirmed) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
                builder.setTitle(R.string.information);
                builder.setMessage(R.string.friends_deactivated_info);
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(dialog ->
                        SettingsStore.putValue("alert_confirmed", true));
                builder.show();
            }
        }

        fetchUsers();
        return root;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_sort:
                showSortDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onUserClick(User value) {
        Intent intent = new Intent();
        intent.putExtra("id", value.id);
        intent.putExtra("user", value);
        intent.putExtra("peer_id", value.id);
        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
    }

    public void onDogClick(int position, User value) {
        if (!AndroidUtil.hasConnection()) {
            AndroidUtil.toastErrorConnection(getActivity());
            return;
        }
        adapter.toggleChecked(position);
        adapter.notifyItemChanged(position);
        if (adapter.getCheckedCount() == 3) {
            Toast.makeText(getActivity(), R.string.long_click_to_select_all, Toast.LENGTH_LONG)
                    .show();
        }

    }

    private boolean deleteSuccess(int id, JSONObject response) {
        boolean success = response.optInt("success") == 1;
        if (success) {
            AppDatabase.database().users().delete(id);
        }
        return success;
    }

    private void showSortDialog() {
        String[] items = new String[]{
                "Default",
                "Hints",
                "Random",
                "Mobile",
                "Name"
        };
        new MaterialAlertDialogBuilder(getActivity())
                .setTitle("Sort order")
                .setSingleChoiceItems(items, checkedOrder, (dialog, which) -> {
                    String order = items[checkedOrder = which].toLowerCase();
                    dialog.cancel();

                    UserUtil.getFriends(getActivity(), order)
                            .flattenAsFlowable(users -> users)
                            .filter(user -> !dogsClean || !TextUtils.isEmpty(user.deactivated))
                            .toList()
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(this::createAdapter, AndroidUtil.handleError(getActivity()));
                }).show();
    }

    public UsersAdapter adapter() {
        return adapter;
    }

    private void fetchUsers() {
        if (!AndroidUtil.hasConnection()) {
            return;
        }

        UserUtil.getFriends(getActivity(), SettingsStore.getUserId(), "hints")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(users -> {
                    DebugLog.w("FriendsFragment", "friends count: " + users.size());
                    for (User user : users) {
                        user.is_friend = true;
                    }
                    AppDatabase.database().users().insert(users);

                    if (dogsClean) {
                        ArrayUtil.filter(users, user -> !TextUtils.isEmpty(user.deactivated));
                    } else if (!hasUser(users, SettingsStore.getUserId())) {
                        User me = AppDatabase.database().users().byIdSync(SettingsStore.getUserId());
                        users.add(0, me);
                    }
                    createAdapter(users);
                }, AndroidUtil.handleError(getActivity()));
    }

    private boolean hasUser(List<User> users, int user) {
        for (User u : users) {
            if (u.id == user) {
                return true;
            }
        }
        return false;
    }

    private void createAdapter(List<User> users) {
        if (getActivity() == null) {
            return;
        }

        if (dogsClean && (users == null || users.isEmpty())) {
            createEmptyView();
            return;
        }

        if (adapter == null) {
            adapter = new UsersAdapter(getActivity(), users);
            recycler.setAdapter(adapter);

            adapter.setOnClickListener(v -> {
                int position = recycler.getChildAdapterPosition(v);
                User item = adapter.getItem(position);
                if (dogsClean) {
                    onDogClick(position, item);
                } else {
                    onUserClick(item);
                }
            });
        } else {
            adapter.getValues().clear();
            adapter.getValues().addAll(users);
            adapter.notifyDataSetChanged();

            if (dogsClean && adapter.getItemCount() == 0) {
                createEmptyView();
            }
        }

        if (dogsClean && adapter.getItemCount() > 0) {
            root.findViewById(R.id.buttonDelete).setVisibility(View.VISIBLE);
            root.findViewById(R.id.buttonDelete)
                    .setOnClickListener(v -> {
                        if (adapter.getCheckedCount() == 0) {
                            Toast.makeText(getActivity(), R.string.select_more_friends, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        deleteFriends();
                    });
        }
        if (dogsClean) {
            adapter.setLongClickListener(v -> {
                adapter.checkAll();
                adapter.notifyDataSetChanged();
                return true;
            });
        }
    }

    private void createEmptyView() {
        Activity activity = getActivity();
        if (activity == null) return;

        recycler.setVisibility(View.GONE);
        View empty = LayoutInflater.from(activity).inflate(R.layout.view_no_items, ((ViewGroup) root), false);
        empty.findViewById(R.id.buttonBuy).setOnClickListener(v -> Toast.makeText(activity, R.string.dogs_shop_not_open, Toast.LENGTH_SHORT).show());
        ((ViewGroup) root).addView(empty);
        View button = activity.findViewById(R.id.buttonDelete);
        if (button != null) {
            button.setVisibility(View.GONE);
        }
    }

    private void deleteFriends() {
        Activity activity = getActivity();
        ProgressDialog dialog = new ProgressDialog(activity);
        dialog.setTitle(getString(R.string.deleting));
        dialog.setMax(adapter.getCheckedCount());
        dialog.setProgress(0);
        dialog.setCancelable(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.show();

        new Thread(() -> {
            for (int i = 0; i < adapter.getCheckedCount(); i++) {
                long checked = adapter.getChecked().get(i);
                try {
                    JSONObject response = VKApi.friends().delete()
                            .userId((int) checked)
                            .json().optJSONObject("response");
                    if (deleteSuccess((int) checked, response)) {
                        activity.runOnUiThread(()
                                -> dialog.setProgress(dialog.getProgress() + 1));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (activity == null) {
                        return;
                    }
                    activity.runOnUiThread(()
                            -> AndroidUtil.toast(activity, e.getMessage()));
                }
            }
            activity.runOnUiThread(() -> {
                dialog.dismiss();
                adapter.getChecked().clear();
                adapter.notifyDataSetChanged();

                if (AppContext.ads) {
                    AndroidUtil.loadInterstitialAds(activity);
                }
            });
            YandexMetrica.reportEvent("Очистка друзей от собачек");
        }).start();
    }
}

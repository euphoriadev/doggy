package ru.euphoria.doggy;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.yandex.metrica.YandexMetrica;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import ru.euphoria.doggy.adapter.BirthdayAdapter;
import ru.euphoria.doggy.adapter.WrapLinearLayoutManager;
import ru.euphoria.doggy.api.model.User;
import ru.euphoria.doggy.data.SettingsStore;
import ru.euphoria.doggy.db.AppDatabase;
import ru.euphoria.doggy.util.AndroidUtil;
import ru.euphoria.doggy.util.ArrayUtil;
import ru.euphoria.doggy.util.UserUtil;

@SuppressLint("CheckResult")
public class BirthdayActivity extends BaseActivity {
    private BirthdayAdapter adapter;
    private RecyclerView recycler;
    private LinearLayoutManager layoutManager;
    private List<User> users;
    private boolean hidden;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler);
        getSupportActionBar().setTitle(R.string.friends_birthday);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        layoutManager = new WrapLinearLayoutManager(this);
        recycler = findViewById(R.id.recycler_view);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(layoutManager);

        getCachedFriends();
        updateUsers();
    }

    private void getCachedFriends() {
        AppDatabase.database().users().friends()
                .observe(this, this::createAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_birthday, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item_hidden_birthdays) {
            if (!hidden) {
                alertExperimental();
                createAdapterForHidden();
                item.setIcon(R.drawable.ic_vector_eye);

                YandexMetrica.reportEvent("Скрытые дни рождения");
            } else {
                item.setIcon(R.drawable.ic_vector_eye_off);
                getCachedFriends();
            }
            hidden = !hidden;
        }
        return super.onOptionsItemSelected(item);
    }

    private void alertExperimental() {
        if (!SettingsStore.getBoolean("alert_experimental")) {
            SettingsStore.putValue("alert_experimental", true);
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            builder.setTitle(R.string.hidden_birthdays);
            builder.setMessage(R.string.hidden_birthdays_message);
            builder.setPositiveButton(android.R.string.ok, null);
            builder.show();
        }
    }

    private void updateUsers() {
        UserUtil.getFriends(this)
                .subscribe(users -> AppDatabase.database().users().insert(users),
                        AndroidUtil.handleError(this));

    }

    private void createAdapterForHidden() {
        if (users.isEmpty()) return;

        ArrayList<BirthdayAdapter.SectionRow> rows = new ArrayList<>(users.size());
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            if (TextUtils.isEmpty(user.birthday)) {
                rows.add(new BirthdayAdapter.SectionRow(user, null, false));
            }
        }

        adapter = new BirthdayAdapter(this, rows);
        recycler.setAdapter(adapter);
        adapter.setOnClickListener(v -> new Thread(() -> {
            try {
                int position = recycler.getChildAdapterPosition(v);
                User user = adapter.getItem(position).user;

                int age = UserUtil.getAge(this, user);
                int month = UserUtil.getMonth(user);

                AndroidUtil.toast(this, String.format("%s лет, %s месяц", age, month));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start());
    }

    private void createAdapter(List<User> users) {
        if (users.isEmpty()) return;
        this.users = users;

        String[] months = getResources().getStringArray(R.array.months);
        ArrayList<BirthdayAdapter.SectionRow> rows = new ArrayList<>(users.size());

        ArrayList<User> copied = new ArrayList<>(users);
        ArrayUtil.filter(copied, user ->
                !TextUtils.isEmpty(user.birthday));

        Collections.sort(copied, (o1, o2) -> {
            String[] split1 = o1.birthday.split("\\.");
            String[] split2 = o2.birthday.split("\\.");
            int date1 = (100 * Integer.parseInt(split1[1])) + Integer.parseInt(split1[0]);
            int date2 = (100 * Integer.parseInt(split2[1])) + Integer.parseInt(split2[0]);

            return Integer.compare(date1, date2);
        });

        for (int i = 0; i < months.length; i++) {
            String month = months[i];

            rows.add(new BirthdayAdapter.SectionRow(null, month, true));
            for (User user : copied) {
                if (TextUtils.isEmpty(user.birthday)) {
                    continue;
                }
                int birthdayMonth = Integer.parseInt(user.birthday.split("\\.")[1]);
                if (i == (birthdayMonth - 1)) {
                    rows.add(new BirthdayAdapter.SectionRow(user, null, false));
                }
            }
        }

        adapter = new BirthdayAdapter(this, rows);
        recycler.setAdapter(adapter);
        adapter.setOnClickListener(v -> {
            int position = recycler.getChildAdapterPosition(v);
            User user = adapter.getItem(position).user;
            AndroidUtil.browse(this, user);
        });

        int currentMonth = new Date().getMonth();
        int headerCount = 0;
        for (int i = 0; i < rows.size(); i++) {
            BirthdayAdapter.SectionRow row = rows.get(i);
            if (row.header && headerCount++ == currentMonth) {
                recycler.scrollToPosition(i);
            }
        }
    }
}


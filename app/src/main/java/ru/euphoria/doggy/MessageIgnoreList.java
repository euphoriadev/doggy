package ru.euphoria.doggy;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import ru.euphoria.doggy.adapter.IgnoreWordAdapter;
import ru.euphoria.doggy.data.SettingsStore;
import ru.euphoria.doggy.dialog.EditTextDialog;
import ru.euphoria.doggy.util.MessageStats;

public class MessageIgnoreList extends BaseActivity {
    @BindView(R.id.recycler_view) RecyclerView recycler;
    @BindView(R.id.fab) FloatingActionButton fab;

    private IgnoreWordAdapter adapter;

    public static void start(Context context) {
        Intent starter = new Intent(context, MessageIgnoreList.class);
        context.startActivity(starter);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ignore_list);
        getSupportActionBar().setTitle(R.string.ignore_list_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fab.setOnClickListener((v) -> createWord());

        adapter = new IgnoreWordAdapter(this, MessageStats.getIgnoreList());
        adapter.setOnClickListener(v -> {
            if (v.getId() == R.id.word_delete) {
                int position = recycler.getChildAdapterPosition((View) v.getParent());
                String word = adapter.getItem(position);
                deleteWord(word);
            }
        });

        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recycler.setAdapter(adapter);
        updateTitle();
        alertInfo();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_ignore_list, menu);

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
        searchView.setOnCloseListener(() -> {
            updateTitle();
            return true;
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_reset:
                MessageStats.resetIgnoreList();
                adapter.getValues().clear();
                adapter.getValues().addAll(MessageStats.getIgnoreList());
                adapter.notifyDataSetChanged();
                updateTitle();
                break;

            case R.id.item_clear:
                adapter.getValues().clear();
                adapter.notifyDataSetChanged();
                updateTitle();

                MessageStats.changeIgnoreList(new ArrayList<>());
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createWord() {
        EditTextDialog dialog = new EditTextDialog(this);
        dialog.setNegativeButton(android.R.string.cancel, null);
        dialog.setPositiveButton(android.R.string.ok, (dialog1, which) -> {
            String word = dialog.getEditText().getText().toString();
            if (!TextUtils.isEmpty(word)) {
                adapter.getValues().add(word.toLowerCase());
                adapter.notifyItemInserted(adapter.getItemCount());
                recycler.scrollToPosition(adapter.getItemCount() - 1);

                MessageStats.changeIgnoreList(adapter.getValues());
                updateTitle();
            }
        });
        dialog.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextInputLayout input = dialog.getInput();
                if (s.toString().contains(" ")) {
                    input.setError(getString(R.string.error_text_has_space));
                    input.setErrorEnabled(true);
                } else {
                    input.setErrorEnabled(false);

                    if (MessageStats.getIgnoreSet().contains(s.toString().toLowerCase())) {
                        input.setError(getString(R.string.error_text_alreary_in_list));
                        input.setErrorEnabled(true);
                    } else {
                        input.setErrorEnabled(false);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        dialog.show();
    }

    private void deleteWord(String word) {
        int cleanIndex = adapter.getValues().indexOf(word);
        adapter.getValues().remove(cleanIndex);
        adapter.notifyItemRemoved(cleanIndex);

        MessageStats.deleteIgnoreWord(word);
        updateTitle();
    }

    private void updateTitle() {
        getSupportActionBar().setSubtitle(String.format(Locale.US,
                "%,d words", adapter.getItemCount()));
    }

    private void alertInfo() {
        if (!SettingsStore.getBoolean("ignore_list_info")) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            builder.setTitle(R.string.ignore_list_title);
            builder.setMessage(R.string.ignore_list_info);
            builder.setPositiveButton(android.R.string.ok, null);
            builder.show();

            SettingsStore.putValue("ignore_list_info", true);
        }
    }
}

package ru.euphoria.doggy;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;
import ru.euphoria.doggy.util.AndroidUtil;

/**
 * Created by admin on 16.04.18.
 */

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {
    protected Toolbar toolbar;
    protected LayoutInflater inflater;
    protected SearchView searchView;
    protected CompositeDisposable composite = new CompositeDisposable();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        onChangeTheme();
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setTitleTextColor(AndroidUtil.getAttrColor(this, android.R.attr.textColorPrimary));
            toolbar.setSubtitleTextColor(AndroidUtil.getAttrColor(this, android.R.attr.textColorSecondary));
        }
        inflater = LayoutInflater.from(this);
        ButterKnife.bind(this);
    }

    public void onChangeTheme() {
        AndroidUtil.changeTheme(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;

            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        composite.dispose();
    }

    @Override
    public void onBackPressed() {
        if (searchView != null && !searchView.isIconified()) {
            searchView.setIconified(true);
            searchView.onActionViewCollapsed();
        } else {
            super.onBackPressed();
        }
    }

    public void toast(String res) {
        AndroidUtil.toast(this, res);
    }

    public void toast(int res) {
        AndroidUtil.toast(this, res);
    }

    public void execute(Runnable runnable) {
        new Thread(runnable).start();
    }
}

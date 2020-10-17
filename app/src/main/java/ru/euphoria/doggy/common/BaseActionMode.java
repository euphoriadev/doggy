package ru.euphoria.doggy.common;

import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

public class BaseActionMode implements androidx.appcompat.view.ActionMode.Callback {
    private androidx.appcompat.view.ActionMode mode;
    private AppCompatActivity activity;

    public BaseActionMode(AppCompatActivity activity) {
        this.activity = activity;
    }

    public void show() {
        mode = activity.startSupportActionMode(this);
    }

    public void click(int position) {
        if (isShowed()) {
            onToggleChecked(position);
        }
    }

    public void longClick(int position) {
        if (!isShowed()) {
            show();
        }
        onToggleChecked(position);
    }

    public boolean isShowed() {
        return mode != null;
    }

    public void onToggleChecked(int position) {
        updateTitle();
        if (getCheckedCount() == 0) {
            mode.finish();
        }
    }

    public int getCheckedCount() {
        return 0;
    }

    public void updateTitle() {
        mode.setTitle(String.valueOf(getCheckedCount()));
    }

    public void finish() {
        if (isShowed()) {
            mode.finish();
        }
    }

    @Override
    public boolean onCreateActionMode(androidx.appcompat.view.ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onPrepareActionMode(androidx.appcompat.view.ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(androidx.appcompat.view.ActionMode mode, MenuItem item) {
        return false;
    }

    @Override
    public void onDestroyActionMode(androidx.appcompat.view.ActionMode mode) {
        this.mode = null;
    }
}

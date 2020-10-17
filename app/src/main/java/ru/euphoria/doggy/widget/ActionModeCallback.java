package ru.euphoria.doggy.widget;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.view.ActionMode;

import ru.euphoria.doggy.adapter.BaseAdapter;

public class ActionModeCallback implements ActionMode.Callback {
    private Context context;
    private BaseAdapter<?, ?> adapter;
    private ActionMode actionMode;

    public ActionModeCallback(Context context, BaseAdapter<?, ?> adapter, ActionMode actionMode) {
        this.context = context;
        this.adapter = adapter;
        this.actionMode = actionMode;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        actionMode = null;
        adapter.getChecked().clear();
        adapter.notifyDataSetChanged();
    }

    public void toggleChecked(int position) {
        adapter.toggleChecked(position);
        actionMode.setTitle(String.valueOf(adapter.getCheckedCount()));

        if (adapter.getCheckedCount() == 0) {
            actionMode.finish();
        }
    }
}

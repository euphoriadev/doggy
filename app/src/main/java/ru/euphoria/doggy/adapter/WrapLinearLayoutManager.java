package ru.euphoria.doggy.adapter;

import android.content.Context;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class WrapLinearLayoutManager extends LinearLayoutManager {
    public WrapLinearLayoutManager(Context context) {
        super(context);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren(recycler, state);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    //
    @Override
    public boolean supportsPredictiveItemAnimations() {
        return false;
    }
}

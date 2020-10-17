package ru.euphoria.doggy.adapter;

import android.content.Context;
import android.util.DisplayMetrics;

import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

public class SpeedyLinearLayoutManager extends WrapLinearLayoutManager {
    private float speedFactor = 0.5f;

    public SpeedyLinearLayoutManager(Context context) {
        super(context);
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        LinearSmoothScroller scroller = new LinearSmoothScroller(recyclerView.getContext()) {
            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                return super.calculateSpeedPerPixel(displayMetrics) * speedFactor;
            }
        };
        scroller.setTargetPosition(position);
        startSmoothScroll(scroller);
    }
}

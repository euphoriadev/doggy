package ru.euphoria.doggy.util;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
    private int space;
    private int spanCount;

    public SpacesItemDecoration(int space, int spanCount) {
        this.space = space;
        this.spanCount = spanCount;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {

        int position = parent.getChildAdapterPosition(view); // item position
        int column = position % spanCount; // item column

        outRect.left = column * space / spanCount; // column * ((1f / spanCount) * spacing)
        outRect.right = space - (column + 1) * space / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
        if (position >= spanCount) {
            outRect.top = space; // item top
        }

    }
}
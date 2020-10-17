package ru.euphoria.doggy.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Outline;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import ru.euphoria.doggy.R;

public class ViewUtil {
    public static final ViewOutlineProvider ALBUM_OUTLINE = new ViewOutlineProvider() {
        @Override
        public void getOutline(View view, Outline outline) {
            float percent = 0.1f;
            int width = view.getWidth();
            int height = view.getHeight();
            outline.setRoundRect(0, 0, width,
                    height, width * percent);
        }
    };

    public static final ViewOutlineProvider ALBUM_OUTLINE_WITH_MARGIN = new ViewOutlineProvider() {
        @Override
        public void getOutline(View view, Outline outline) {
            float percent = 0.02f;
            int width = view.getWidth();
            int height = view.getHeight();
            int margin  = (int) (width * percent);
            outline.setRoundRect(margin, margin, width - margin,
                    height, width * percent);
        }
    };

    private static final int ROOT_CONTENT = android.R.id.content;

    public static int[] swipeRefreshColors(Context context) {
        return new int[]{
                Color.BLACK,
                context.getResources().getColor(R.color.red_600),
                context.getResources().getColor(R.color.blue_a400),
                context.getResources().getColor(R.color.green_a400),
                context.getResources().getColor(R.color.lime_a400),
        };
    }

    public static CardView createCardGroup(Context context, int title) {
        CardView card = (CardView) inflate(context, R.layout.card_group);
        if (Build.VERSION.SDK_INT == 21
                || Build.VERSION.SDK_INT == 22) {
            card.setUseCompatPadding(true);
        }
        TextView view = card.findViewById(R.id.textCard);
        view.setText(title);

        return card;
    }

    public static void addViews(ViewGroup group, View... views) {
        for (View v : views) {
            if (v != null) {
                group.addView(v);
            }
        }
    }

    private static View inflate(Context context, int res) {
        return LayoutInflater.from(context).inflate(res,
                ((Activity) context).findViewById(ROOT_CONTENT),
                false);
    }
}

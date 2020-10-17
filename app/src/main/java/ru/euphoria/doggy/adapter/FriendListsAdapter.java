package ru.euphoria.doggy.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

import ru.euphoria.doggy.R;
import ru.euphoria.doggy.api.model.User;
import ru.euphoria.doggy.common.Pair;
import ru.euphoria.doggy.util.AndroidUtil;

public class FriendListsAdapter extends UsersAdapter {
    private SparseArray<ArrayList<Pair<Integer, String>>> lists;

    public FriendListsAdapter(Context context, ArrayList<User> users) {
        super(context, users);
    }

    public void setLists(SparseArray<ArrayList<Pair<Integer, String>>> lists) {
        this.lists = lists;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        User item = getItem(position);

        holder.screenName.setVisibility(View.GONE);
        holder.lists.setVisibility(View.VISIBLE);

        holder.secondOnline.setVisibility(item.online ? View.VISIBLE : View.GONE);
        holder.secondOnline.setImageDrawable(holder.online.getDrawable());
        if (holder.secondOnline.getVisibility() == View.VISIBLE) {
            holder.secondOnline.setImageTintList(ColorStateList.valueOf(onlineColor));
        }
        holder.online.setVisibility(View.GONE);
        holder.lastSeen.setVisibility(View.GONE);

        int padding = (int) AndroidUtil.px(getContext(), 12);
        holder.container.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        holder.container.setPadding(holder.container.getPaddingLeft(),
                padding, holder.container.getPaddingRight(), padding);

        holder.lists.removeAllViews();

        ArrayList<Pair<Integer, String>> values = lists.valueAt(lists.indexOfKey(item.id));
        for (Pair<Integer, String> value : values) {
            TextView text = (TextView) getInflater().inflate(R.layout.chip_lists, holder.lists, false);
            text.setText(value.second);
            text.setTextColor(getTextColor(value.second));

            GradientDrawable shape = (GradientDrawable) text.getBackground();
            shape.setColor(getBackgroundColor(value.second));

            holder.lists.addView(text);
        }
    }

    private int getTextColor(String name) {
        int color = R.color.chip_best_friends_text;

        switch (name.toLowerCase()) {
            case "best friends":
            case "лучшие друзья":
                color = R.color.chip_best_friends_text;
                break;

            case "colleagues":
            case "коллеги":
                color = R.color.chip_colleagues_text;
                break;

            case "family":
            case "родственники":
                color = R.color.chip_family_text;
                break;

            case "school friends":
            case "друзья по школе":
                color = R.color.chip_school_text;
                break;

            case "university friends":
            case "друзья по вузу":
                color = R.color.chip_university_text;
                break;
        }
        return ContextCompat.getColor(getContext(), color);
    }

    private int getBackgroundColor(String name) {
        int color = R.color.chip_best_friends_background;

        switch (name.toLowerCase()) {
            case "best friends":
            case "лучшие друзья":
                color = R.color.chip_best_friends_background;
                break;

            case "colleagues":
            case "коллеги":
                color = R.color.chip_colleagues_background;
                break;

            case "family":
            case "родственники":
                color = R.color.chip_family_background;
                break;

            case "school friends":
            case "друзья по школе":
                color = R.color.chip_school_background;
                break;

            case "university friends":
            case "друзья по вузу":
                color = R.color.chip_university_background;
                break;
        }
        return ContextCompat.getColor(getContext(), color);
    }
}

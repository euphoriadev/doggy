package ru.euphoria.doggy.adapter;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

import ru.euphoria.doggy.common.Pair;
import ru.euphoria.doggy.db.AppDatabase;
import ru.euphoria.doggy.util.AndroidUtil;
import ru.euphoria.doggy.util.UserUtil;

public class MembersMoreAdapter extends MoreAdapter<Integer> {
    public int[] activeMembers;
    private int primaryColor, hintColor;

    public MembersMoreAdapter(Context context, List<Pair<Integer, Integer>> values) {
        super(context, values);

        this.primaryColor = AndroidUtil.getAttrColor(context, android.R.attr.textColorPrimary);
        this.hintColor = AndroidUtil.getAttrColor(context, android.R.attr.textColorHint);
    }

    public MembersMoreAdapter(Context context, List<Pair<Integer, Integer>> values, int[] members) {
        this(context, values);
        if (members != null) {
            setActiveMembers(members);
        }
    }

    public void setActiveMembers(int[] values) {
        if (values == null) return;

        this.activeMembers = values;
        Arrays.sort(activeMembers);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        Pair<Integer, Integer> item = getItem(position);

        if (activeMembers != null && activeMembers.length > 0) {
            int color = Arrays.binarySearch(activeMembers, item.first) >= 0 ?
                    primaryColor : hintColor;

            holder.word.setTextColor(color);
            holder.count.setTextColor(color);
        }
    }

    @Override
    public String getKey(Pair<Integer, Integer> pair) {
        return pair.first < 0
                ? AppDatabase.database().groups().byId(Math.abs(pair.first)).name
                : UserUtil.getCachedUser(pair.first).toString();
    }
}

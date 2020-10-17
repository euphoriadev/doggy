package ru.euphoria.doggy.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import ru.euphoria.doggy.R;
import ru.euphoria.doggy.api.model.User;
import ru.euphoria.doggy.common.Pair;
import ru.euphoria.doggy.util.AndroidUtil;
import ru.euphoria.doggy.util.UserUtil;

/**
 * Created by admin on 22.05.18.
 */

public class PhonesAdapter extends UsersAdapter {
    public PhonesAdapter(Context context, List<User> users) {
        super(context, users);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        User user = getItem(position);
        String phones = getPhoneLine(user);
        String connections = getConnectionsLine(user);

        holder.screenName.setText(AndroidUtil.join("\n", phones, connections));

        holder.lastSeen.setVisibility(View.GONE);
        holder.online.setVisibility(View.GONE);
        holder.call.setVisibility(View.VISIBLE);

        if (TextUtils.isEmpty(phones)) {
            holder.call.setEnabled(false);
            holder.call.setImageResource(R.drawable.ic_vector_call_hint);
        } else {
            holder.call.setEnabled(true);
            holder.call.setImageResource(R.drawable.ic_vector_call);
            holder.call.setOnClickListener(v -> {
                if (!TextUtils.isEmpty(user.home_phone)) {
                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
                    builder.setTitle(R.string.choice_number);
                    String[] items = {
                            user.mobile_phone, user.home_phone
                    };
                    builder.setItems(items, (dialog, which) -> {
                        AndroidUtil.browsePhone(getContext(), UserUtil
                                .formatNumber(items[which]));
                    });
                    builder.show();

                    return;
                }

                AndroidUtil.browsePhone(getContext(), UserUtil.formatNumber(user.mobile_phone));
            });
        }
    }

    @Override
    public boolean onSearchItem(String query, User value, int position) {
        boolean user_name = super.onSearchItem(query, value, position);
        boolean mobile_phone = !TextUtils.isEmpty(value.mobile_phone) && value.mobile_phone.contains(query);
        boolean home_phone = !TextUtils.isEmpty(value.home_phone) && value.home_phone.contains(query);
        boolean skype = !TextUtils.isEmpty(value.skype) && value.skype.toLowerCase().contains(query);
        boolean facebook = !TextUtils.isEmpty(value.facebook) && value.facebook.toLowerCase().contains(query);
        boolean twitter = !TextUtils.isEmpty(value.twitter) && value.twitter.toLowerCase().contains(query);
        boolean instagram = !TextUtils.isEmpty(value.instagram) && value.instagram.toLowerCase().contains(query);

        return user_name || mobile_phone || home_phone
                || skype || facebook || twitter || instagram;
    }

    public String getConnectionsLine(User user) {
        ArrayList<Pair<String, String>> connections = UserUtil.getConnections(user);
        if (connections.isEmpty()) {
            return "";
        }

        StringBuilder buffer = new StringBuilder();

        Pair<String, String> item = connections.get(0);
        buffer.append(String.format("%s: %s", item.first, item.second));
        for (int i = 1; i < connections.size(); i++) {
            item = connections.get(i);
            buffer.append(String.format("\n%s: %s", item.first, item.second));
        }

        return buffer.toString();
    }

    public ArrayList<String> getPhonesList(User user) {
        boolean first = UserUtil.isPossibleNumber(user.mobile_phone);
        boolean second = UserUtil.isPossibleNumber(user.home_phone);
        if (!first && !second) {
            return new ArrayList<>(0);
        }
        ArrayList<String> list = new ArrayList<>(2);
        if (first) {
            list.add(UserUtil.formatNumber(user.mobile_phone));
        }
        if (second) {
            list.add(UserUtil.formatNumber(user.mobile_phone));
        }
        return list;
    }

    public String getPhoneLine(User user) {
        return TextUtils.join(" | ", getPhonesList(user));
    }
}

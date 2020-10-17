package ru.euphoria.doggy.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import ru.euphoria.doggy.R;
import ru.euphoria.doggy.api.VKApi;
import ru.euphoria.doggy.api.model.User;
import ru.euphoria.doggy.data.SettingsStore;
import ru.euphoria.doggy.util.AndroidUtil;
import ru.euphoria.doggy.util.MessageUtil;
import ru.euphoria.doggy.util.UserUtil;

/**
 * Created by admin on 07.06.18.
 */

public class MembersAdapter extends UsersAdapter {
    private int me;
    private int peer;
    private long admin;

    public MembersAdapter(Context context, int peer, long admin, ArrayList<User> users) {
        super(context, users);
        this.admin = admin;
        this.peer = peer;
        this.me = SettingsStore.getUserId();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        User user = getItem(position);
        User invited = UserUtil.getCachedUser(user.invited_by);

        holder.secondOnline.setVisibility(View.VISIBLE);
        holder.secondOnline.setImageDrawable(holder.online.getDrawable());
        holder.online.setVisibility(View.GONE);
        holder.close.setVisibility((me == admin || me == invited.id)
                ? View.VISIBLE : View.GONE);
        holder.close.setOnClickListener(v -> kickUser(position, user));

        if (user.id == admin) {
            holder.screenName.setText(R.string.chat_admin);
        } else if (invited != null) {
            holder.screenName.setText(getContext().getString(R.string.invited_by, invited.toString()));
        }
    }

    @SuppressLint("CheckResult")
    private void kickUser(int position, User user) {
        MessageUtil.removeChatUser(peer - VKApi.PEER_OFFSET, user.id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> {
                    if (success) {
                        getValues().remove(position);
                        notifyDataSetChanged();
                    }
                }, AndroidUtil.handleError(getContext()));
    }
}

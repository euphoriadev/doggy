package ru.euphoria.doggy.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayout;

import java.util.List;

import ru.euphoria.doggy.R;
import ru.euphoria.doggy.api.Identifiers;
import ru.euphoria.doggy.api.model.User;
import ru.euphoria.doggy.data.SettingsStore;
import ru.euphoria.doggy.util.AndroidUtil;

/**
 * Created by admin on 01.04.18.
 */

public class UsersAdapter extends BaseAdapter<UsersAdapter.ViewHolder, User> {
    protected int onlineColor;
    private int account;

    public UsersAdapter(Context context, List<User> users) {
        super(context, users);
        this.account = SettingsStore.getUserId();
        this.onlineColor = context.getResources().getColor(R.color.online);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.list_item_friend,
                parent, false);

        v.setOnClickListener(this);
        v.setOnLongClickListener(this);
        return new ViewHolder(v);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).id;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        bindViewHolder(getItem(position), holder, position);
    }

    public void bindViewHolder(User user, ViewHolder holder, int position) {
        if (user == null) return;

        switch (user.deactivated) {
            case User.BANNED:
            case User.DELETED:
                holder.name.setTextColor(hintColor);
                holder.screenName.setTextColor(hintColor);
                break;

            default:
                holder.name.setTextColor(primaryColor);
                holder.screenName.setTextColor(secondaryColor);
        }
        holder.name.setText(user.toString());
        if (user.id == account) {
            holder.name.append(getContext().getString(R.string.you));
        }
        String screenName = TextUtils.isEmpty(user.screen_name)
                ? ("@id" + user.id)
                : (user.screen_name);
        if (!TextUtils.isEmpty(user.deactivated)) {
            screenName += " • " + user.deactivated.toUpperCase();
        }
        holder.screenName.setText(screenName);

        if (user.online || user.last_seen != null) {
            int res = 0, color = 0;
            if (user.online) {
                color = onlineColor;
                res = getOnlineIndicatorResource(user);

                holder.lastSeen.setVisibility(View.GONE);
            } else {
                color = hintColor;
                res = getLastSeenOnlineIndicatorResource(user.last_seen.platform);

                holder.lastSeen.setVisibility(View.VISIBLE);
                holder.lastSeen.setText(getLastSeenShortTime(user.last_seen));
            }

            holder.online.setVisibility(View.VISIBLE);
            holder.online.setImageResource(res);
            holder.online.setImageTintList(ColorStateList.valueOf(color));
        }


        AndroidUtil.loadImage(holder.image, user.photo_50);
        holder.checked.setVisibility(isChecked(position) ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onSearchItem(String query, User value, int position) {
        boolean byId = query.charAt(0) == '@';
        if (byId && !TextUtils.isEmpty(value.screen_name)) {
            return value.screen_name
                    .toLowerCase()
                    .contains(query.substring(1));
        } else {
            return value.toString()
                    .toLowerCase()
                    .contains(query);
        }
    }

    public static int getLastSeenOnlineIndicatorResource(int platform) {
        switch (platform) {
            case 1:
                return R.drawable.ic_vector_smartphone;
            case 2:
            case 3:
                return R.drawable.ic_vector_apple;
            case 4:
                return R.drawable.ic_vector_android;
            case 5:
            case 6:
                return R.drawable.ic_vector_win;
            case 7:
                return R.drawable.ic_vector_web;
        }
        return R.drawable.ic_vector_settings;
    }

    public static int getOnlineIndicatorResource(User user) {
        int resource = R.drawable.ic_vector_smartphone;
        if (user.online_mobile) {
            // online from mobile app
            switch (user.online_app) {
                case Identifiers.ANDROID_OFFICIAL:
                    resource = R.drawable.ic_vector_android;
                    break;

                case Identifiers.WP_OFFICIAL:
                case Identifiers.WP_OFFICIAL_NEW:
                case Identifiers.WINDOWS_OFFICIAL:
                    resource = R.drawable.ic_vector_win;
                    break;

                case Identifiers.IPAD_OFFICIAL:
                case Identifiers.IPHONE_OFFICIAL:
                    resource = R.drawable.ic_vector_apple;
                    break;

                case Identifiers.EUPHORIA:
                case Identifiers.DOGGY:
                    resource = R.drawable.ic_vector_pets;
                    break;

                case Identifiers.MP3_MOD:
                    resource = R.drawable.music_note;
                    break;

                default:
                    if (user.online_app > 0) {
                        // other unknown mobile app
                        resource = R.drawable.ic_vector_settings;
                    }
            }
        } else {
            // online from desktop (PC)
            resource = R.drawable.ic_vector_web;
        }

        return resource;
    }

    private String getLastSeenShortTime(User.LastSeen lastSeen) {
        long now = System.currentTimeMillis();
        long time = lastSeen.time * 1000;

        long elapsed = now - time;
        if (elapsed > 31104000000L) {
            return (elapsed / 31104000000L) + " Y";
        }
        if (elapsed > 86400000) {
            return (elapsed / 86400000) + " D";
        }
        if (elapsed > 3600000) {
            return (elapsed / 3600000) + " H";
        }
        if (elapsed > 60000) {
            return (elapsed / 60000) + " M";
        }
        return (elapsed / 1000) + " S";
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout container;
        public FlexboxLayout lists;
        public ImageView image;
        public ImageView cloud;
        public ImageView online;
        public ImageView secondOnline;
        public ImageView call;
        public ImageView close;
        public ImageView checked;
        public TextView name;
        public TextView screenName;
        public TextView lastSeen;
        public TextView time;
        public ProgressBar progress;

        public ViewHolder(View v) {
            super(v);

            this.container = v.findViewById(R.id.container);
            this.lists = v.findViewById(R.id.flexLists);
            this.image = v.findViewById(R.id.user_avatar);
            this.name = v.findViewById(R.id.user_fullname);
            this.screenName = v.findViewById(R.id.user_summary);
            this.lastSeen = v.findViewById(R.id.user_last_seen);
            this.time = v.findViewById(R.id.text_time);
            this.progress = v.findViewById(R.id.progressBar);
            this.cloud = v.findViewById(R.id.user_cloud);
            this.online = v.findViewById(R.id.user_online);
            this.checked = v.findViewById(R.id.user_checked);
            this.secondOnline = v.findViewById(R.id.user_online_second);
            this.call = v.findViewById(R.id.imageCall);
            this.close = v.findViewById(R.id.imageClose);
        }
    }
}

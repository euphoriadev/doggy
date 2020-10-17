package ru.euphoria.doggy.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import ru.euphoria.doggy.R;
import ru.euphoria.doggy.api.model.Chat;
import ru.euphoria.doggy.data.SettingsStore;
import ru.euphoria.doggy.util.AndroidUtil;
import ru.euphoria.doggy.util.ArrayUtil;

public class ChatsAdapter extends BaseAdapter<UsersAdapter.ViewHolder, Chat> {
    public int[] deletedChats;

    public ChatsAdapter(Context context, List<Chat> values) {
        super(context, values);
    }

    @NonNull
    @Override
    public UsersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.list_item_friend,
                parent, false);

        v.setOnClickListener(this);
        v.setOnLongClickListener(this);
        return new UsersAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        Chat item = getItem(position);

        holder.name.setText(item.title);
        holder.screenName.setText(String.format(Locale.getDefault(),
                "id%d", item.id));
        String types = getTypes(item);
        if (!TextUtils.isEmpty(types)) {
            holder.screenName.append(" • " + types);
        }


        holder.online.setVisibility(View.GONE);
        AndroidUtil.loadImage(holder.image, item.photo_50);
    }

    @Override
    public boolean onSearchItem(String query, Chat item, int position) {
        return item.title.toLowerCase().contains(query);
    }

    private String getTypes(Chat chat) {
        ArrayList<String> types = new ArrayList<>(3);
        if (chat.admin_id == SettingsStore.getUserId()) {
            types.add("admin");
        }
        if (chat.kicked) {
            types.add("kicked");
        }
        if (chat.left) {
            types.add("left");
        }
        if (!ArrayUtil.isEmpty(deletedChats)) {
            if (Arrays.binarySearch(deletedChats, chat.id) >= 0) {
                types.add("deleted");
            }
        }

        return TextUtils.join(", ", types);
    }
}

package ru.euphoria.doggy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Locale;

import ru.euphoria.doggy.R;
import ru.euphoria.doggy.api.model.Community;
import ru.euphoria.doggy.util.AndroidUtil;

public class GroupsAdapter extends BaseAdapter<UsersAdapter.ViewHolder, Community> {
    public GroupsAdapter(Context context, List<Community> groups) {
        super(context, groups);
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
        Community item = getItem(position);

        holder.name.setText(item.name);

        holder.screenName.setText(String.format(Locale.getDefault(), "%,d • ", item.members_count));
        switch (item.type) {
            case Community.Type.GROUP:
                holder.screenName.append(getContext().getString(R.string.type_group));
                break;
            case Community.Type.PAGE:
                holder.screenName.append(getContext().getString(R.string.type_page));
                break;
            case Community.Type.EVENT:
                holder.screenName.append(getContext().getString(R.string.type_event));
                break;
        }

        AndroidUtil.loadImage(holder.image, item.photo_50);
        if (isChecked(position)) {
            holder.checked.setVisibility(View.VISIBLE);
        } else {
            holder.checked.setVisibility(View.GONE);
        }
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).id;
    }
}

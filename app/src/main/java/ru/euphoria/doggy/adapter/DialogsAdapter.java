package ru.euphoria.doggy.adapter;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ru.euphoria.doggy.api.ConversationResponse;
import ru.euphoria.doggy.api.model.Community;
import ru.euphoria.doggy.api.model.Conversation;
import ru.euphoria.doggy.api.model.Message;
import ru.euphoria.doggy.api.model.User;
import ru.euphoria.doggy.db.AppDatabase;
import ru.euphoria.doggy.util.AndroidUtil;
import ru.euphoria.doggy.util.UserUtil;

/**
 * Created by admin on 10.05.18.
 */

public class DialogsAdapter extends UsersAdapter {
    private static final int TYPE_USER = 0;
    private static final int TYPE_CHAT = 1;
    private static final int TYPE_GROUP = 2;

    public ArrayList<Conversation> conversation;
    public List<Message> messages;

    public DialogsAdapter(Context context, ConversationResponse response) {
        super(context, response.users());
        this.conversation = response.items();
        this.messages = response.lastMessages();
    }

    @Override
    public int getItemCount() {
        return conversation.size();
    }

    @Override
    public User getItem(int position) {
        int type = getItemViewType(position);
        if (type == TYPE_USER) {
            return UserUtil.getCachedUser(conversation.get(position).peer.id);
        }
        return User.EMPTY;
    }

    @Override
    public long getItemId(int position) {
        return messages.get(position).id;
    }

    @Override
    public int getItemViewType(int position) {
        Conversation item = conversation.get(position);

        switch (item.peer.type) {
            case "user":
                return TYPE_USER;
            case "chat":
                return TYPE_CHAT;
            case "group":
                return TYPE_GROUP;
        }
        return TYPE_USER;
    }

    public Conversation getConversation(int position) {
        return conversation.get(position);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int type = getItemViewType(position);
        if (type == TYPE_USER) {
            super.onBindViewHolder(holder, position);
        } else {
            Conversation conversation = this.conversation.get(position);
            if (type == TYPE_CHAT) {
                holder.name.setText(conversation.chat_settings.title);
                holder.screenName.setText(String.format(Locale.getDefault(),
                        "id%d", conversation.peer.id));
                holder.online.setVisibility(View.GONE);
                AndroidUtil.loadImage(holder.image, conversation.chat_settings.photo.photo_50);
            } else if (type == TYPE_GROUP) {
                Community group = AppDatabase.database().groups().byId(conversation.peer.local_id);
                holder.name.setText(group.name);

                holder.screenName.setText(String.format(Locale.getDefault(),
                        "id%d", conversation.peer.local_id));

                AndroidUtil.loadImage(holder.image, group.photo_50);
            }
            holder.lastSeen.setVisibility(View.GONE);
        }

        if (getChecked().contains(position)) {
            holder.checked.setVisibility(View.VISIBLE);
        } else {
            holder.checked.setVisibility(View.GONE);
        }
    }
}

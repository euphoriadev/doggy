package ru.euphoria.doggy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.euphoria.doggy.R;
import ru.euphoria.doggy.api.model.AudioMessage;
import ru.euphoria.doggy.api.model.Community;
import ru.euphoria.doggy.api.model.Message;
import ru.euphoria.doggy.api.model.User;
import ru.euphoria.doggy.db.AppDatabase;
import ru.euphoria.doggy.util.GroupUtil;
import ru.euphoria.doggy.util.TimeUtil;
import ru.euphoria.doggy.util.UserUtil;

public class VoiceAdapter extends BaseAdapter<VoiceAdapter.ViewHolder, AudioMessage> {
    private DateFormat dateFormat;

    public VoiceAdapter(Context context, List<AudioMessage> values) {
        super(context, values);
        this.dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.list_item_voice,
                parent, false);

        v.setOnClickListener(this);
        v.setOnLongClickListener(this);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        AudioMessage item = getItem(position);
        Message msg = AppDatabase.database().messages().byId(item.msg_id);

        holder.title.setText(dateFormat.format(msg.date * 1000L));
        holder.subtitle.setText(getContext().getString(R.string.audios_subtitle,
                getOwnerName(item), TimeUtil.formatSeconds(item.duration)));

        holder.overflow.setOnClickListener(v -> {
            if (overflowClickListener != null) {
                overflowClickListener.onClick(holder.overflow, position);
            }
        });
    }

    public static String getOwnerName(AudioMessage voice) {
        int owner = voice.owner_id;
        User user = UserUtil.getCachedUser(owner);
        if (user != null) {
            return user.toString();
        }
        Community group = GroupUtil.getCachedGroup(owner);
        if (group != null) {
            return group.name;
        }
        return "Unknown";
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.voice_title) TextView title;
        @BindView(R.id.voice_subtitle) TextView subtitle;
        @BindView(R.id.overflow) ImageView overflow;

        public ViewHolder(@NonNull View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}

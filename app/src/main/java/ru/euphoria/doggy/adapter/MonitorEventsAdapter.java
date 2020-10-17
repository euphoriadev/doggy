package ru.euphoria.doggy.adapter;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.euphoria.doggy.api.model.LongPollEvent;
import ru.euphoria.doggy.api.model.User;

public class MonitorEventsAdapter extends UsersAdapter {
    public ArrayList<LongPollEvent> events = new ArrayList<>();
    private DateFormat format;

    public MonitorEventsAdapter(Context context, List<User> users) {
        super(context, users);
        this.format = SimpleDateFormat.getTimeInstance(DateFormat.SHORT);
    }

    public void setEvents(ArrayList<LongPollEvent> events) {
        this.events = events;
    }

    @Override
    public User getItem(int position) {
        return events.get(position).user;
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        LongPollEvent event = events.get(position);

        holder.lastSeen.setVisibility(View.GONE);
        holder.online.setVisibility(View.GONE);
        holder.secondOnline.setVisibility(View.GONE);

        holder.screenName.setMaxLines(3);
        holder.screenName.setSingleLine(false);
        holder.screenName.setText(event.text);

        holder.time.setText(format.format(new Date(event.time)));
    }

    public void insert(LongPollEvent event) {
        events.add(0, event);
        notifyItemInserted(0);
    }
}

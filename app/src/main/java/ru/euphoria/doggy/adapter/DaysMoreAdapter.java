package ru.euphoria.doggy.adapter;

import android.content.Context;
import android.text.Html;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.text.DateFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ru.euphoria.doggy.common.Pair;

public class DaysMoreAdapter extends MoreAdapter<Long> {
    private DateFormat format;

    public DaysMoreAdapter(Context context, List<Pair<Long, Integer>> values) {
        super(context, values);
        format = DateFormat.getDateInstance();
    }

    @Override
    public String getKey(Pair<Long, Integer> pair) {
        return format.format(TimeUnit.DAYS.toMillis(pair.first));
    }

    @Override
    public void onBindViewHolder(@NonNull MoreAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        int count = getItem(position).second;

        if (enableArrows && position != (getItemCount() - 1)) {
            int previous = getItem(position + 1).second;
            if (previous < count) {
                holder.count.append("<font color='green'> ▲</font>");
            } else if (previous > count) {
                holder.count.append("<font color='red'> ▼</font>");
            }
            holder.count.setText(Html.fromHtml(holder.count.getText().toString()),
                    TextView.BufferType.SPANNABLE);
        }
    }
}

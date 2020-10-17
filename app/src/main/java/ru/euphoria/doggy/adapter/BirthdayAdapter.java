package ru.euphoria.doggy.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.text.DateFormatSymbols;
import java.util.ArrayList;

import ru.euphoria.doggy.R;
import ru.euphoria.doggy.api.model.User;

public class BirthdayAdapter extends BaseAdapter<BirthdayAdapter.ViewHolder, BirthdayAdapter.SectionRow> {
    private UsersAdapter baseAdapter;
    private String[] months;

    public BirthdayAdapter(Context context, ArrayList<SectionRow> values) {
        super(context, values);

        this.baseAdapter = new UsersAdapter(context, null) {
            @Override
            public long getItemId(int position) {
                return BirthdayAdapter.this.getItemId(position);
            }
        };
        this.months = DateFormatSymbols.getInstance().getMonths();
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).header ? SectionRow.TYPE_HEADER : SectionRow.TYPE_ITEM;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int res = viewType == SectionRow.TYPE_HEADER
                ? R.layout.list_item_birthday_header
                : R.layout.list_item_birthday;
        View v = getInflater().inflate(res, parent, false);
        if (viewType == SectionRow.TYPE_ITEM) {
            v.setOnClickListener(this);
        }
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SectionRow item = getItem(position);
        if (item.header) {
            holder.header.setText(item.month);
        } else {
            baseAdapter.bindViewHolder(item.user, holder, position);
            if (!TextUtils.isEmpty(item.user.birthday)) {
                String[] split = item.user.birthday.split("\\.");
                String birthday = split[0] + " " + months[Integer.parseInt(split[1]) - 1];
                holder.screenName.setText(birthday);
            } else {
                holder.screenName.setText(R.string.unknown);
            }

        }
    }

    static class ViewHolder extends UsersAdapter.ViewHolder {
        public TextView header;

        public ViewHolder(View v) {
            super(v);
            this.header = v.findViewById(R.id.header);
        }
    }

    public static class SectionRow {
        public static final int TYPE_HEADER = 0;
        public static final int TYPE_ITEM = 1;

        public String month;
        public User user;
        public boolean header;

        public SectionRow(User user, String month, boolean header) {
            this.user = user;
            this.header = header;
            this.month = month;
        }
    }
}

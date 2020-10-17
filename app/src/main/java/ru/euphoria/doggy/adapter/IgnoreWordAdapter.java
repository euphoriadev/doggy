package ru.euphoria.doggy.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ru.euphoria.doggy.R;

public class IgnoreWordAdapter extends BaseAdapter<IgnoreWordAdapter.ViewHolder, String> {

    public IgnoreWordAdapter(Context context, List<String> values) {
        super(context, values);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = getInflater().inflate(R.layout.list_item_ignore_word, parent, false);
        v.setOnClickListener(this);

        ViewHolder holder = new ViewHolder(v);
        holder.delete.setOnClickListener(this);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        holder.text.setText(getItem(position));
    }

    @Override
    public boolean onSearchItem(String query, String item, int position) {
        return item.contains(query);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView text;
        public ImageButton delete;

        public ViewHolder(@NonNull View v) {
            super(v);

            this.text = v.findViewById(R.id.word_text);
            this.delete = v.findViewById(R.id.word_delete);
        }
    }
}

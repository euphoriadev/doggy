package ru.euphoria.doggy.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;

import ru.euphoria.doggy.R;
import ru.euphoria.doggy.SettingsFragment;
import ru.euphoria.doggy.common.Pair;
import ru.euphoria.doggy.data.SettingsStore;
import ru.euphoria.doggy.util.MessageStats;

/**
 * Created by admin on 04.05.18.
 */

public class MoreAdapter<E> extends BaseAdapter<MoreAdapter.ViewHolder, Pair<E, Integer>> {
    private static final Matcher explicit = MessageStats.explicitPattern().matcher("");

    protected boolean enableArrows;
    private boolean night;
    private boolean countKeys;


    public MoreAdapter(Context context, List<Pair<E, Integer>> values) {
        super(context, values);
        this.night = SettingsStore.getBoolean(SettingsFragment.KEY_NIGHT_MODE);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = getInflater().inflate(R.layout.row_top_words, parent,
                false);
        v.setOnClickListener(this);
        v.setOnLongClickListener(this);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MoreAdapter.ViewHolder holder, int position) {
        Pair<E, Integer> pair = getItem(position);
        int value = pair.second;

        if (position % 2 == 0) {
            holder.line.setBackgroundColor(night ? 0xFF2E3439 : 0xFFEEEEEE);
        } else {
            holder.line.setBackground(null);
        }
        holder.explicit.setVisibility(View.INVISIBLE);

        String key = getKey(pair);
        if (countKeys) {
            explicit.reset(key);
            if (explicit.find()) {
                holder.explicit.setVisibility(View.VISIBLE);
            }

            key = (position + 1) + ". " + key;
        }

        holder.word.setText(key);
        holder.count.setText(String.valueOf(value));
    }

    public String getKey(Pair<E, Integer> pair) {
        return String.valueOf(pair.first);
    }

    @Override
    public boolean onSearchItem(String query, Pair<E, Integer> value, int position) {
        return getKey(value).contains(query);
    }

    public void sort(Comparator<Pair<E, Integer>> comparator) {
        Collections.sort(getValues(), comparator);
        notifyDataSetChanged();
    }

    public void setCountKeys(boolean countKeys) {
        this.countKeys = countKeys;
    }

    public void setEnableArrows(boolean enableArrows) {
        this.enableArrows = enableArrows;
    }

    public static Comparator<Pair<Long, Integer>> byDate() {
        return DATE_COMPARATOR;
    }

    public static Comparator<Pair<String, Integer>> byWords() {
        return WORDS_COMPARATOR;
    }

    public static Comparator<Pair<Long, Integer>> byCount() {
        return COUNT_COMPARATOR;
    }

    public static Comparator<Pair<String, Integer>> byLength() {
        return LENGTH_COMPARATOR;
    }

    public static Comparator<Pair<String, Integer>> byExplicit() {
        return EXPLICIT_COMPARATOR;
    }

    private static final Comparator<Pair<Long, Integer>> DATE_COMPARATOR =
            (o1, o2) -> Long.compare(o2.first, o1.first);
    private static final Comparator<Pair<Long, Integer>> COUNT_COMPARATOR =
            (o1, o2) -> Long.compare(o2.second, o1.second);
    private static final Comparator<Pair<String, Integer>> WORDS_COMPARATOR =
            (o1, o2) -> o1.first.compareTo(o2.first);
    private static final Comparator<Pair<String, Integer>> LENGTH_COMPARATOR =
            (o1, o2) -> Integer.compare(o2.first.length(), o1.first.length());
    private static final Comparator<Pair<String, Integer>> EXPLICIT_COMPARATOR = (o1, o2) -> {
        boolean b1 = explicit.reset(o1.first).find();
        boolean b2 = explicit.reset(o2.first).find();
        return Boolean.compare(b2, b1);
    };

    static class ViewHolder extends RecyclerView.ViewHolder {
        protected LinearLayout line;
        protected TextView word;
        protected TextView count;
        protected ImageView explicit;

        public ViewHolder(View v) {
            super(v);

            this.line = (LinearLayout) v;
            this.word = v.findViewById(R.id.textWord);
            this.count = v.findViewById(R.id.textCount);
            this.explicit = v.findViewById(R.id.img_explicit);
        }
    }
}

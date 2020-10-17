package ru.euphoria.doggy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import ru.euphoria.doggy.util.AndroidUtil;

/**
 * Created by admin on 10.05.18.
 */

public class BaseAdapter<H extends RecyclerView.ViewHolder, E>
        extends RecyclerView.Adapter<H>
        implements View.OnClickListener, View.OnLongClickListener {
    private Context context;
    private LayoutInflater inflater;
    private View.OnClickListener listener;
    private View.OnLongClickListener longClickListener;
    protected OnOverflowClickListener overflowClickListener;
    private List<E> values;
    private List<E> cleanValues;
    private List<Long> checked = new ArrayList<>();
    private String query;

    public int primaryColor;
    public int secondaryColor;
    public int hintColor;

    public BaseAdapter(Context context, List<E> values) {
        this.context = context;
        this.values = values;
        this.inflater = LayoutInflater.from(context);

        primaryColor = AndroidUtil.getAttrColor(context, android.R.attr.textColorPrimary);
        secondaryColor = AndroidUtil.getAttrColor(context, android.R.attr.textColorSecondary);
        hintColor = AndroidUtil.getAttrColor(context, android.R.attr.textColorHint);
    }

    @NonNull
    @Override
    public H onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull H holder, int position) {

    }

    public E getItem(int position) {
        return values.get(position);
    }

    @Override
    public int getItemCount() {
        return values.size();
    }

    public LayoutInflater getInflater() {
        return inflater;
    }

    public Context getContext() {
        return context;
    }

    public void setValues(List<E> values) {
        this.values = values;
    }

    public List<E> getValues() {
        return values;
    }

    public List<E> getCleanValues() {
        return cleanValues != null ? cleanValues : values;
    }

    public void setOnClickListener(View.OnClickListener l) {
        this.listener = l;
    }

    public void setLongClickListener(View.OnLongClickListener l) {
        this.longClickListener = l;
    }

    public void setOverflowClickListener(AudiosAdapter.OnOverflowClickListener l) {
        this.overflowClickListener = l;
    }

    public void checkAll() {
        checked.clear();
        for (int i = 0; i < getItemCount(); i++) {
            checked.add(getItemId(i));
        }
        notifyDataSetChanged();
    }

    public List<Long> getChecked() {
        return checked;
    }

    public boolean isChecked(int position) {
        return checked.contains(getItemId(position));
    }

    public int getCheckedCount() {
        return checked.size();
    }

    public void toggleChecked(int position) {
        long itemId = getItemId(position);

        if (checked.contains(itemId)) {
            checked.remove(checked.indexOf(itemId));
        } else {
            checked.add(itemId);
        }
        notifyItemChanged(position);
    }

    public boolean onSearchItem(String query, E item, int position) {
        return false;
    }

    public String query() {
        return query;
    }

    public void filter(Predicate<E> predicate) {
        if (cleanValues == null) {
            cleanValues = new ArrayList<>(values);
        }
        values.clear();

        for (E value : cleanValues) {
            if (predicate.test(value)) {
                values.add(value);
            }
        }
        notifyDataSetChanged();
    }

    public void search(String query) {
        this.query = query.toLowerCase();
        if (cleanValues == null) {
            cleanValues = new ArrayList<>(values);
        }
        values.clear();
        if (query.isEmpty()) {
            values.addAll(cleanValues);
            notifyDataSetChanged();
            return;
        }

        for (int i = 0; i < cleanValues.size(); i++) {
            E item = cleanValues.get(i);
            if (onSearchItem(query, item, i)) {
                values.add(item);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        if (listener != null) {
            listener.onClick(v);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        return longClickListener != null && longClickListener.onLongClick(v);
    }

    public interface OnOverflowClickListener {
        void onClick(View v, int position);
    }
}

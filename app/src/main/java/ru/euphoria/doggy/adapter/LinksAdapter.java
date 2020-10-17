package ru.euphoria.doggy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.euphoria.doggy.R;
import ru.euphoria.doggy.api.model.Link;
import ru.euphoria.doggy.util.AndroidUtil;

public class LinksAdapter extends BaseAdapter<LinksAdapter.ViewHolder, Link> {

    public LinksAdapter(Context context, List<Link> values) {
        super(context, values);
    }

    @NonNull
    @Override
    public LinksAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.list_item_link,
                parent, false);

        v.setOnClickListener(this);
        v.setOnLongClickListener(this);
        return new LinksAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        Link link = getItem(position);

        holder.title.setText(link.title);
        holder.description.setText(link.caption);

        if (link.photo != null && link.photo.sizes != null) {
            AndroidUtil.loadImage(holder.image, link.photo.sizes.get(0).src);
        } else {
            holder.image.setImageResource(R.drawable.circle_link_placeholder);
        }
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).url.hashCode();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.link_image) ImageView image;
        @BindView(R.id.link_title) TextView title;
        @BindView(R.id.link_description) TextView description;
        @BindView(R.id.link_arrow) ImageView arrow;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}

package ru.euphoria.doggy.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.data.Feature;
import com.google.maps.android.data.Point;

import java.util.List;

import ru.euphoria.doggy.R;
import ru.euphoria.doggy.util.AndroidUtil;

public class SearchPlaceAdapter extends BaseAdapter<SearchPlaceAdapter.ViewHolder, Feature> {
    private Location location;

    public SearchPlaceAdapter(Context context, List<Feature> values, Location location) {
        super(context, values);
        this.location = location;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = getInflater().inflate(R.layout.list_item_place, parent,
                false);
        v.setOnClickListener(this);
        return new ViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        Feature item = getItem(position);

        holder.title.setText(item.getProperty("display_name"));
        holder.type.setText(item.getProperty("type"));

        if (location != null) {
            holder.distance.setVisibility(View.VISIBLE);
            float[] meters = new float[1];

            Point point = (Point) item.getGeometry();
            LatLng coordinates = point.getGeometryObject();
            Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                    coordinates.latitude, coordinates.longitude, meters);

            holder.distance.setText(AndroidUtil.formatDistance(getContext(),
                    Math.round(meters[0])));

        } else {
            holder.distance.setVisibility(View.GONE);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView type;
        public TextView distance;

        public ViewHolder(@NonNull View v) {
            super(v);
            this.title = v.findViewById(R.id.place_title);
            this.type = v.findViewById(R.id.place_type);
            this.distance = v.findViewById(R.id.place_distance);
        }
    }
}

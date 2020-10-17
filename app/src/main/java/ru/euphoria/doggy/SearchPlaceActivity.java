package ru.euphoria.doggy;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.maps.android.data.Feature;
import com.google.maps.android.data.Point;
import com.jakewharton.rxbinding3.widget.RxTextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import ru.euphoria.doggy.adapter.SearchPlaceAdapter;
import ru.euphoria.doggy.adapter.WrapLinearLayoutManager;
import ru.euphoria.doggy.data.geojson.GeoJsonParser;
import ru.euphoria.doggy.util.AndroidUtil;

@SuppressWarnings("CheckResult")
public class SearchPlaceActivity extends BaseActivity {
    public static final int REQUEST_CODE_SEARCH = 100;

    @BindView(R.id.search_bar)
    EditText searchBar;
    @BindView(R.id.recycler_view)
    RecyclerView recycler;

    private Location myLocation;
    private SearchPlaceAdapter adapter;

    public static void start(Activity context, Location location) {
        Intent starter = new Intent(context, SearchPlaceActivity.class);
        starter.putExtra("location", location);
        context.startActivityForResult(starter, REQUEST_CODE_SEARCH);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(AndroidUtil.getAttrColor(this, android.R.attr.colorPrimary));
        setContentView(R.layout.activity_search_place);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recycler.setLayoutManager(new WrapLinearLayoutManager(this));
        recycler.setHasFixedSize(true);

        myLocation = getIntent().getParcelableExtra("location");
        handleSearchTextChanges();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    private void refreshAdapter(ArrayList<Feature> places) {
        if (adapter == null) {
            adapter = new SearchPlaceAdapter(this, places, myLocation);
            recycler.setAdapter(adapter);
        } else {
            adapter.getValues().clear();
            adapter.getValues().addAll(places);
            adapter.notifyDataSetChanged();
        }
        adapter.setOnClickListener(v -> {
            int position = recycler.getChildAdapterPosition(v);
            Feature item = adapter.getItem(position);

            Point point = (Point) item.getGeometry();

            Intent data = new Intent();
            data.putExtra("coordinates", point.getGeometryObject());
            setResult(RESULT_OK, data);
            finish();
        });
    }

    private void handleSearchTextChanges() {
        RxTextView.textChanges(searchBar)
                .debounce(300, TimeUnit.MILLISECONDS)
                .filter(AndroidUtil::nonEmpty)
                .map(String::valueOf)
                .distinctUntilChanged()
                .switchMap(s -> searchPlace(s).toObservable())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::refreshAdapter, AndroidUtil.handleError(this));
    }

    private Single<ArrayList<Feature>> searchPlace(String q) {
        return AndroidUtil.request("https://nominatim.openstreetmap.org/search?q=" + q + "&format=geojson")
                .flatMap(s -> Single.just(GeoJsonParser.parse(new JSONObject(s))));
    }

}

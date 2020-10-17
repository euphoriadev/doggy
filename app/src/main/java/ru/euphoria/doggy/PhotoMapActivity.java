package ru.euphoria.doggy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.yandex.metrica.YandexMetrica;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnLongClick;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ru.euphoria.doggy.api.VKApi;
import ru.euphoria.doggy.api.model.Photo;
import ru.euphoria.doggy.api.model.PhotoSizes;
import ru.euphoria.doggy.common.DebugLog;
import ru.euphoria.doggy.common.Pair;
import ru.euphoria.doggy.data.SettingsStore;
import ru.euphoria.doggy.db.AppDatabase;
import ru.euphoria.doggy.image.MultiDrawable;
import ru.euphoria.doggy.image.SquareTransformation;
import ru.euphoria.doggy.util.AndroidUtil;

@SuppressLint("CheckResult")
public class PhotoMapActivity extends BaseActivity
        implements OnMapReadyCallback, ClusterManager.OnClusterItemClickListener<Photo>,
        ClusterManager.OnClusterClickListener<Photo> {
    private static final String TAG = "PhotoMapActivity";
    private static final char PREVIEW_PHOTO_SIZE = PhotoSizes.PhotoSize.S;
    private static final char REQUEST_CODE_SEARCH = SearchPlaceActivity.REQUEST_CODE_SEARCH;

    private static final String KEY_CAMERA_LAT = "photo_map_camera_lat";
    private static final String KEY_CAMERA_LONG = "photo_map_camera_long";
    private static final String KEY_CAMERA_ZOOM = "photo_map_camera_zoom";
    private static final String KEY_CAMERA_BEARING = "photo_map_camera_bearing";
    private static final String KEY_CAMERA_TILT = "photo_map_camera_tilt";

    @BindView(R.id.map) MapView mapView;
    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.fabProgress) ProgressBar fabProgress;
    @BindView(R.id.searchBarContainer) FrameLayout searchBarContainer;
    @BindView(R.id.search_bar) EditText searchBar;

    private Set<Photo> mapPhotos = Collections.synchronizedSet(new HashSet<>());
    private ClusterManager<Photo> clusterManager;
    private LocationListener listener;
    private Location lastLocation;
    private Marker placeMarker;
    private GoogleMap map;
    private boolean locationGranted;
    private int statusBarHeight;


    public static void start(Context context) {
        Intent starter = new Intent(context, PhotoMapActivity.class);
        context.startActivity(starter);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_map);

        if (AndroidUtil.checkLocationPermissions(this)) {
            locationGranted = true;
            getLocation();
        }
        statusBarHeight = AndroidUtil.getStatusBarHeight(this);
        searchBarContainer.setPadding(0, statusBarHeight, 0, 0);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        alert();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_SEARCH) {
            LatLng coordinates = data.getParcelableExtra("coordinates");
            map.animateCamera(CameraUpdateFactory.newLatLng(coordinates));

            if (placeMarker != null) {
                placeMarker.remove();
            }
            placeMarker = map.addMarker(new MarkerOptions().position(coordinates));

        }
    }

    @OnLongClick(R.id.fab)
    public boolean onFabLongClick(View v) {
        createFabOptionsDialog();
        return true;
    }

    @OnClick(R.id.fab)
    public void onFabClick(View v) {
        VisibleRegion region = map.getProjection().getVisibleRegion();
        LatLng center = region.latLngBounds.getCenter();
        float radius = getRadius(region);

        getPhotos(center.latitude, center.longitude, Math.round(radius));
    }

    @OnClick(R.id.search_bar)
    public void onSearchBarClick(View v) {
        SearchPlaceActivity.start(this, lastLocation);
        overridePendingTransition(0, 0);
    }

    @Override
    public void onChangeTheme() {
        boolean night = SettingsStore.getBoolean(SettingsFragment.KEY_NIGHT_MODE);
        setTheme(night ? R.style.AppTheme_Dark_PhotoMap
                : R.style.AppTheme_PhotoMap);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        if (map != null) {
            saveCameraPosition(map.getCameraPosition());
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        mapClear();

        AppContext.imageCache.clear();

        if (listener != null) {
            LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
            lm.removeUpdates(listener);
            listener = null;
        }
        Runtime.getRuntime().gc();
    }

    @Override
    public void onBackPressed() {
        if (placeMarker != null) {
            placeMarker.remove();
            placeMarker = null;
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocation();
            locationGranted = true;
            map.setMyLocationEnabled(locationGranted);

        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap map) {
        if (map == null) {
            toast("К сожалению карта недоступна. Возможно вам надо обновить Google Play Services?");
            return;
        }

        this.map = map;

        map.setPadding(0, statusBarHeight, 0, 0);
        map.setMyLocationEnabled(locationGranted);

        CameraPosition position = restoreCameraPosition();
        if (position != null) {
            map.moveCamera(CameraUpdateFactory.newCameraPosition(position));
        }
        if (SettingsStore.nightMode()) {
            MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle_night);
            map.setMapStyle(style);
        }

        clusterManager = new ClusterManager<>(this, map);
        clusterManager.setRenderer(new PhotoRenderer(this, map, clusterManager));
        clusterManager.setOnClusterItemClickListener(this);
        clusterManager.setOnClusterClickListener(this);

        map.setOnCameraIdleListener(clusterManager);
        map.setOnMarkerClickListener(clusterManager);
        map.setOnInfoWindowClickListener(clusterManager);
    }

    @Override
    public boolean onClusterItemClick(Photo item) {
        PhotoViewerActivity.start(this, item);
        return true;
    }

    @Override
    public boolean onClusterClick(Cluster<Photo> cluster) {
        ClusterPhotosActivity.start(this, (ArrayList<Photo>) cluster.getItems());
        return true;
    }

    private void alert() {
        if (!SettingsStore.getBoolean("photo_map_first_alert", false)) {
            SettingsStore.putValue("photo_map_first_alert", true);

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            builder.setTitle(R.string.item_photos_map);
            builder.setMessage(R.string.photo_map_description);
            builder.setPositiveButton(android.R.string.ok, null);

            builder.show();
        }
    }

    private void saveCameraPosition(CameraPosition position) {
        SettingsStore.putValue(KEY_CAMERA_LAT, (float) position.target.latitude);
        SettingsStore.putValue(KEY_CAMERA_LONG, (float) position.target.longitude);
        SettingsStore.putValue(KEY_CAMERA_ZOOM, position.zoom);
        SettingsStore.putValue(KEY_CAMERA_BEARING, position.bearing);
        SettingsStore.putValue(KEY_CAMERA_TILT, position.tilt);
    }

    private CameraPosition restoreCameraPosition() {
        if (!SettingsStore.has(KEY_CAMERA_LAT)) {
            return null;
        }

        return new CameraPosition.Builder()
                .target(new LatLng(
                        SettingsStore.getFloat(KEY_CAMERA_LAT),
                        SettingsStore.getFloat(KEY_CAMERA_LONG))
                )
                .zoom(SettingsStore.getFloat(KEY_CAMERA_ZOOM))
                .bearing(SettingsStore.getFloat(KEY_CAMERA_BEARING))
                .tilt(SettingsStore.getFloat(KEY_CAMERA_TILT))
                .build();
    }

    private void createFabOptionsDialog() {
        String[] items = new String[]{
                "Все фотографии",
                "Только из диалогов",
        };

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Загрузить из кеша");
        builder.setItems(items, (dialog, which) -> {
            LiveData<List<Photo>> cachedPhotos = null;

            switch (which) {
                case 0:
                    cachedPhotos = AppDatabase.database().photos().onlyGeo();
                    break;
                case 1:
                    cachedPhotos = AppDatabase.database().photos().onlyDialogs();
                    break;
            }
            if (cachedPhotos == null) return;

            cachedPhotos.observe(this, value -> {
                toast("Loading " + value.size() + " photos from db");
                addPhotosToCluster(value, 100);
            });
        });

        builder.setNegativeButton("Очистить карту", (dialog, which) -> {
            mapClear();
            composite.dispose();
        });
        builder.show();
    }

    private void mapClear() {
        mapPhotos.clear();
        if (clusterManager != null) {
            clusterManager.clearItems();
        }
        map.clear();
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                lastLocation = location;
                AndroidUtil.saveLocation(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1_000, 0, listener);
    }

    private void addPhotosToCluster(Collection<Photo> photos) {
        addPhotosToCluster(photos, 10);
    }

    private void addPhotosToCluster(Collection<Photo> photos, int step) {
        fabProgress.setVisibility(View.VISIBLE);
        fabProgress.setIndeterminate(false);
        fabProgress.setMax(photos.size());
        fabProgress.setProgress(0);

        AtomicInteger counter = new AtomicInteger();
        DebugLog.w(TAG, "total photos: " + photos.size());

        Disposable subscribe = Flowable.fromIterable(photos)
                .parallel()
                .runOn(Schedulers.io())
                .filter(photo -> !mapPhotos.contains(photo))
                .flatMap(photo -> {
                    mapPhotos.add(photo);

                    String src = getPreviewUrl(photo);
                    DebugLog.w(TAG, "Loading image " + counter.get() +" " + src);

                    Bitmap image = getBitmap(src);
                    if (image == null) {
                        try {
                            image = loadImage(src).get();
                            AppContext.imageCache.set(src, image);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    counter.incrementAndGet();

                    clusterManager.addItem(photo);
                    return Flowable.just(Pair.create(src, image));
                })
                .sequential()
                .doOnComplete(() -> runOnUiThread(() -> {
                    fabProgress.setProgress(0);
                    fabProgress.setVisibility(View.INVISIBLE);
                    clusterManager.cluster();

                }))
                .subscribe(pair -> {
                    if (counter.get() >= step) {
                        counter.set(0);
                        runOnUiThread(() -> clusterManager.cluster());
                    }
                    fabProgress.post(() -> fabProgress.incrementProgressBy(1));
                });
        composite.add(subscribe);

    }

    private RequestCreator loadImage(String src) {
        return Picasso.get()
                .load(src)
                .config(Bitmap.Config.RGB_565)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .transform(new SquareTransformation());
    }

    private String getPreviewUrl(Photo photo) {
        return photo.sizes.of(PREVIEW_PHOTO_SIZE).src;
    }

    private void getPhotos(double lat, double lng, int radius) {
        if (!AndroidUtil.hasConnection()) {
            AndroidUtil.toastErrorConnection(this);
            return;
        }
        fabProgress.setVisibility(View.VISIBLE);
        fabProgress.setIndeterminate(true);

        YandexMetrica.reportEvent("Карта фотографий");
        VKApi.photos().search()
                .lat(lat)
                .lng(lng)
                .radius(radius)
                .count(1000)
                .async(Photo.class)
                .flatMap(photos -> {
                    AppDatabase.database().photos().insert(photos);
                    return Flowable.just(photos);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::addPhotosToCluster, AndroidUtil.handleError(this));
    }

    public float getRadius(VisibleRegion region) {
        float[] result = new float[1];

        Location.distanceBetween(
                region.farLeft.latitude,
                region.farLeft.longitude,
                region.farRight.latitude,
                region.farRight.longitude, result);
        return result[0] / 2f;
    }

    private class PhotoRenderer extends DefaultClusterRenderer<Photo> implements GoogleMap.OnCameraIdleListener {
        private final IconGenerator iconGenerator = new IconGenerator(AppContext.context);
        private final IconGenerator clusterIconGenerator = new IconGenerator(AppContext.context);

        private ImageView imageView;
        private ImageView clusterImageView;
        private int dimension, padding;

        public PhotoRenderer(Context context, GoogleMap map, ClusterManager<Photo> clusterManager) {
            super(context, map, clusterManager);

            View multiPhoto = View.inflate(context, R.layout.multi_profile, null);
            this.clusterImageView = multiPhoto.findViewById(R.id.image);
            this.clusterIconGenerator.setContentView(multiPhoto);

            padding = (int) getResources().getDimension(R.dimen.custom_profile_padding);
            dimension = (int) getResources().getDimension(R.dimen.custom_profile_image);

            imageView = new ImageView(context);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(dimension, dimension));
            imageView.setScaleType(ImageView.ScaleType.FIT_START);
            imageView.setPadding(padding, padding, padding, padding);
            iconGenerator.setContentView(imageView);
        }

        @Override
        protected void onBeforeClusterItemRendered(Photo item, MarkerOptions options) {
            options.icon(getItemIcon(item));
        }

        @Override
        protected void onClusterItemUpdated(Photo item, Marker marker) {
            marker.setIcon(getItemIcon(item));
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<Photo> cluster, MarkerOptions options) {
            System.out.println("on before cluster");

            options.icon(getClusterIcon(cluster));
        }

        @Override
        protected void onClusterUpdated(Cluster<Photo> cluster, Marker marker) {
            marker.setIcon(getClusterIcon(cluster));
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster<Photo> cluster) {
            return cluster.getSize() > 4;
        }

        @Override
        public void onCameraIdle() {

        }

        private BitmapDescriptor getClusterIcon(Cluster<Photo> cluster) {
            List<Drawable> photos = new ArrayList<>(Math.min(4, cluster.getSize()));
            int width = dimension;
            int height = dimension;

            for (Photo p : cluster.getItems()) {
                if (photos.size() == 4) break;
                String src = getPreviewUrl(p);

                Drawable drawable;
                Bitmap bitmap = getBitmap(src);
                if (bitmap != null) {
                    drawable = new BitmapDrawable(getResources(), bitmap);
                } else {
                    drawable = new ColorDrawable(Color.WHITE);
                }

                drawable.setBounds(0, 0, width, height);
                photos.add(drawable);
            }
            MultiDrawable multiDrawable = new MultiDrawable(photos);
            multiDrawable.setBounds(0, 0, width, height);

            clusterImageView.setImageDrawable(multiDrawable);
            Bitmap icon = clusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
            return BitmapDescriptorFactory.fromBitmap(icon);
        }

        private BitmapDescriptor getItemIcon(Photo item) {
            Bitmap bitmap = getBitmap(getPreviewUrl(item));
            imageView.setImageBitmap(bitmap);

            Bitmap icon = iconGenerator.makeIcon();
            return BitmapDescriptorFactory.fromBitmap(icon);
        }
    }

    public static Bitmap getBitmap(String url) {
        return AppContext.imageCache.get(url);
    }
}

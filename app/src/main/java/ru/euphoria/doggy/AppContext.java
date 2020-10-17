package ru.euphoria.doggy;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.StrictMode;
import android.preference.PreferenceManager;

import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteStatement;
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory;

import com.google.android.exoplayer2.database.ExoDatabaseProvider;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.yandex.metrica.YandexMetrica;
import com.yandex.metrica.YandexMetricaConfig;

import org.sqlite.database.sqlite.RequerySQLiteOpenHelperFactory;

import java.text.MessageFormat;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import ru.euphoria.doggy.api.VKApi;
import ru.euphoria.doggy.api.model.App;
import ru.euphoria.doggy.api.model.User;
import ru.euphoria.doggy.api.model.Video;
import ru.euphoria.doggy.common.Tokens;
import ru.euphoria.doggy.data.SettingsStore;
import ru.euphoria.doggy.db.AppDatabase;
import ru.euphoria.doggy.itunes.AudiosAlbumConverterAdapter;
import ru.euphoria.doggy.itunes.ItunesApi;
import ru.euphoria.doggy.retrofit.AppsService;
import ru.euphoria.doggy.retrofit.DocsService;
import ru.euphoria.doggy.retrofit.ExecutesService;
import ru.euphoria.doggy.retrofit.MessagesService;
import ru.euphoria.doggy.retrofit.ModelConverterAdapter;
import ru.euphoria.doggy.retrofit.RawJsonConverterAdapter;
import ru.euphoria.doggy.retrofit.RetryCallAdapterFactory;
import ru.euphoria.doggy.retrofit.TokenInterceptor;
import ru.euphoria.doggy.retrofit.UsersService;
import ru.euphoria.doggy.retrofit.VideosService;
import ru.euphoria.doggy.util.AndroidUtil;

/**
 * Created by admin on 23.03.18.
 */

public class AppContext extends Application {
    private static final String METRICA_API_KEY = Tokens.METRICA_API_KEY;

    public static volatile ExoDatabaseProvider databaseProvider;
    public static volatile SharedPreferences preference;
    public static volatile OkHttpClient httpClient;
    public static volatile AppDatabase database;
    public static volatile Location location;
    public static volatile Context context;
    public static volatile Retrofit retrofit;
    public static volatile Executor executor;
    public static volatile LruCache imageCache;
    public static volatile ItunesApi itunes;
    public static volatile boolean ads = true;

    public static volatile UsersService users;
    public static volatile VideosService videos;
    public static volatile AppsService apps;
    public static volatile DocsService docs;
    public static volatile MessagesService messages;
    public static volatile ExecutesService executes;

    @Override
    public void onCreate() {
        super.onCreate();
        executor = Executors.newFixedThreadPool(3);

        context = this;
        preference = PreferenceManager.getDefaultSharedPreferences(this);
        databaseProvider = new ExoDatabaseProvider(context);
        ads = AndroidUtil.shouldShowAds();
        VKApi.initBaseUrls();

        initHttpClient();
        initDatabase();
        initAnalytics();
        initExceptionHandler();
        initPicasso();
        initRetrofit();

        if (getPackageName().contains(".dev")) {
            ads = false;
            YandexMetrica.setStatisticsSending(getApplicationContext(), false);
        }
    }

    private void initHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.level(HttpLoggingInterceptor.Level.BASIC);

            builder.addInterceptor(logging);
        }
        httpClient = builder
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(0, TimeUnit.SECONDS)
                .build();
    }

    private void initRetrofit() {
        OkHttpClient apiClient = httpClient.newBuilder()
                .addInterceptor(new TokenInterceptor())
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(MessageFormat.format("https://{0}/method/", VKApi.apiDomain))
                .client(apiClient)
                .addCallAdapterFactory(RetryCallAdapterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .addConverterFactory(RawJsonConverterAdapter.create())
                .addConverterFactory(RawJsonConverterAdapter.createForInteger())
                .addConverterFactory(AudiosAlbumConverterAdapter.create())
                .addConverterFactory(ModelConverterAdapter.create(User.class))
                .addConverterFactory(ModelConverterAdapter.create(Video.class))
                .addConverterFactory(ModelConverterAdapter.create(App.class))
                .build();

        itunes = retrofit.create(ItunesApi.class);
        users = retrofit.create(UsersService.class);
        videos = retrofit.create(VideosService.class);
        apps = retrofit.create(AppsService.class);
        docs = retrofit.create(DocsService.class);
        messages = retrofit.create(MessagesService.class);
        executes = retrofit.create(ExecutesService.class);
    }

    public static String getSQLiteVersion() {
        SupportSQLiteStatement statement = database.compileStatement("select sqlite_version()");
        return statement.simpleQueryForString();
    }

    private void initDatabase() {
        boolean inMemory = SettingsStore.getBoolean(SettingsFragment.KEY_IN_MEMORY_DATABASE);
        String path = context.getDatabasePath("database.db").toString();

        RoomDatabase.Builder<AppDatabase> builder = inMemory
                ? Room.inMemoryDatabaseBuilder(this, AppDatabase.class)
                : Room.databaseBuilder(context, AppDatabase.class,
                path);

        database = builder
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .openHelperFactory(SettingsStore.useOptimizedSqlite()
                        ? new RequerySQLiteOpenHelperFactory()
                        : new FrameworkSQLiteOpenHelperFactory())
                .build();

        System.out.println("SQLite Version: " + getSQLiteVersion());
    }

    private void initAnalytics() {
        YandexMetricaConfig.Builder configBuilder = YandexMetricaConfig.newConfigBuilder(METRICA_API_KEY);
        configBuilder.withSessionTimeout(60);

        YandexMetrica.activate(getApplicationContext(), configBuilder.build());
        YandexMetrica.enableActivityAutoTracking(this);
    }

    public void initStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.enableDefaults();
        }
    }

    public void initPicasso() {
        imageCache = new LruCache(context);
        Picasso.setSingletonInstance(new Picasso.Builder(context)
                .downloader(new OkHttp3Downloader(context, 100 * 1014 * 1024))
                .memoryCache(imageCache).build());
    }

    @SuppressLint("ApplySharedPref")
    public void initExceptionHandler() {
        Thread.UncaughtExceptionHandler exceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            if (e instanceof UndeliverableException) {
                return;
            }
            if (e instanceof InterruptedException) {
                // fine, some blocking code was interrupted by a dispose call
                return;
            }

            preference.edit()
                    .putBoolean("crashed", true)
                    .putString("crashed_msg", e.toString())
                    .commit();

            if (exceptionHandler != null) {
                exceptionHandler.uncaughtException(t, e);
            }
        });
    }
}

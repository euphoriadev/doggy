package ru.euphoria.doggy.util;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;

import com.android.billingclient.api.Purchase;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;
import com.yandex.metrica.YandexMetrica;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import ru.euphoria.doggy.AppContext;
import ru.euphoria.doggy.R;
import ru.euphoria.doggy.SettingsFragment;
import ru.euphoria.doggy.StartActivity;
import ru.euphoria.doggy.ads.AdsManager;
import ru.euphoria.doggy.api.ErrorCodes;
import ru.euphoria.doggy.api.VKApi;
import ru.euphoria.doggy.api.VKException;
import ru.euphoria.doggy.api.model.Audio;
import ru.euphoria.doggy.api.model.AudioMessage;
import ru.euphoria.doggy.api.model.Chat;
import ru.euphoria.doggy.api.model.Document;
import ru.euphoria.doggy.api.model.Photo;
import ru.euphoria.doggy.api.model.PhotoSizes;
import ru.euphoria.doggy.api.model.User;
import ru.euphoria.doggy.browser.ChromeTabs;
import ru.euphoria.doggy.common.DebugLog;
import ru.euphoria.doggy.common.FileDownloader;
import ru.euphoria.doggy.common.SignatureChecker;
import ru.euphoria.doggy.data.SettingsStore;
import ru.euphoria.doggy.db.AppDatabase;
import ru.euphoria.doggy.image.CircleTransformation;
import ru.euphoria.doggy.io.Streams;

import static android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

/**
 * Created by admin on 29.03.18.
 */

public class AndroidUtil {
    private static final File DIRECTORY_DOWNLOADS = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    private static final String DIRECTORY_DOWNLOADS_PATH = DIRECTORY_DOWNLOADS.getAbsolutePath();

    private static final char[] HEX = "0123456789ABCDEF".toCharArray();
    private static final int BUFFER_SIZE = 8192;
    public static final String MOBILE_USER_AGENT = "Mozilla/5.0 (Linux; U; Android 4.4.2; " +
            "en-us; SCH-I535 Build/KOT49H) AppleWebKit/534.30 (KHTML, " +
            "like Gecko) Version/4.0 Mobile Safari/534.30";


    public static boolean isGooglePlaySignature(Activity activity) {
        try {
            return SignatureChecker.checkGooglePlaySignature(activity);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Bitmap getDiskBitmap(String url) {
        Bitmap image = AppContext.imageCache.get(url);
        if (image == null) {
            url += "\nsquare\n";
            image = AppContext.imageCache.get(url);
        }
        return image;
    }

    public static File getImageCacheFolder(Activity activity) {
        return new File(activity.getCacheDir(), "picasso-cache");
    }

    public static File getAudioCacheFolder(Activity activity) {
        return new File(activity.getCacheDir(), "audio-cache");
    }

    public static void clearFolder(File folder) {
        if (folder.isDirectory()) {
            for (File file : folder.listFiles()) {
                if (!file.isDirectory()) {
                    file.delete();
                } else {
                    clearFolder(file);
                }
            }
        }
    }

    public static void clearImageCache(Activity activity) {
        clearFolder(getImageCacheFolder(activity));
    }

    public static void clearAudioCache(Activity activity) {
        clearFolder(getAudioCacheFolder(activity));
    }

    public static void clearCache() {
        AppDatabase.database().clearAllTables();
    }

    public static void copy(File from, File to) throws IOException {

    }

    public static long folderSize(File directory) {
        long length = 0;
        if (!directory.exists()) {
            return length;
        }
        for (File file : directory.listFiles()) {
            length += file.isDirectory() ? folderSize(file) : file.length();
        }
        return length;
    }

    public static Single<Long> folderSizeAsync(File directory) {
        return Single.fromCallable(() -> folderSize(directory))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX[v >>> 4];
            hexChars[j * 2 + 1] = HEX[v & 0x0F];
        }
        return String.valueOf(hexChars);
    }

    // with empty check
    public static String join(CharSequence delimiter, String... tokens) {
        ArrayList<String> noEmptyList = new ArrayList<>(Arrays.asList(tokens));
        ArrayUtil.filter(noEmptyList, s -> !TextUtils.isEmpty(s));

        return TextUtils.join(delimiter, noEmptyList);
    }

    public static boolean nonNull(Object obj) {
        return obj != null;
    }

    public static void startService(Context context, Intent intent) {
        ContextCompat.startForegroundService(context, intent);
    }

    public static byte[] marshall(Parcelable parceable) {
        Parcel parcel = Parcel.obtain();
        parceable.writeToParcel(parcel, 0);
        byte[] bytes = parcel.marshall();
        parcel.recycle();
        return bytes;
    }

    public static Parcel unmarshall(byte[] bytes) {
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(bytes, 0, bytes.length);
        parcel.setDataPosition(0); // This is extremely important!
        return parcel;
    }

    public static <T> T unmarshall(byte[] bytes, Parcelable.Creator<T> creator) {
        Parcel parcel = unmarshall(bytes);
        T result = creator.createFromParcel(parcel);
        parcel.recycle();
        return result;
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int identifier = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (identifier > 0) {
            result = context.getResources().getDimensionPixelSize(identifier);
        }
        return result;
    }

    public static String requestSync(Request request) throws IOException {
        Response response = AppContext.httpClient.newCall(request).execute();
        return response.body().string();
    }

    public static String requestSync(String url) throws IOException {
        Request builder = new Request.Builder()
                .url(url)
                .build();
        return requestSync(builder);
    }

    public static Single<String> request(Request request) {
        return Single.fromCallable(() -> requestSync(request)).subscribeOn(Schedulers.io());
    }

    public static Single<String> request(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        return request(request);
    }

    public static String sub(String text) {
        Matcher matcher = Pattern.compile("\\w+").matcher(text);
        StringBuilder buffer = new StringBuilder();
        while (matcher.find()) {
            buffer.append(matcher.group());
        }

        return buffer.toString();
    }

    public static String getKateVersionApi() {
        return "5.72";
    }

    public static String getKateUserAgent() {
        return String.format(Locale.getDefault(),
                "KateMobileAndroid/%s-%d (Android %s; SDK %d; %s; %s %s; %s)",
                "51.1", 442, Build.VERSION.RELEASE, Build.VERSION.SDK_INT, Build.CPU_ABI, Build.MANUFACTURER,
                Build.MODEL, Locale.getDefault().getLanguage());
    }

    public DocumentFile getDocumentFile(Context context, File file) {
        DocumentFile document = null;
        String baseFolder = null;
        File filePath = getExternalStorageDir(context);
        if (filePath.getAbsolutePath().startsWith(file.getAbsolutePath())) {
            baseFolder = filePath.getAbsolutePath();
        }
        if (baseFolder == null) {
            return null;
        }
        try {
            String relativePath = file.getCanonicalPath().substring(baseFolder.length() + 1);
            Uri permissionUri = Uri.parse(SettingsStore.getString("music_folder"));
            document = getDocumentFileForUri(context, permissionUri, relativePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return document;
    }

    private DocumentFile getDocumentFileForUri(Context context, Uri treeUri, String relativePath) {
        String[] parts = relativePath.split("/");
        if (parts.length == 0) {
            return null;
        }
        DocumentFile document = DocumentFile.fromTreeUri(context, treeUri);
        for (String part : parts) {
            DocumentFile nextDocument = document.findFile(part);
            if (nextDocument != null) {
                document = nextDocument;
            }
        }
        return document;
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private static String getPath(StorageVolume volume) {
        try {
            Method method = StorageVolume.class.getMethod("getPath");
            method.setAccessible(true);
            return (String) method.invoke(volume);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static File getExternalStorageDir(Context context) {
        StorageManager storage = (StorageManager)
                context.getSystemService(Context.STORAGE_SERVICE);
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            List<StorageVolume> volumes = storage.getStorageVolumes();
            for (int i = 0; i < volumes.size(); i++) {
                StorageVolume volume = volumes.get(i);
                if (volume.isRemovable()) {
                    String path = getPath(volume);
                    if (path != null) {
                        return new File(path);
                    }
                }
            }
        } else {
            return Environment.getExternalStorageDirectory();
        }
        return null;
    }

    public static String getExternalStorageFreeSize() {
        File directory = getExternalStorageDir(AppContext.context);
        return getFreeSize(directory);
    }

    public static String getInternalStorageFreeSize() {
        File directory = Environment.getDataDirectory();
        return getFreeSize(directory);
    }

    public static String getFreeSize(File dir) {
        StatFs fs = new StatFs(dir.getPath());
        return formatSize(fs.getFreeBlocksLong() * fs.getBlockSizeLong());
    }

    public static String formatSize(long size) {
        return Formatter.formatFileSize(AppContext.context, size);
    }

    public static String formatDistance(Context context, int meters) {
        if (meters > 1000) {
            return context.getString(R.string.kilometers_short, meters / 1000);
        }
        return context.getString(R.string.meters_short, meters);
    }

    public static String formatSeconds(Context context, int secs) {
        if (secs > 3600) {
            int hours = secs / 3600;
            return context.getResources().getQuantityString(R.plurals.hours, hours, hours);
        }
        if (secs > 60) {
            int mins = secs / 60;
            return context.getResources().getQuantityString(R.plurals.minutes, mins, mins);
        }
        return context.getResources().getQuantityString(R.plurals.seconds, secs, secs);
    }

    public static void pickAudio(Activity activity, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/*");
        activity.startActivityForResult(intent, requestCode);
    }

    public static void download(File file, String url) throws Exception {
        byte[] data = AppContext.httpClient.newCall(new Request.Builder().url(url).build())
                .execute().body().bytes();

        FileOutputStream fos = new FileOutputStream(file);
        fos.write(data);
        fos.close();
    }

    public static long download(Context context, Document doc) {
        return download(context, doc.url, doc.title, null);
    }

    public static long download(Context context, PhotoSizes.PhotoSize photo) {
        return download(context, photo.src, String.valueOf(photo.src.hashCode()), "jpg");
    }

    public static long download(Context context, AudioMessage msg) {
        return download(context, msg.link_ogg, "Voice Message-" + msg.id, "ogg");
    }

    public static long download(Context context, Audio audio) {
        return download(context, audio.url, audio.toString(), "mp3", SettingsStore.getMusicFolder());
    }

    public static long download(Context context, String url, String title, String ext) {
        return download(context, url, title, ext, DIRECTORY_DOWNLOADS_PATH);
    }

    public static long download(Context context, String url, String title, String ext, String dir) {
        try {
            String filename = title + "." + ext;
            File file = new File(dir, filename);

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url.trim()));
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI
                    | DownloadManager.Request.NETWORK_MOBILE);
            request.setTitle(filename);
            request.setNotificationVisibility(VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDescription(context.getString(R.string.downloading));
            request.setDestinationUri(Uri.fromFile(file));
            request.allowScanningByMediaScanner();
            request.setMimeType(FileDownloader.getMimeType(file.getAbsolutePath()));

            DownloadManager downloader = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            return downloader.enqueue(request);
        } catch (Throwable e) {
            e.printStackTrace();
            AndroidUtil.toast(context, e.getMessage());
            YandexMetrica.reportError("Ошибка загрузки файла", e);
        }
        return -1;
    }

    public static void toast(Context context, String message) {
        ((Activity) context).runOnUiThread(() -> Toast.makeText(context, message, Toast.LENGTH_LONG).show());
    }

    public static Snackbar snackbar(Context context, String message) {
        return snackbar(context, message, Snackbar.LENGTH_LONG);
    }

    public static Snackbar snackbar(Context context, String message, int duration) {
        View root = ((Activity) context).findViewById(R.id.container);
        if (root == null) {
            root = ((Activity) context).findViewById(android.R.id.content);
        }

        Snackbar snackbar = Snackbar.make(root, message, duration);
        snackbar.show();
        return snackbar;
    }

    public static void toast(Context context, int res) {
        toast(context, context.getString(res));
    }

    public static void purchaseDisableAds(Activity context) {
        GooglePlayUtil.purchase(context, purchase -> {
            refreshPurchaseToken(context, purchase);

            toast(context, R.string.ads_disabled);
            YandexMetrica.reportEvent("Отключение рекламы");
        });
    }

    public static void refreshPurchaseToken(Context context, Purchase purchase) {
        SettingsStore.putValue("purchase_token", purchase.getPurchaseToken());
        AppContext.ads = false;
    }

    public static void copyText(Context context, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
        if (clipboard != null) {
            try {
                clipboard.setPrimaryClip(clip);

                String msg = String.format(Locale.ROOT, "%s:\n%s",
                        context.getString(R.string.text_copied), text);
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            } catch (Throwable e) {
                e.printStackTrace();
                Toast.makeText(context, "Текст слишком большой", Toast.LENGTH_SHORT).show();
            }
        }

    }

    public static boolean checkStoragePermissions(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                ActivityCompat.requestPermissions(activity, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
                return false;
            }
        }
        return true;
    }

    public static boolean checkLocationPermissions(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(activity, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
                return false;
            }
        }
        return true;
    }

    public static Consumer<? super Throwable> handleError(Context context) {
        return error -> handleError(context, error);
    }

    public static void handleError(Context context, Throwable error) {
        YandexMetrica.reportError(error.getMessage(), error);
        error.printStackTrace();
        toast(context, error.getMessage());

        if (error instanceof VKException) {
            int code = ((VKException) error).code;
            switch (code) {
                case ErrorCodes.ACCESS_DENIED:
                case ErrorCodes.USER_AUTHORIZATION_FAILED:
                    createAuthNotification(context, code);
                    break;
            }
        }
    }

    public static void createAuthNotification(Context context, int code) {
        NotificationManager nm = getSystemService(context, Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(context, StartActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, String.valueOf(code));
        builder.setContentTitle("Требуется авторизация");
        builder.setContentText("Необходимо перезайти в аккаунт");
        builder.setSmallIcon(R.drawable.ic_vector_login);
        builder.setCategory(NotificationCompat.CATEGORY_ERROR);
        builder.setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(String.valueOf(code), "API Error",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Требуется авторизация");
            channel.enableLights(false);
            channel.enableVibration(false);
            nm.createNotificationChannel(channel);
        }

        Notification notification = builder.build();
        nm.notify(code, notification);
    }

    @SuppressWarnings("unchecked")
    public static <E> E getSystemService(Context context, String service) {
        return (E) context.getSystemService(service);
    }

    public static void toastErrorConnection(Context context) {
        Toast.makeText(context, R.string.error_connection, Toast.LENGTH_LONG).show();
    }

    public static boolean shouldShowAds() {
        if (SettingsStore.getString("purchase_token").length() > 0) {
            return false;
        }
        long lastSeen = SettingsStore.getLong("last_seen_ads");
        return !(lastSeen > 0 && System.currentTimeMillis() - lastSeen < (1000 * 60 * 60 * 24));
    }

    public static void loadRewardedAds(Activity activity) {
        // Use an activity context to get the rewarded video instance.
        RewardedVideoAd instance = MobileAds.getRewardedVideoAdInstance(activity);
        instance.setRewardedVideoAdListener(new RewardedVideoAdListener() {
            @Override
            public void onRewarded(RewardItem reward) {
                // Reward the user.
                SettingsStore.putValue("last_seen_ads", System.currentTimeMillis());
                AppContext.ads = false;
                Toast.makeText(activity, R.string.ads_disabled, Toast.LENGTH_LONG).show();

                YandexMetrica.reportEvent("Просмотр рекламного видео");
            }

            @Override
            public void onRewardedVideoAdLeftApplication() {
                System.out.println("on Rewarded Video Ad Left Application");
            }

            @Override
            public void onRewardedVideoAdClosed() {
                System.out.println("on Rewarded Video Ad Closed");
            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int errorCode) {
                System.out.println("on Rewarded Video Ad Failed To Load");
                Toast.makeText(activity, R.string.reward_failed_to_load, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRewardedVideoAdLoaded() {
                System.out.println("on Rewarded Video Ad Loaded");
                instance.show();
            }

            @Override
            public void onRewardedVideoAdOpened() {
                System.out.println("on Rewarded Video Ad Opened");
            }

            @Override
            public void onRewardedVideoStarted() {
                System.out.println("on Rewarded Video Started");
            }

            @Override
            public void onRewardedVideoCompleted() {
                System.out.println("on Rewarded Video Completed");
            }
        });

        AdRequest.Builder builder = new AdRequest.Builder();
        instance.loadAd(activity.getString(R.string.ad_main_reward),
                builder.build());
    }

    public static void loadInterstitialAds(Context context) {
        AdsManager.showInterstitial(context, AdsManager.interstitialId());
    }

    public static void saveLocation(Location location) {
        JSONObject json = new JSONObject();
        try {
            json.put("lat", location.getLatitude());
            json.put("long", location.getLongitude());
            SettingsStore.putValue("location", json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public static void openFolder(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(file.getAbsolutePath()), "resource/folder");
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.choice_app)));
    }

    public static void browseUserOrGroup(Context context, int peer) {
        browse(context, peer < 0
                ? "https://vk.com/club" + Math.abs(peer) : "https://vk.com/id" + peer);
    }

    public static void browse(Context context, int peer) {
        if (peer > VKApi.PEER_OFFSET) {
            browse(context, "https://vk.com/id" + (peer - VKApi.PEER_OFFSET));
            return;
        }
        if (peer < 0) {
            browse(context, "https://vk.com/club" + Math.abs(peer));
            return;
        }
        browse(context, "https://vk.com/im?sel=c" + peer);
    }

    public static void browse(Context context, User user) {
        browse(context, link(user));
    }

    public static void browse(Context context, Chat chat) {
        browse(context, link(chat));
    }

    public static void browse(Context context, String url, String ext) {
        Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        if (!TextUtils.isEmpty(ext)) {
            MimeTypeMap map = MimeTypeMap.getSingleton();
            String type = map.getMimeTypeFromExtension(ext);

            if (type == null) {
                type = "*/*";
            }
            DebugLog.w("AndroidUtil", "browse ext: " + ext + ", type: " + type);
            browser.setType(type);
        }

        List<ResolveInfo> activities = context.getPackageManager()
                .queryIntentActivities(browser, 0);
        if (activities != null && !activities.isEmpty()) {
            for (ResolveInfo info : activities) {
                if (info.activityInfo.packageName.toLowerCase().contains("chrome")) {
                    ChromeTabs.open(context, url);
                    return;
                }
            }
        }

        context.startActivity(Intent.createChooser(browser, context.getString(R.string.choice_app)));
    }

    public static void browse(Context context, String url) {
        browse(context, url, "");
    }

    public static void browsePhone(Context context, String phone) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
        context.startActivity(intent);
    }

    public static void browse(Context context, LatLng location) {
        browse(context, "geo:" + location.latitude + "," + location.longitude);
    }

    public static void openSkype(Context context, String number) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("skype:" + number));
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            toast(context, R.string.skype_not_found);
        }
    }


    public static void browseInstagram(Context context, String user) {
        browse(context, linkInstagram(user));
    }

    public static void browseTwitter(Context context, String user) {
        browse(context, linkTwitter(user));
    }

    public static void browseFacebook(Context context, String user) {
        browse(context, linkFacebook(user));
    }

    public static String link(User user) {
        return "https://vk.com/id" + user.id;
    }

    public static String link(Photo photo) {
        return "https://vk.com/" + photo.toAttachmentString();
    }

    public static String link(Chat chat) {
        return "https://vk.com/im?sel=c" + chat.id;
    }

    public static String linkInstagram(String user) {
        return "https://www.instagram.com/" + user;
    }

    public static String linkTwitter(String user) {
        return "https://www.twitter.com/" + user;
    }

    public static String linkFacebook(String user) {
        return "https://www.facebook.com/" + user;
    }

    public static void search(Context context, String q) {
        String url = HttpUrl.get("https://www.google.com/search").newBuilder()
                .addQueryParameter("q", q)
                .toString();
        AndroidUtil.browse(context, url);
    }

    public static WindowManager getWindowManager(Context context) {
        if (context instanceof Activity) {
            return ((Activity) context).getWindowManager();
        }
        return (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    public static int getDisplayWidth(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager(context).getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }

    public static boolean hasConnection() {
        if (AppContext.context == null) {
            return false;
        }
        ConnectivityManager cm = (ConnectivityManager) AppContext.context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }
        NetworkInfo info = cm.getActiveNetworkInfo();
        return (info != null &&
                info.isAvailable() &&
                info.isConnected());
    }


    public static void changeTheme(Context context) {
        boolean night = SettingsStore.getBoolean(SettingsFragment.KEY_NIGHT_MODE);
        context.setTheme(night ? R.style.AppTheme_Dark : R.style.AppTheme);
    }

    public static int getAttrColor(Context c, int id) {
        int[] attrs = {id};
        TypedArray array = c.obtainStyledAttributes(attrs);
        try {
            return array.getColor(0, Color.BLACK);
        } finally {
            array.recycle();
        }
    }

    public static float px(Context context, int dp) {
        Resources res = context.getResources();
        DisplayMetrics metrics = res.getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics);
    }

    public static float sp(Context context, int px) {
        Resources res = context.getResources();
        DisplayMetrics metrics = res.getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, px, metrics);
    }

    public static void setDialogContentPadding(View layout) {
        layout.setPadding(Math.round(AndroidUtil.px(layout.getContext(), 19)),
                Math.round(AndroidUtil.px(layout.getContext(), 5)),
                Math.round(AndroidUtil.px(layout.getContext(), 19)),
                Math.round(AndroidUtil.px(layout.getContext(), 5)));
    }

    public static void loadImage(ImageView view, String url) {
        if (TextUtils.isEmpty(url)) {
            view.setImageResource(R.drawable.empty_avatar);
            return;
        }

        Picasso.get()
                .load(url)
                .config(Bitmap.Config.ARGB_8888)
                .transform(new CircleTransformation())
                .placeholder(R.drawable.empty_avatar)
                .into(view);
    }


    public static String loadAssestsFile(String filename) {
        try {
            return loadAssestsFile(AppContext.context, filename);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    @Deprecated
    public static String loadAssestsFile(Context context, String filename) throws IOException {
        AssetManager am = context.getAssets();
        InputStream input;

        input = am.open(filename);
        return readStream(input);
    }

    public static String readStream(InputStream input) throws IOException {
        return Streams.read(input);
    }


    public static String readStream(InputStream input, String encoding) throws IOException {
        return Streams.read(input, Charset.forName(encoding));
    }

    public static boolean nonEmpty(CharSequence str) {
        return !TextUtils.isEmpty(str);
    }
}

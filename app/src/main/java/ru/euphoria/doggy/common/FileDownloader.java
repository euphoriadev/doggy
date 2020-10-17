package ru.euphoria.doggy.common;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import java.io.File;

import ru.euphoria.doggy.R;
import ru.euphoria.doggy.data.SettingsStore;

import static android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED;

public class FileDownloader {
    public static final String TYPE_SYSTEM = "system";
    public static final String TYPE_ALTERNATIVE = "alternative";

    public static String getType() {
        return SettingsStore.getString("downloader");
    }

    public static void setType(String type) {
        SettingsStore.putValue("downloader", type);
    }

    public static File makeFile(String name, String ext) {
        return makeFile(Environment.DIRECTORY_DOWNLOADS, name, ext);
    }

    public static File makeFile(String dirType, String name, String ext) {
        return new File(Environment.getExternalStoragePublicDirectory(dirType),
                name + (TextUtils.isEmpty(ext) ? "" : ("." + ext)));
    }

    public static long systemDownload(Context context, String url, File file) {
        String filename = file.getName();

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url.trim()));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI
                | DownloadManager.Request.NETWORK_MOBILE);
        request.setTitle(filename);
        request.setNotificationVisibility(VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDescription(context.getString(R.string.downloading));
        request.setDestinationUri(Uri.fromFile(file));
        request.allowScanningByMediaScanner();
        request.setMimeType(getMimeType(file.getAbsolutePath()));

        DownloadManager downloader = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        return downloader.enqueue(request);
    }

    // url = file path or whatever suitable URL you want.
    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }
}

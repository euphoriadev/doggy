package ru.euphoria.doggy.util;

import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;

import okhttp3.Request;
import ru.euphoria.doggy.AppContext;
import ru.euphoria.doggy.api.model.Photo;
import ru.euphoria.doggy.api.model.PhotoSizes.PhotoSize;

/**
 * Created by admin on 01.06.18.
 */

public class AttachSaver {
    public static final File ROOT_DIR = new File(Environment.getExternalStorageDirectory() + "/Doggy");
    public static final File PHOTOS_DIR = new File(ROOT_DIR, "Photos");

    public static final int QUALITY_2560 = 0;
    public static final int QUALITY_1280 = 1;
    public static final int QUALITY_807 = 2;
    public static final int QUALITY_604 = 3;
    public static final int QUALITY_130 = 4;
    public static final int QUALITY_75 = 5;

    public static void save(String subFolder, Photo photo, int quality) throws Exception {
        File dir = new File(PHOTOS_DIR + "/" + subFolder);
        dir.mkdirs();

        String link = getPhotoLink(photo, quality);
        File file = getFile(dir, String.valueOf(link.hashCode()));
        rawSave(file, link);
    }

    private static synchronized File getFile(File dir, String name) {
        File file = new File(dir, name + "." + "jpg");
        if (file.exists()) {
            for (int i = 0; i < 10; i++) {
                file = new File(dir, name + " (" + i + ")." + "jpg");
                if (!file.exists()) {
                    break;
                }
            }
        }
        return file;
    }

    private static void rawSave(File file, String url) throws Exception {
        byte[] data = AppContext.httpClient.newCall(new Request.Builder().url(url).build())
                .execute()
                .body()
                .bytes();

        FileOutputStream fos = new FileOutputStream(file);
        fos.write(data);
        fos.flush();
        fos.close();
    }

    @SuppressWarnings("ConstantConditions")
    private static String getPhotoLink(Photo photo, int quality) {
        String link = null;
        PhotoSize size = null;
        switch (quality) {
            case QUALITY_2560:
                size = photo.sizes.of(PhotoSize.W);
                break;
            case QUALITY_1280:
                size = photo.sizes.of(PhotoSize.Z);
                break;
            case QUALITY_807:
                size = photo.sizes.of(PhotoSize.YY);
                break;
            case QUALITY_604:
                size = photo.sizes.of(PhotoSize.X);
                break;
            case QUALITY_130:
                size = photo.sizes.of(PhotoSize.M);
                break;
            case QUALITY_75:
                size = photo.sizes.of(PhotoSize.S);
                break;
        }
        if (size != null) {
            link = size.src;
        }
        if (TextUtils.isEmpty(link)) {
            return ArrayUtil.firstNotEmpty(
                    photo.sizes.of(PhotoSize.W),
                    photo.sizes.of(PhotoSize.Z),
                    photo.sizes.of(PhotoSize.YY),
                    photo.sizes.of(PhotoSize.X),
                    photo.sizes.of(PhotoSize.M),
                    photo.sizes.of(PhotoSize.S)
            ).src;
        }

        return link;
    }
}

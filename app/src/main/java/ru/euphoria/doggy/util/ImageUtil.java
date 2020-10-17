package ru.euphoria.doggy.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;

public class ImageUtil {
    public static byte[] bitmapToBytes(Bitmap source) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        source.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        return bos.toByteArray();
    }

    public static Bitmap bitmapFromBytes(byte[] array) {
        return BitmapFactory.decodeByteArray(array, 0, array.length);
    }

    public static Bitmap getBitmap(ImageView view) {
        return ((BitmapDrawable) view.getDrawable()).getBitmap();
    }

    public static Bitmap scaleDown(Bitmap source, double factor) {
        return Bitmap.createScaledBitmap(source,
                (int) Math.round(source.getWidth() * factor),
                (int) Math.round(source.getHeight() * factor),
                true);
    }
}

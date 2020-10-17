package ru.euphoria.doggy.image;

import android.graphics.Bitmap;

import com.squareup.picasso.Transformation;

public class SquareTransformation implements Transformation {
    @Override
    public Bitmap transform(Bitmap source) {
        if (source == null) return null;

        int size = Math.min(source.getWidth(), source.getHeight());
        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;
        Bitmap result = Bitmap.createBitmap(source, x, y, size, size);
        if (result != source) {
            source.recycle();
        }
        return result;
    }

    @Override
    public String key() {
        return "square";
    }
}

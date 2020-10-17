package ru.euphoria.doggy.image;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.squareup.picasso.Transformation;

/**
 * Created by admin on 23.03.18.
 */

public class CircleTransformation implements Transformation {

    @Override
    public Bitmap transform(Bitmap source) {
        if (source == null) return null;

        Bitmap circled = Bitmap.createBitmap(source.getWidth(),
                source.getHeight(), Bitmap.Config.ARGB_8888);
        float cx = source.getWidth() / 2f;
        float cy = source.getHeight() / 2f;

        Paint paint = new Paint();
        paint.setShader(new BitmapShader(source, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
        paint.setAntiAlias(true);

        Canvas canvas = new Canvas(circled);
        canvas.drawCircle(cx, cy, Math.min(cx, cy), paint);
        source.recycle();
        return circled;
    }

    @Override
    public String key() {
        return "circle";
    }
}

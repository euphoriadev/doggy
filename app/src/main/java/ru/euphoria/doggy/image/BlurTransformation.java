package ru.euphoria.doggy.image;

import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

import com.squareup.picasso.Transformation;

import static ru.euphoria.doggy.AppContext.context;

/**
 * Created by admin on 24.04.18.
 */

public class BlurTransformation implements Transformation {
    private static RenderScript rs = RenderScript.create(context);
    private int radius;

    public BlurTransformation(int radius) {
        this.radius = radius;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        if (source == null) return null;
        Bitmap copy = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        renderScriptBlur(source, copy, radius);

        source.recycle();
        return copy;
    }

    @Override
    public String key() {
        return "blur_" + radius;
    }

    public static void renderScriptBlur(Bitmap source, Bitmap copy, int radius) {
        Allocation input = Allocation.createFromBitmap(rs, source);
        Allocation output = Allocation.createTyped(rs, input.getType());
        ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        script.setRadius(radius);
        script.setInput(input);
        script.forEach(output);
        output.copyTo(copy);
    }

}

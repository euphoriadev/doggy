package ru.euphoria.doggy.dialog;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.squareup.picasso.Picasso;

import ru.euphoria.doggy.api.VKException;
import ru.euphoria.doggy.util.AndroidUtil;

public class CaptchaDialog extends MaterialAlertDialogBuilder {
    private EditText text;

    public CaptchaDialog(Context context, VKException ex) {
        super(context);

        LinearLayout root = makeRootLayout(context);
        ImageView image = makeImage(context);
        this.text = makeEditText(context);

        root.addView(image);
        root.addView(text);

        Picasso.get()
                .load(ex.captchaImg)
                .config(Bitmap.Config.ARGB_8888)
                .into(image);

        setTitle("Введите капчу");
        setView(root);
        setNegativeButton(android.R.string.cancel, null);
    }

    public String getText() {
        return text.getText().toString();
    }

    private ImageView makeImage(Context context) {
        ImageView image = new ImageView(context);
        image.setAdjustViewBounds(true);
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        image.setLayoutParams(makeLayoutParams());
        return image;
    }

    private EditText makeEditText(Context context) {
        EditText text = new EditText(context);
        text.setLayoutParams(makeLayoutParams());
        return text;
    }

    private LinearLayout.LayoutParams makeLayoutParams() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private LinearLayout makeRootLayout(Context context) {
        LinearLayout.LayoutParams params = makeLayoutParams();

        LinearLayout layout = new LinearLayout(context);
        layout.setLayoutParams(params);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(Math.round(AndroidUtil.px(context, 24)),
                Math.round(AndroidUtil.px(context, 5)),
                Math.round(AndroidUtil.px(context, 24)),
                Math.round(AndroidUtil.px(context, 5)));

        return layout;
    }
}

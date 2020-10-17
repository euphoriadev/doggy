package ru.euphoria.doggy.browser;

import android.content.Context;
import android.net.Uri;

import androidx.browser.customtabs.CustomTabsIntent;

import ru.euphoria.doggy.util.AndroidUtil;

public class ChromeTabs {

    public static void open(Context context, String url) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(AndroidUtil.getAttrColor(context, android.R.attr.colorPrimary));

        CustomTabsIntent intent = builder.build();
        intent.launchUrl(context, Uri.parse(url));
    }
}

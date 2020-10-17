package ru.euphoria.doggy.ads;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.formats.UnifiedNativeAd;

import ru.euphoria.doggy.AppContext;
import ru.euphoria.doggy.R;

public class AdsManager {
    public static final String TEST_ID = "ca-app-pub-3940256099942544/2247696110";

    public static String appUnitId() {
        return AppContext.context.getString(R.string.ad_app_id);
    }

    public static String messageBannerId() {
        return AppContext.context.getString(R.string.ad_message_banner_id);
    }

    public static String rewardId() {
        return AppContext.context.getString(R.string.ad_main_reward);
    }

    public static String interstitialId() {
        return AppContext.context.getString(R.string.ad_friends_interstitial);
    }

    public static String nativeId() {
        return AppContext.context.getString(R.string.ad_native_banner);
    }

    public static void showBanner(AdView view) {
        AdRequest request = createBuilder().build();
        view.setVisibility(View.VISIBLE);
        view.loadAd(request);
    }

    public static void showInterstitial(Context context, String unitId) {
        InterstitialAd ad = new InterstitialAd(context);
        ad.setAdUnitId(unitId);
        ad.loadAd(createBuilder().build());

        ad.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                ad.show();
            }
        });
    }

    public static AdRequest.Builder createBuilder() {
        AdRequest.Builder builder = new AdRequest.Builder();
        if (AppContext.location != null) {
            builder.setLocation(AppContext.location);
        }
        return builder;
    }

    public static boolean adHasOnlyStore(UnifiedNativeAd nativeAd) {
        String store = nativeAd.getStore();
        String advertiser = nativeAd.getAdvertiser();
        return !TextUtils.isEmpty(store) && TextUtils.isEmpty(advertiser);
    }

    public static void init() {
        MobileAds.initialize(AppContext.context, appUnitId());
    }
}

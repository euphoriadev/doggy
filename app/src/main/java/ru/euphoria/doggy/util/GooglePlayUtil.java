package ru.euphoria.doggy.util;

import android.app.Activity;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.Purchase;

import java.util.List;

import ru.euphoria.doggy.AppContext;

import static com.android.billingclient.api.BillingClient.BillingResponse;
import static com.android.billingclient.api.BillingClient.SkuType;

public class GooglePlayUtil {
    @Deprecated
    private static final String DISABLE_ADS = "disable_ads";
    private static final String REMOVE_ADS = "remove_ads";

    public static void purchase(Activity activity, OnPurchaseListener listener) {
        BillingClient client = BillingClient.newBuilder(AppContext.context)
                .setListener((code, purchases) -> {
                    if (code == BillingResponse.OK) {
                        foreach(purchases, listener);
                    }
                })
                .build();
        client.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(int code) {
                if (code == BillingResponse.OK) {
                    client.queryPurchaseHistoryAsync(SkuType.INAPP, (responseCode, purchases) -> {
                        if (responseCode == BillingResponse.OK) {
                            foreach(purchases, listener);
                        }
                    });

                    BillingFlowParams flowParams = createFlowParams();
                    client.launchBillingFlow(activity, flowParams);
                }
            }

            @Override
            public void onBillingServiceDisconnected() {

            }
        });
    }

    public static void checkLicense(OnLicenseListener listener) {
        BillingClient client = BillingClient.newBuilder(AppContext.context)
                .setListener((responseCode, purchases) -> { })
                .build();
        client.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(int code) {
                if (code == BillingResponse.OK) {
                    client.queryPurchaseHistoryAsync(SkuType.INAPP, (responseCode, purchases) -> {
                        if (responseCode == BillingResponse.OK) {
                            if (purchases == null) {
                                listener.onLicenseChecked(false, null);
                                return;
                            }

                            for (Purchase purchase : purchases) {
                                if (purchase.getSku().equals(REMOVE_ADS)
                                        || purchase.getSku().equals(DISABLE_ADS)) {
                                    listener.onLicenseChecked(true, purchase);
                                    return;
                                }
                            }
                            listener.onLicenseChecked(false, null);
                        }
                    });
                }
            }

            @Override
            public void onBillingServiceDisconnected() {

            }
        });
    }

    private static void foreach(List<Purchase> purchases, OnPurchaseListener listener) {
        if (purchases == null) {
            return;
        }
        for (Purchase purchase : purchases) {
            if (purchase.getSku().equals(DISABLE_ADS)
                    || purchase.getSku().equals(REMOVE_ADS)) {
                listener.onPurchaseSuccess(purchase);
            }
        }
    }

    private static BillingFlowParams createFlowParams() {
        return BillingFlowParams.newBuilder()
                .setSku(REMOVE_ADS)
                .setType(SkuType.INAPP)
                .build();
    }

    public interface OnLicenseListener {
        void onLicenseChecked(boolean licensed, Purchase purchase);
    }

    public interface OnPurchaseListener {
        void onPurchaseSuccess(Purchase purchase);
    }
}

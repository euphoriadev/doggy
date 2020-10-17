package ru.euphoria.doggy.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import ru.euphoria.doggy.util.AndroidUtil;

public class SignatureChecker {
    private static final String GOOGLE_PLAY_SIGNATURE_SHA1 = "F818B5D2F050B8C6E4350E2D2468DB38C2FFFF81";
    private static final String RELEASE_SIGNATURE_SHA1 = "15D158B48A22124ADC3453283F46C28C1076B779";

    @SuppressLint("WrongConstant")
    private static Signature[] getSignature(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);
            return info.signatures;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getSignatureString(Context context) throws NoSuchAlgorithmException {
        Signature[] signatures = getSignature(context);
        if (signatures == null) return null;

        Signature signature = signatures[0];
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(signature.toByteArray());

        return bytesToHex(md.digest());
    }

    public static boolean checkReleaseSignature(Context context) throws NoSuchAlgorithmException {
        return RELEASE_SIGNATURE_SHA1.equals(getSignatureString(context));
    }

    public static boolean checkGooglePlaySignature(Context context) throws NoSuchAlgorithmException {
        return GOOGLE_PLAY_SIGNATURE_SHA1.equals(getSignatureString(context));
    }

    private static String bytesToHex(byte[] bytes) {
        return AndroidUtil.bytesToHex(bytes);
    }
}

package com.acj.client.prosegur.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Base64;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

public class Util {

    private final static String TAG = Util.class.getSimpleName();

    public static String bytesToString(byte[] bytesATransformar) {
        String base64 = Base64.encodeToString(bytesATransformar, Base64.NO_WRAP);
        Log.d(TAG, "Byte to string: " + base64);
        base64 = base64.replace(",", StringUtils.EMPTY);
        return base64;

    }

    public static byte[] stringToBytes(String stringATransformar) {
        return Base64.decode(stringATransformar, Base64.NO_WRAP);
    }

    public static String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.NO_WRAP);
    }

    public static String obfuscateKeep(String value, Integer keepQuantity, Boolean reverse) {
        if(StringUtils.isEmpty(value) || (keepQuantity > value.length())) return StringUtils.EMPTY;
        int obfuscateQuantity = value.length() - keepQuantity;
        return (reverse)
            ? value.substring(0, keepQuantity).concat(StringUtils.repeat("*", obfuscateQuantity))
            : StringUtils.repeat("*", obfuscateQuantity).concat(value.substring(obfuscateQuantity));
    }

}

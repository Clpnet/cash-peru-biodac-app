package com.acj.client.prosegur.util;

import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import com.acj.client.prosegur.config.SessionConfig;
import com.google.gson.GsonBuilder;
import com.microsoft.identity.client.ISingleAccountPublicClientApplication;
import com.microsoft.identity.client.exception.MsalException;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

public class Util {

    private final static String LOG_TAG = Util.class.getSimpleName();

    public static String bytesToString(byte[] bytesATransformar) {
        String base64 = Base64.encodeToString(bytesATransformar, Base64.NO_WRAP);
        Log.d(LOG_TAG, "Byte to string: " + base64);
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

    public static <T> T jsonToClass(Object data, Class<T> clazz) {
        GsonBuilder gson = new GsonBuilder();
        String json = gson.create().toJson(data);
        return gson.create().fromJson(json, clazz);
    }

    public static void killSessionOnMicrosoft() {
        ISingleAccountPublicClientApplication mSingleAccountApp = SessionConfig.getInstance().getMSingleAccountApp();

        if (Objects.isNull(mSingleAccountApp)) return;

        Log.i(LOG_TAG, "Cerrando al sesion activa en microsoft");

        mSingleAccountApp.signOut(new ISingleAccountPublicClientApplication.SignOutCallback() {
            @Override
            public void onSignOut() {
                Log.i(LOG_TAG, "La sesion se cerro correctamente");
                SessionConfig.getInstance().setMAccount(null);
            }

            @Override
            public void onError(@NonNull MsalException exception) {
                Log.e(LOG_TAG, "Ocurrio un error en el cierre de sesion del usuario. Exception: " + exception);
            }
        });
    }

}

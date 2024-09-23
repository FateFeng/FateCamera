package com.feng.libcamera.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

public abstract class Extensions {

    private static final String TAG = Extensions.class.getSimpleName();

    public static byte[] getBytes(InputStream inputStream) {
        try {
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            inputStream.close();
            return bytes;
        } catch (IOException e) {
            Log.w(TAG, e);
        }

        return new byte[0];
    }

    public static byte[] getBytes(AssetManager assetManager, String fileName) {
        try {
            return getBytes(assetManager.open(fileName));
        } catch (IOException e) {
            Log.w(TAG, e);
        }

        return new byte[0];
    }

    public static String readTextFileFromResource(Context context, int resourceId) {
        return new String(Extensions.getBytes(context.getResources().openRawResource(resourceId)));
    }

}

package com.feng.libcamera.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;
import android.util.Size;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

public class Util {

    public static int createOESTextureObject() {
        int[] tex = new int[1];
        GLES20.glGenTextures(1, tex, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, tex[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        return tex[0];
    }

    public static boolean getHasNormalCamera() {
        return Camera.getNumberOfCameras() > 0;
    }

    public static boolean getHasUsbCamera(UsbManager mUsbManager) {
        HashMap<String, UsbDevice> deviceMap = mUsbManager.getDeviceList();
        if (deviceMap != null) {
            for (UsbDevice usbDevice : deviceMap.values()) {
                if (isUsbCamera(usbDevice)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断某usb设备是否摄像头，usb摄像头的大小类是239-2
     *
     * @param usbDevice
     * @return
     */
    public static boolean isUsbCamera(UsbDevice usbDevice) {
        return usbDevice != null && 239 == usbDevice.getDeviceClass() && 2 == usbDevice.getDeviceSubclass();
    }

    public static String readShaderFromResource(Context context, int resourceId) {
        StringBuilder builder = new StringBuilder();
        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            is = context.getResources().openRawResource(resourceId);
            isr = new InputStreamReader(is);
            br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                builder.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                    is = null;
                }
                if (isr != null) {
                    isr.close();
                    isr = null;
                }
                if (br != null) {
                    br.close();
                    br = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return builder.toString();
    }

    /**
     * 获取预览最后尺寸
     *
     * camera 和 ui 的宽高是反的
     *
     */
    public static Camera.Size getBestSize1(List<Camera.Size> sizes, int w, int h) {
        if (sizes== null ||sizes.size() == 0) {
            return  null;
        }
        Camera.Size bestSize = sizes.get(0);
        float uiRatio = (float) h / w;
        float minRatio = uiRatio;
        for (Camera.Size previewSize : sizes) {
            float cameraRatio = (float) previewSize.width / previewSize.height;

            //如果找不到比例相同的，找一个最近的,防止预览变形
            float offset = Math.abs(cameraRatio - uiRatio);
            if (offset < minRatio) {
                minRatio = offset;
                bestSize = previewSize;
            }
            //比例相同
            if (uiRatio == cameraRatio) {
                bestSize = previewSize;
                break;
            }

        }
        Log.d("fate", " previewSize : " + bestSize.width + " x " + bestSize.height);
        return bestSize;
    }

    public static Size getBestSize2(List<Size> sizes, int w, int h) {
        if (sizes== null ||sizes.size() == 0) {
            return  null;
        }
        Size bestSize = sizes.get(0);
        float uiRatio = (float) h / w;
        float minRatio = uiRatio;
        for (Size previewSize : sizes) {
            float cameraRatio = (float) previewSize.getWidth() / previewSize.getHeight();

            //如果找不到比例相同的，找一个最近的,防止预览变形
            float offset = Math.abs(cameraRatio - uiRatio);
            if (offset < minRatio) {
                minRatio = offset;
                bestSize = previewSize;
            }
            //比例相同
            if (uiRatio == cameraRatio) {
                bestSize = previewSize;
                break;
            }

        }

        Log.d("fate", " previewSize : " + bestSize.getWidth() + " x " + bestSize.getHeight());
        return bestSize;
    }

    public static Size getBestSize3(List<Size> sizes, int w, int h) {
        if (sizes== null ||sizes.size() == 0) {
            return  null;
        }
        Size bestSize = sizes.get(0);
        float uiRatio = (float) h / w;
        float minRatio = uiRatio;
        for (Size previewSize : sizes) {
            if (previewSize.getWidth() > 1920 || previewSize.getHeight() > 1920) {
                continue;
            }
            float cameraRatio = (float) previewSize.getWidth() / previewSize.getHeight();

            //如果找不到比例相同的，找一个最近的,防止预览变形
            float offset = Math.abs(cameraRatio - uiRatio);
            if (offset < minRatio) {
                minRatio = offset;
                bestSize = previewSize;
            }
            //比例相同
            if (uiRatio == cameraRatio) {
                bestSize = previewSize;
                break;
            }

        }

        Log.d("fate", " previewSize : " + bestSize.getWidth() + " x " + bestSize.getHeight());
        return bestSize;
    }

    public static Bitmap rotate(Bitmap bitmap, float degress){
        Matrix matrix = new Matrix();
        matrix.postRotate(degress);
        return Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
    }

}

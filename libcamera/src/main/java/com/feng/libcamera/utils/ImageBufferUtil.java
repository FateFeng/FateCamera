package com.feng.libcamera.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.media.Image;
import android.os.FileUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by hyj on 2020/9/16.
 * Image转换为I420和NV21格式byte数组
 */
public class ImageBufferUtil {
    private static final int COLOR_FormatI420 = 1;
    private static final int COLOR_FormatNV21 = 2;
    private static int imageWidth;
    private static int imageHeight;
    private static byte[] data, rowData;
    private static byte[] mCameraNV21Byte;

    private static boolean isImageFormatSupported(Image image) {
        int format = image.getFormat();
        switch (format) {
            case ImageFormat.YUV_420_888:
            case ImageFormat.NV21:
            case ImageFormat.YV12:
                return true;
        }
        return false;
    }

    public static byte[] getNV21(Image image) {
        return getDataFromImage(image, COLOR_FormatNV21);
    }

    public static byte[] getDataFromImage(Image image, int colorFormat) {
        Image.Plane[] planes = image.getPlanes();
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        int yRowStride = planes[0].getRowStride();
        int uRowStride = planes[1].getRowStride();
        int vRowStride = planes[2].getRowStride();
        int uPixelStride = planes[1].getPixelStride();
        int vPixelStride = planes[2].getPixelStride();
        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        int uBufferSize = uBuffer.capacity() - uBuffer.position();
        ByteBuffer vBuffer = planes[2].getBuffer();
        int vBufferSize = vBuffer.capacity() - vBuffer.position();
        if (mCameraNV21Byte == null || mCameraNV21Byte.length < imageWidth * imageHeight * 3 / 2) {
            // allocate NV21 buffer
            mCameraNV21Byte = new byte[imageWidth * imageHeight * 3 / 2];
        }

        assert (image.getPlanes()[0].getPixelStride() == 1);

        int pos = 0;
        if (yRowStride == imageWidth) { // likely
            yBuffer.get(mCameraNV21Byte, 0, imageWidth * imageHeight);
            pos += imageWidth * imageHeight;
        } else {
            int yBufferPos = 0; // not an actual position
            for (; pos < imageWidth * imageHeight; pos += imageWidth) {
                yBuffer.position(yBufferPos);
                yBuffer.get(mCameraNV21Byte, pos, imageWidth);
                yBufferPos += yRowStride;
            }
        }

        if (uPixelStride == 1 && vPixelStride == 1) {
            //I420 format, each plane is seperate
            uBuffer.get(mCameraNV21Byte, imageWidth * imageHeight, uBufferSize);
            vBuffer.get(mCameraNV21Byte, imageWidth * imageHeight + imageWidth * imageHeight / 4, vBufferSize);
            //mPixelFormat = 0;
        } else if (uPixelStride == 2 && vPixelStride == 2) {
            //NV21 format, UV is packed in one buffer
            // uv in one buffer, v buffer is just offset one U pixel

            if (uRowStride == imageWidth) {
                vBuffer.get(mCameraNV21Byte, imageWidth * imageHeight, uBufferSize);
            } else {
                pos = 0;
                int vBufferPos = -vRowStride; // not an actual position
                for (int i = 0; i < imageHeight / 2; pos += imageWidth, i++) {
                    vBufferPos += vRowStride;
                    vBuffer.position(vBufferPos);
                    if (i == imageHeight / 2 - 1) {
                        vBuffer.get(mCameraNV21Byte, imageWidth * imageHeight + pos, vBufferSize - vBufferPos);
                    } else {
                        vBuffer.get(mCameraNV21Byte, imageWidth * imageHeight + pos, imageWidth);
                    }
                }
            }
        }
        return mCameraNV21Byte;
    }

    public static void saveBitmap(String name, Bitmap bm, Context mContext) {
        Log.d("Save Bitmap", "Ready to save picture");
        //指定我们想要存储文件的地址
        String TargetPath = mContext.getFilesDir() + "/images/";
        Log.d("Save Bitmap", "Save Path=" + TargetPath);
        //判断指定文件夹的路径是否存在
        if (!fileIsExist(TargetPath)) {
            Log.d("Save Bitmap", "TargetPath isn't exist");
        } else {
            //如果指定文件夹创建成功，那么我们则需要进行图片存储操作
            File saveFile = new File(TargetPath, name);

            try {
                FileOutputStream saveImgOut = new FileOutputStream(saveFile);
                // compress - 压缩的意思
                bm.compress(Bitmap.CompressFormat.JPEG, 80, saveImgOut);
                //存储完成后需要清除相关的进程
                saveImgOut.flush();
                saveImgOut.close();
                Log.d("Save Bitmap", "The picture is save to your phone!");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }


    static boolean fileIsExist(String fileName)
    {
        //传入指定的路径，然后判断路径是否存在
        File file=new File(fileName);
        if (file.exists())
            return true;
        else{
            //file.mkdirs() 创建文件夹的意思
            return file.mkdirs();
        }
    }
}

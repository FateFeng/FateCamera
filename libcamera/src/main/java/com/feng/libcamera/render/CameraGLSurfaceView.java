package com.feng.libcamera.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.Surface;

import com.feng.libcamera.interfaces.OnSurfaceAvailableListener;


public class CameraGLSurfaceView extends GLSurfaceView {
    public static final String TAG = "CameraGLSurfaceView";

    private CameraRender mCameraRender;

    public void init(Context context) {
        setEGLContextClientVersion(2);

        mCameraRender = new CameraRender();
        mCameraRender.init(context, this);

        setRenderer(mCameraRender);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    public CameraGLSurfaceView(Context context) {
        super(context);
        init(context);
    }

    public CameraGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CameraRender getCameraRender() {
        return mCameraRender;
    }

    public void setOnFrameAvailableListener(OnSurfaceAvailableListener onFrameAvailableListener) {
        if (mCameraRender == null) {
            return;
        }
        mCameraRender.setOnFrameAvailableListener(onFrameAvailableListener);
    }

    public void deinit() {
        if (mCameraRender != null) {
            mCameraRender.deinit();
            mCameraRender = null;
        }
    }
}

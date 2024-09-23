package com.feng.libcamera.render;

import static android.opengl.GLES20.GL_FRAMEBUFFER;
import static android.opengl.GLES20.glBindFramebuffer;
import static android.opengl.GLES20.glGenFramebuffers;
import static android.opengl.GLES20.glViewport;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.Surface;

import com.feng.libcamera.interfaces.OnSurfaceAvailableListener;
import com.feng.libcamera.utils.Util;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraRender implements GLSurfaceView.Renderer {

    public static final String TAG = "CameraRender";
    private Context mContext;
    private GLSurfaceView mGlSurfaceView;
    private Surface mPreviewSurface;
    boolean bIsPreviewStarted;
    private int mOESTextureId = -1;

    private SurfaceTexture mSurfaceTexture;
    private float[] mTransformMatrix = new float[16];
    private RenderEngine mRenderEngine;
    private OnSurfaceAvailableListener onFrameAvailableListener;

    private int[] mFBOIds = new int[1];

    public void init(Context context, GLSurfaceView surfaceView) {
        mContext = context;
        mGlSurfaceView = surfaceView;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mOESTextureId = Util.createOESTextureObject();
        mRenderEngine = new RenderEngine(mOESTextureId, mContext);

        glGenFramebuffers(1, mFBOIds, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, mFBOIds[0]);

        initSurfaceTexture();
        Log.i(TAG, "onSurfaceCreated: mFBOId: " + mFBOIds[0]);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);
        Log.i(TAG, "onSurfaceChanged: " + width + ", " + height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (!bIsPreviewStarted) {
            return;
        }

        if (mSurfaceTexture != null) {
            mSurfaceTexture.updateTexImage();
            mSurfaceTexture.getTransformMatrix(mTransformMatrix);
            //Matrix.rotateM(transformMatrix,0,360f,0f,1f,0f);
        }

        getFrameRate();

        mRenderEngine.drawTexture(mTransformMatrix, RenderEngine.sTransRotate);
    }

    long lastTimestamp = System.currentTimeMillis();

    public String getFrameRate() {
        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - lastTimestamp;
        float fps = 1000f / timeDiff;

        lastTimestamp = currentTime;
        String result = String.valueOf((int) fps);
        if (onFrameAvailableListener != null) {
            onFrameAvailableListener.onFpsChangeListener(result);
        } else {
            Log.d(TAG, "fate FPS: " + fps);
        }
        return result;
    }

    public boolean initSurfaceTexture() {
        if (mGlSurfaceView == null) {
            Log.i(TAG, "mCamera or mGLSurfaceView is null!");
            return false;
        }
        mSurfaceTexture = new SurfaceTexture(mOESTextureId);
        mSurfaceTexture.setOnFrameAvailableListener(surfaceTexture -> {
            if (!bIsPreviewStarted) {
                Log.i(TAG, "mCamera firstframe recived");
            }
            bIsPreviewStarted = true;
            mGlSurfaceView.requestRender();
        });
        if (onFrameAvailableListener != null) {
            onFrameAvailableListener.onSurfaceAvailableListener();
        }
        return true;
    }

    public SurfaceTexture getSurfaceTexture() {
        return mSurfaceTexture;
    }

    public Surface getSurface() {
        if (mPreviewSurface == null) {
            final SurfaceTexture st = getSurfaceTexture();
            if (st != null) {
                mPreviewSurface = new Surface(st);
            }
        }
        return mPreviewSurface;
    }

    public OnSurfaceAvailableListener getOnFrameAvailableListener() {
        return onFrameAvailableListener;
    }

    public void setOnFrameAvailableListener(OnSurfaceAvailableListener onFrameAvailableListener) {
        this.onFrameAvailableListener = onFrameAvailableListener;
    }

    public void deinit() {
        if (mRenderEngine != null) {
            mRenderEngine.deinit();
            mRenderEngine = null;
        }

        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
        onFrameAvailableListener = null;
        mOESTextureId = -1;
        bIsPreviewStarted = false;
    }
}

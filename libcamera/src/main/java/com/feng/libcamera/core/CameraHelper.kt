package com.feng.libcamera.core

import android.graphics.SurfaceTexture
import android.util.Log
import android.widget.Toast
import androidx.annotation.StringRes
import com.feng.libcamera.base.BaseFragment
import com.feng.libcamera.interfaces.OnCameraOpenListener
import com.feng.libcamera.render.CameraGLSurfaceView

abstract class CameraHelper {

    val TAG = javaClass.simpleName

    var mBaseFragment: BaseFragment? = null
    var mScreemWidth: Int = 1920
    var mScreemHeight: Int = 1080
    var mCameraId: String? = null
    //var mOnCameraOpenListener: OnCameraOpenListener? = null

    constructor(mContext: BaseFragment?) {
        this.mBaseFragment = mContext
        initCamera()
    }

    open fun initCamera() {
    }

    abstract fun getCameraIds(): MutableList<String>

    open fun setupCamera(screenWidth: Int, screenHeight: Int, cameraId: String) {
        mScreemWidth = screenWidth
        mScreemHeight = screenHeight
        mCameraId = cameraId
    }

    open fun switchCamera(cameraId: String) {
        mCameraId = cameraId
    }

    abstract fun openCamera(onCameraOpenListener: OnCameraOpenListener)

    abstract fun isCameraOpen(): Boolean

    abstract fun setDisplaySurface(glSurfaceView: CameraGLSurfaceView,surfaceTexture: SurfaceTexture)

    abstract fun startPreview()

    abstract fun stopPreview()

    abstract fun releaseCamera()

    abstract fun updateCameraOrientation(displayOrientation: Int)

    abstract fun updateCameraRatio(ratio: Float)

    abstract fun takePicture(path: String)

    open fun notify(@StringRes message: Int) =
        Toast.makeText(mBaseFragment!!.context, message, Toast.LENGTH_SHORT).show()

    open fun notify(message: String) {
        Toast.makeText(mBaseFragment!!.context, message, Toast.LENGTH_SHORT).show()
        cameraLog(message)
    }


    open fun cameraLog(out: String) {
        Log.d(TAG, out)
    }
}
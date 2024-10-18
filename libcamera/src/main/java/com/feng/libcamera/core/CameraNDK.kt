package com.feng.libcamera.core

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.params.StreamConfigurationMap
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.view.Surface
import androidx.annotation.RequiresApi
import com.feng.libcamera.base.BaseFragment
import com.feng.libcamera.interfaces.OnCameraOpenListener
import com.feng.libcamera.render.CameraGLSurfaceView
import com.feng.libcamera.utils.Util.getBestSize2


class CameraNDK(mContext: BaseFragment?) : CameraHelper(mContext) {

    var mCameraDevice: Long? = null
    var displayOrientation: Int = 0
    var mCameraHandler: Handler? = null
    var mCameraThread: HandlerThread? = null
    var cameraManager: CameraManager? = null
    var mSurfaceTexture: SurfaceTexture? = null
    var mSurface: Surface? = null
    var mGLSurfaceView: CameraGLSurfaceView? = null
    var isOpened: Boolean = true
    var numCamera: Int = 0

    override fun initCamera() {
        super.initCamera()
        cameraManager = mBaseFragment!!.requireContext()
            .getSystemService(Context.CAMERA_SERVICE) as CameraManager
        mCameraDevice = createCamera()
    }

    override fun getCameraIds(): MutableList<String>? {
        //临时 懒得弄暂时
        if (numCamera == 0) {
            return null;
        }
        var cameraIds: MutableList<String> = cameraManager!!.cameraIdList.toMutableList()

        for (cameraId in cameraIds) {
            val characteristics: CameraCharacteristics = cameraManager!!
                .getCameraCharacteristics(cameraId)

            cameraLog(
                "cameraId  : " + cameraId + "   face :  " + characteristics.get(
                    CameraCharacteristics.LENS_FACING
                )
            )
        }

        cameraLog("cameraIds number : " + cameraIds.size)
        return cameraIds
    }

    override fun setupCamera(screenWidth: Int, screenHeight: Int, cameraId: String) {
        super.setupCamera(screenWidth, screenHeight, cameraId)
        mCameraThread = HandlerThread("CameraThread")
        mCameraThread!!.start()
        mCameraHandler = Handler(mCameraThread!!.getLooper())
    }

    @SuppressLint("MissingPermission")
    override fun openCamera(onCameraOpenListener: OnCameraOpenListener) {
        try {
            // 创建相机对象，这个对象由CPP维护
            openCamera(mCameraDevice!!, mCameraId!!)
            if (isOpened) {
                onCameraOpenListener.onOpen()
            } else {
                onCameraOpenListener.onFail()
            }
        } catch (ex: Exception) {
            onCameraOpenListener.onFail()
        }
    }

    override fun isCameraOpen(): Boolean {
        return mCameraDevice != null
    }

    override fun setDisplaySurface(
        glSurfaceView: CameraGLSurfaceView,
        surfaceTexture: SurfaceTexture
    ) {
        mGLSurfaceView = glSurfaceView
        mSurfaceTexture = surfaceTexture
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun startPreview() {
        if (!isCameraOpen()) {
            return
        }
        val characteristics: CameraCharacteristics = cameraManager!!.getCameraCharacteristics(
            mCameraId!!
        )
        val map: StreamConfigurationMap? =
            characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        var mPreviewSize: Size = getBestSize2(
            map!!.getOutputSizes(ImageFormat.YUV_420_888).toList(),
            mScreemWidth,
            mScreemHeight
        )

        updatePreviewSize(
            mCameraDevice!!,
            mPreviewSize.getWidth(),
            mPreviewSize.getHeight(),
            displayOrientation
        )
        mSurfaceTexture!!.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight())
        mSurface = Surface(mSurfaceTexture)

        try {
            onPreviewSurfaceCreated(mCameraDevice!!, mSurface!!)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun stopPreview() {
        if (!isCameraOpen()) {
            return
        }
        if (mSurface != null) {
            onPreviewSurfaceDestroyed(mCameraDevice!!, mSurface!!)
        }
    }

    override fun releaseCamera() {
        if (!isCameraOpen()) {
            return
        }
        destroyCamera(mCameraDevice!!)
        mCameraDevice = null
    }

    override fun takePicture(path: String) {
        if (!isCameraOpen()) {
            return
        }

    }

    override fun updateCameraRatio(ratio: Float) {

    }

    override fun updateCameraOrientation(displayOrientation: Int) {
        val info = Camera.CameraInfo()
        Camera.getCameraInfo(mCameraId!!.toInt(), info)

        var degrees = 0
        when (displayOrientation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }

        var result: Int
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360
            result = (360 - result) % 360 // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360
        }
        cameraLog("updateCameraOrientation :" + result)
        this.displayOrientation = result
    }

    /**
     * A native method that is implemented by the 'tcamera' native library,
     * which is packaged with this application.
     */
    private external fun createCamera(): Long
    private external fun openCamera(ndkCamera: Long, cameraId: String)
    private external fun updatePreviewSize(
        ndkCamera: Long,
        width: Int,
        height: Int,
        rotation: Int
    ): Long

    private external fun onPreviewSurfaceCreated(ndkCamera: Long, surface: Surface)
    private external fun onPreviewSurfaceDestroyed(ndkCamera: Long, surface: Surface)
    private external fun destroyCamera(ndk_camera: Long)

    // 其他接口
    private external fun getCompatiblePreviewSize(ndkCamera: Long): Size?

    // 网络接口测试
//    private external fun testSocket()
//    private external fun testSocketClient()

    companion object {
        init {
            System.loadLibrary("libndkcamera")
        }
    }
}
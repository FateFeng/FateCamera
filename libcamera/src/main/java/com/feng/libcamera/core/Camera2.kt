package com.feng.libcamera.core

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.hardware.camera2.*
import android.hardware.camera2.CameraDevice.StateCallback
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.StreamConfigurationMap
import android.media.ImageReader
import android.media.ImageReader.OnImageAvailableListener
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.view.Surface
import androidx.annotation.RequiresApi
import com.feng.libcamera.base.BaseFragment
import com.feng.libcamera.interfaces.OnCameraOpenListener
import com.feng.libcamera.render.CameraGLSurfaceView
import com.feng.libcamera.utils.ImageBufferUtil
import com.feng.libcamera.utils.Util.getBestSize2
import java.io.ByteArrayOutputStream
import java.util.*


class Camera2(mContext: BaseFragment?) : CameraHelper(mContext) {

    var mCameraDevice: CameraDevice? = null
    var displayOrientation: Int = 0
    var mCameraHandler: Handler? = null
    var mCameraThread: HandlerThread? = null
    var cameraManager: CameraManager? = null
    var mSurfaceTexture: SurfaceTexture? = null
    var mGLSurfaceView: CameraGLSurfaceView? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun initCamera() {
        super.initCamera()
        cameraManager = mBaseFragment!!.requireContext()
            .getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    override fun getCameraIds(): MutableList<String> {
        var cameraIds: MutableList<String> = cameraManager!!.cameraIdList.toMutableList()

        for (cameraId in cameraIds) {
            val characteristics: CameraCharacteristics = cameraManager!!
                .getCameraCharacteristics(cameraId)
            cameraLog("cameraId  : " + cameraId + "   face :  " + characteristics.get(CameraCharacteristics.LENS_FACING))
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
            cameraManager!!.openCamera(
                mCameraId!!, object : StateCallback() {
                    override fun onOpened(camera: CameraDevice) {
                        cameraLog("onOpen")
                        mCameraDevice = camera
                        onCameraOpenListener.onOpen()
                    }

                    override fun onDisconnected(camera: CameraDevice) {
                        cameraLog("onDisconnected")
                        mCameraDevice?.close()
                        mCameraDevice = null
                        onCameraOpenListener.onFail()
                    }

                    override fun onError(camera: CameraDevice, error: Int) {
                        var errorInfo = error.toString()
                        when(error) {
                            ERROR_CAMERA_IN_USE-> errorInfo = "ERROR_CAMERA_IN_USE";
                            ERROR_MAX_CAMERAS_IN_USE-> errorInfo = "ERROR_MAX_CAMERAS_IN_USE";
                            ERROR_CAMERA_DISABLED-> errorInfo = "ERROR_CAMERA_DISABLED";
                            ERROR_CAMERA_DEVICE-> errorInfo = "ERROR_CAMERA_DEVICE";
                            ERROR_CAMERA_SERVICE-> errorInfo = "ERROR_CAMERA_SERVICE";
                        }
                        cameraLog("onError : " + errorInfo)
                        mCameraDevice?.close()
                        mCameraDevice = null
                        onCameraOpenListener.onFail()
                    }

                }, mCameraHandler
            )
        } catch (ex: CameraAccessException) {
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

        var imageReader = ImageReader.newInstance(
            mPreviewSize.width, mPreviewSize.height, ImageFormat.YUV_420_888, 3)
        var index = 0

        imageReader.setOnImageAvailableListener(object : OnImageAvailableListener{
            override fun onImageAvailable(reader: ImageReader?) {
                index++;
                cameraLog("feng")
                if (index == 50){
                    val image = reader!!.acquireLatestImage()

                    val yuvImage = YuvImage(ImageBufferUtil.getNV21(image), ImageFormat.NV21, mPreviewSize.getWidth(), mPreviewSize.getHeight(), null)
                    val stream = ByteArrayOutputStream()
                    yuvImage.compressToJpeg(Rect(0, 0, mPreviewSize.getWidth(), mPreviewSize.getHeight()), 80, stream)
                    val newBitmap =
                        BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size())
                    stream.close()

                    ImageBufferUtil.saveBitmap("yuv", newBitmap, mBaseFragment!!.requireContext())
                }
            }

        }, mCameraHandler)

        mSurfaceTexture!!.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight())
        val surface = Surface(mSurfaceTexture)


        try {
            var mCaptureRequestBuilder: CaptureRequest.Builder =
                mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)

            mCaptureRequestBuilder.addTarget(surface)

            var configuration = OutputConfiguration(-1, surface)

            mCameraDevice!!.createCaptureSession(
                Arrays.asList(surface, imageReader.surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        try {
                            var mCaptureRequest = mCaptureRequestBuilder.build()

                            var mCameraCaptureSession = session
                            mCameraCaptureSession.setRepeatingRequest(
                                mCaptureRequest,
                                null,
                                mCameraHandler
                            )
                        } catch (e: CameraAccessException) {
                            e.printStackTrace()
                        }
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {}
                },
                mCameraHandler
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    override fun stopPreview() {
        if (!isCameraOpen()) {
            return
        }
    }

    override fun releaseCamera() {
        if (!isCameraOpen()) {
            return
        }
        mCameraDevice?.close()
        mCameraDevice = null
    }

    override fun takePicture(path: String) {
        if (!isCameraOpen()) {
            return
        }

    }

    override fun updateCameraOrientation(displayOrientation: Int) {
        this.displayOrientation = displayOrientation
    }

    override fun updateCameraRatio(ratio: Float) {

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setSunmiDisplayOrientation() {
        val temp = Class.forName("android.hardware.Camera");
        val size = temp.methods.size
        cameraLog("method  :" + size)

        cameraLog("fengshuai   :" + Build.getSerial())

        var degree = 180;

        for (mm in temp.methods){
            cameraLog("method   :" + mm.name)
        }
        val method = temp.getMethod("setSunmiDisplayOrientation", Integer.TYPE)
        cameraLog("method  :" + (method == null))
        method.invoke(null, degree)
    }
}
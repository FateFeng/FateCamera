package com.feng.libcamera.core

import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.hardware.Camera.PictureCallback
import android.view.Surface
import com.feng.libcamera.base.BaseFragment
import com.feng.libcamera.extension.SavePictureAsyncTask
import com.feng.libcamera.interfaces.OnCameraOpenListener
import com.feng.libcamera.render.CameraGLSurfaceView
import com.feng.libcamera.utils.Util.getBestSize1

class Camera1(mContext: BaseFragment?) : CameraHelper(mContext) {

    var mCameraDevice: Camera? = null

    var displayOrientation: Int = 0
    var mSurfaceTexture: SurfaceTexture? = null
    var mGLSurfaceView: CameraGLSurfaceView? = null

    override fun getCameraIds(): MutableList<String> {
        var cameraIds: MutableList<String> = mutableListOf()
        val number = Camera.getNumberOfCameras()
        for (i in 0 until number) {
            cameraIds.add(i.toString())
        }
        cameraLog("cameraIds number : " + cameraIds.size)
        return cameraIds
    }

    override fun openCamera(onCameraOpenListener: OnCameraOpenListener) {
        try {
            mCameraDevice = Camera.open(mCameraId!!.toInt())
            val parameters = mCameraDevice!!.getParameters()

            val previewSizes = parameters.supportedPreviewSizes
            val previewSize: Camera.Size = getBestSize1(previewSizes, mScreemWidth, mScreemHeight)

            parameters["orientation"] = "portrait"
            startContinuousAutoFocus()
            cameraLog("previewSize :" + previewSize.width + " x " + previewSize.height)
            parameters.setPreviewSize(previewSize.width, previewSize.height)

            val pictureSizes = parameters.supportedPictureSizes
            val pictureSize: Camera.Size = getBestSize1(pictureSizes, mScreemWidth, mScreemHeight)
            parameters.setPictureSize(pictureSize.width, pictureSize.height)
            cameraLog("parameters.maxExposureCompensation :" + parameters.maxExposureCompensation)
            cameraLog("parameters.minExposureCompensation :" + parameters.minExposureCompensation)

            parameters.exposureCompensation = 0

            mCameraDevice!!.apply {
                setParameters(parameters)
            }.run {
                /*setPreviewCallback(object : Camera.PreviewCallback {
                    override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
                        //mGLSurfaceView?.requestRender()
                    }
                })*/
            }.also {
                cameraLog("open camera")
                onCameraOpenListener.onOpen()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onCameraOpenListener.onFail()
        }
    }

    fun startContinuousAutoFocus(): Boolean {
        val params: Camera.Parameters = mCameraDevice!!.getParameters()
        val focusModes = params.supportedFocusModes
        val CAF_PICTURE: String = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
        val CAF_VIDEO: String = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
        val supportedMode = if (focusModes
                .contains(CAF_PICTURE)
        ) CAF_PICTURE else if (focusModes
                .contains(CAF_VIDEO)
        ) CAF_VIDEO else ""
        if (supportedMode != "") {
            params.focusMode = supportedMode
            mCameraDevice!!.setParameters(params)
            return true
        }
        return false
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

    override fun startPreview() {
        mCameraDevice?.run {
            setPreviewTexture(mSurfaceTexture)
            startPreview()
        }
    }

    override fun stopPreview() {
        mCameraDevice?.stopPreview()
    }

    override fun releaseCamera() {
        mCameraDevice?.release()
        mCameraDevice = null
    }

    override fun takePicture(path: String) {

        val jpegCallback = object : PictureCallback {
            override fun onPictureTaken(data: ByteArray?, camera: Camera?) {
                camera?.startPreview()

                val savework = SavePictureAsyncTask(data, displayOrientation, path);
                savework.execute()
            }

        }

        mCameraDevice!!.takePicture(null, null, jpegCallback)
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
        mCameraDevice?.setDisplayOrientation(result)
    }

    override fun updateCameraRatio(ratio: Float) {
    }

}
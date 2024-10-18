package com.feng.libcamera.core

import android.annotation.SuppressLint
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.hardware.usb.UsbDevice
import android.os.Build
import android.util.Size
import android.view.Surface
import androidx.annotation.RequiresApi
import com.feng.libcamera.base.BaseFragment
import com.feng.libcamera.extension.appContext
import com.feng.libcamera.interfaces.OnCameraOpenListener
import com.feng.libcamera.render.CameraGLSurfaceView
import com.feng.libcamera.utils.Util.getBestSize2
import com.feng.libcamera.utils.Util.getBestSize3
import com.ifeng.f_uvccamera.UVCCameraProxy
import com.ifeng.f_uvccamera.bean.PicturePath
import com.ifeng.f_uvccamera.callback.ConnectCallback


class CameraUVC(mContext: BaseFragment?) : CameraHelper(mContext) {

    var mCameraDevice: UVCCameraProxy? = null
    var displayOrientation: Int = 0

    var mSurfaceTexture: SurfaceTexture? = null
    var mGLSurfaceView: CameraGLSurfaceView? = null
    var onCameraOpenListener: OnCameraOpenListener? = null

    override fun initCamera() {
        super.initCamera()
        mCameraDevice = UVCCameraProxy(mBaseFragment!!.appContext).apply {
            getConfig().isDebug(true)
                .setPicturePath(PicturePath.APPCACHE)
                .setDirName("uvccamera")
                .setProductId(0)
                .setVendorId(0)
        }.apply {
            setConnectCallback(object : ConnectCallback {
                override fun onAttached(usbDevice: UsbDevice?) {
                    requestPermission(usbDevice)
                }

                override fun onGranted(usbDevice: UsbDevice?, granted: Boolean) {
                    if (granted) {
                        connectDevice(usbDevice)
                    } else {
                        notify("usb 权限被拒绝")
                    }
                }

                override fun onConnected(usbDevice: UsbDevice?) {
                    openCamera()
                }

                override fun onCameraOpened() {
                    onCameraOpenListener?.onOpen()
                    //startPreview()
                }

                override fun onDetached(usbDevice: UsbDevice?) {
                    onCameraOpenListener?.onFail()
                    closeCamera()
                }
            })
        }.also {  }
    }

    override fun getCameraIds(): MutableList<String> {
        return mCameraDevice?.getUsbCameraDeviceList()!!.map { usbDevice -> usbDevice.deviceName }
            .toMutableList();
    }

    override fun setupCamera(screenWidth: Int, screenHeight: Int, cameraId: String) {
        super.setupCamera(screenWidth, screenHeight, cameraId)
        //mCameraDevice?.(screenWidth, screenHeight)
    }

    @SuppressLint("MissingPermission")
    override fun openCamera(onCameraOpenListener: OnCameraOpenListener) {
        try {
            this.onCameraOpenListener = onCameraOpenListener
            mCameraDevice!!.checkDevice()
            //val result = mCameraDevice!!.openCamera()
            //getCurrentCamera()?.setCameraStateCallBack(this)
            /*if (result == 0) {
                onCameraOpenListener.onOpen()
            } else {
                onCameraOpenListener.onFail()
            }*/
        } catch (ex: CameraAccessException) {
            onCameraOpenListener.onFail()
        }
    }

    override fun isCameraOpen(): Boolean {
        return mCameraDevice?.isCameraOpen ?: false
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
        try {
            var mPreviewSize: Size = getBestSize3(
                mCameraDevice!!.supportedPreviewSizes?.map { size: com.ifeng.f_uvccamera.uvc.Size? ->
                    Size(
                        size!!.width,
                        size!!.height
                    )
                },
                mScreemWidth,
                mScreemHeight
            )

            //mPreviewSize = Size(1920, 1080)

            mSurfaceTexture!!.setDefaultBufferSize(
                mPreviewSize.getWidth(),
                mPreviewSize.getHeight()
            )

            val surface = Surface(mSurfaceTexture)

            mCameraDevice!!.run {
                setPreviewSurface(surface)
                setPreviewSize(mPreviewSize.width, mPreviewSize.height)
                startPreview()
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    override fun stopPreview() {
        if (!isCameraOpen()) {
            return
        }
        mCameraDevice?.stopPreview()
    }

    override fun releaseCamera() {
        if (isCameraOpen()) {
            mCameraDevice?.stopPreview()
        }
        mCameraDevice?.closeCamera()
    }

    override fun takePicture(path: String) {
        if (!isCameraOpen()) {
            return
        }
        mCameraDevice!!.takePicture()
    }

    override fun updateCameraOrientation(displayOrientation: Int) {
        this.displayOrientation = displayOrientation
    }

    override fun updateCameraRatio(ratio: Float) {
    }

}
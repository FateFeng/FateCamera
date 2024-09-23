package com.feng.fatecamera.ui.camera

import android.graphics.SurfaceTexture
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.feng.fatecamera.constant.CameraVersion
import com.feng.fatecamera.databinding.FragmentCamerasBinding
import com.feng.libcamera.base.BaseFragment
import com.feng.libcamera.core.Camera1
import com.feng.libcamera.core.Camera2
import com.feng.libcamera.core.CameraHelper
import com.feng.libcamera.core.CameraNDK
import com.feng.libcamera.core.CameraUVC
import com.feng.libcamera.core.CameraX
import com.feng.libcamera.interfaces.OnCameraOpenListener
import com.feng.libcamera.interfaces.OnSurfaceAvailableListener
import com.feng.libcamera.render.CameraGLSurfaceView
import kotlin.math.roundToInt


class CameraFragments : BaseFragment() {

    companion object {
        fun newInstance() = CameraFragments()
    }

    private lateinit var viewModel: CameraViewModel
    private lateinit var binding: FragmentCamerasBinding

    private lateinit var version: CameraVersion

    private var mCameraGLSurfaceView: CameraGLSurfaceView? = null
    private var mOESSurfaceTexture: SurfaceTexture? = null

    private var mCameraIdList: MutableList<String>? = null
    private var mCameraId: String? = null
    private var mCameraIdIndex = 0
    private var mCamera: CameraHelper? = null

    private var bIsCameraInited = false
    private var mScreemWidth: Int = 1080
    private var mScreemHeight: Int = 1920


    private var mCameraGLSurfaceViewPlus: CameraGLSurfaceView? = null
    private var mOESSurfaceTexturePlus: SurfaceTexture? = null
    private var mCameraIdPlus: String? = null
    private var mCameraIdIndexPlus = 1
    private var mCameraPlus: CameraHelper? = null

    override fun config() {
        viewModel = ViewModelProvider(this).get(CameraViewModel::class.java)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun layoutView(): View {
        binding = FragmentCamerasBinding.inflate(layoutInflater).also {
            mCameraGLSurfaceView = it.viewCamera
            mCameraGLSurfaceViewPlus = it.viewCameraPlus

            it.cameraCaptureButton.setOnClickListener {
                if (!bIsCameraInited) {
                    return@setOnClickListener
                }
                takePicture()
            }
            it.cameraSwitchButton.setOnClickListener {
                if (!bIsCameraInited) {
                    return@setOnClickListener
                }
                switchCamera()
            }
        }
        return binding.root
    }

    fun initVersion(temp: CameraVersion) {
        cameraLog("initVersion " + temp)
        version = temp;
    }

    override fun init() {
        cameraLog("init")

        when (version) {
            CameraVersion.Version1 -> {
                mCamera = Camera1(this)
            }

            CameraVersion.Version2 -> {
                mCamera = Camera2(this)
            }

            CameraVersion.VersionX -> {
                mCamera = CameraX(this)
            }

            CameraVersion.VersionNDK -> {
                mCamera = CameraNDK(this)
            }

            CameraVersion.VersionUVC -> {
                mCamera = CameraUVC(this)
            }
        }

        mCameraPlus = Camera2(this)
        binding.title.text = version.toString()

        with(mCameraGLSurfaceView) {
            val dm = DisplayMetrics()
            activity?.windowManager?.defaultDisplay?.getMetrics(dm)

            var w = dm.widthPixels
            var h = dm.heightPixels

            val c: Float = 1920 / 1080.toFloat()

            /*
            *  这才是正确的逻辑
            *
            if (w > h) {
                mPreviewHeight = h
                mPreviewWidth = (h / c).roundToInt()
            } else {
                mPreviewWidth = w
                mPreviewHeight = (w * c).roundToInt()
            }*/

            if (w > h) { //为了商米设备。。。。
                mScreemHeight = h
                mScreemWidth = (h * c).roundToInt()
            } else {
                mScreemWidth = w
                mScreemHeight = (w * c).roundToInt()
            }

            this!!.layoutParams.width = mScreemWidth
            this!!.layoutParams.height = mScreemHeight

            setOnFrameAvailableListener(object : OnSurfaceAvailableListener {
                override fun onSurfaceAvailableListener() {
                    if (mOESSurfaceTexture == null) {
                        post { openCamera() }
                    }
                }

                override fun onFpsChangeListener(fps: String) {

                }
            })
        }

        with(mCameraGLSurfaceViewPlus) {
            val dm = DisplayMetrics()
            activity?.windowManager?.defaultDisplay?.getMetrics(dm)

            var w = dm.widthPixels
            var h = dm.heightPixels

            val c: Float = 1920 / 1080.toFloat()

            /*
            *  这才是正确的逻辑
            *
            if (w > h) {
                mPreviewHeight = h
                mPreviewWidth = (h / c).roundToInt()
            } else {
                mPreviewWidth = w
                mPreviewHeight = (w * c).roundToInt()
            }*/

            if (w > h) { //为了商米设备。。。。
                mScreemHeight = h
                mScreemWidth = (h * c).roundToInt()
            } else {
                mScreemWidth = w
                mScreemHeight = (w * c).roundToInt()
            }

            this!!.layoutParams.width = mScreemWidth / 2
            this!!.layoutParams.height = mScreemHeight / 2

            setOnFrameAvailableListener(object : OnSurfaceAvailableListener {
                override fun onSurfaceAvailableListener() {
                    post { openCameraPlus() }
                }

                override fun onFpsChangeListener(fps: String) {

                }
            })
        }

        mCameraIdList = mCamera?.getCameraIds()
        if (mCameraIdList!!.isEmpty()) {
            notify("  no cameraid ????????????  ")
            return
        }

        binding.cameraSwitchButton.apply {
            visibility = when (mCameraIdList!!.size) {
                1 -> View.GONE
                else -> View.VISIBLE
            }
        }

        mCameraId = mCameraIdList!!.get(mCameraIdIndex);
        mCamera!!.setupCamera(mScreemWidth, mScreemHeight, mCameraId!!)



        mCameraIdPlus = mCameraIdList!!.get(mCameraIdIndexPlus);
        mCameraPlus!!.setupCamera(mScreemWidth / 2, mScreemHeight / 2, mCameraIdPlus!!)


        mOESSurfaceTexture = mCameraGLSurfaceView?.cameraRender?.surfaceTexture
        if (mOESSurfaceTexture == null) {
            cameraLog("mOESSurfaceTexture == null warning")
            return
        }

        openCamera()
    }

    fun openCamera() {
        if (mCamera!!.mCameraId == null) {
            return
        }

        mOESSurfaceTexture = mCameraGLSurfaceView?.cameraRender?.surfaceTexture
        mCamera!!.setDisplaySurface(mCameraGLSurfaceView!!, mOESSurfaceTexture!!)

        mCamera!!.openCamera(object : OnCameraOpenListener {
            override fun onOpen() {
                val rotation = requireActivity().windowManager.defaultDisplay.rotation

                //only for camera 1
                mCamera!!.updateCameraOrientation(rotation)

                mCamera!!.startPreview()
                bIsCameraInited = true
            }

            override fun onFail() {
                bIsCameraInited = false
                cameraLog("opencamera fail")
            }

        })
    }

    fun openCameraPlus() {
        if (mCameraPlus!!.mCameraId == null) {
            return
        }

        mOESSurfaceTexturePlus = mCameraGLSurfaceViewPlus?.cameraRender?.surfaceTexture
        mCameraPlus!!.setDisplaySurface(mCameraGLSurfaceView!!, mOESSurfaceTexturePlus!!)

        mCameraPlus!!.openCamera(object : OnCameraOpenListener {
            override fun onOpen() {
                val rotation = requireActivity().windowManager.defaultDisplay.rotation

                //only for camera 1
                mCameraPlus!!.updateCameraOrientation(rotation)

                mCameraPlus!!.startPreview()
            }

            override fun onFail() {
                cameraLog("opencameraPlus fail")
            }

        })
    }

    fun switchCamera() {
        mCameraIdIndex = 1 - mCameraIdIndex;
        mCameraId = mCameraIdList!!.get(mCameraIdIndex);
        mCamera!!.releaseCamera()
        mCamera!!.switchCamera(mCameraId!!)
        openCamera()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun takePicture() {
        mCamera!!.takePicture(activity?.dataDir!!.absolutePath)
    }

    override fun onResume() {
        super.onResume()
        //mCameraGLSurfaceView?.onResume()
        mCamera?.startPreview()
    }

    override fun onPause() {
        super.onPause()
        //todo 原生有BUG  后面再修复
        //mCameraGLSurfaceView?.onPause()
        mCamera?.stopPreview()
    }

    override fun deInit() {
        super.deInit()
        mCameraGLSurfaceView?.onPause()
        mCameraGLSurfaceView?.deinit()
        mCameraGLSurfaceView = null

        mCamera?.stopPreview()
        mCamera?.releaseCamera()
        mCamera = null

        mCameraGLSurfaceViewPlus?.onPause()
        mCameraGLSurfaceViewPlus?.deinit()
        mCameraGLSurfaceViewPlus = null

        mCameraPlus?.stopPreview()
        mCameraPlus?.releaseCamera()
        mCameraPlus = null
    }
}
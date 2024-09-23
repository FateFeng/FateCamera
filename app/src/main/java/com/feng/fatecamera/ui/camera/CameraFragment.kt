package com.feng.fatecamera.ui.camera

import android.graphics.SurfaceTexture
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.feng.fatecamera.constant.CameraVersion
import com.feng.fatecamera.databinding.FragmentCameraBinding
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

class CameraFragment : BaseFragment() {

    companion object {
        fun newInstance() = CameraFragment()
    }

    private lateinit var viewModel: CameraViewModel
    private lateinit var binding: FragmentCameraBinding

    private lateinit var version: CameraVersion

    private var mCameraGLSurfaceView: CameraGLSurfaceView? = null
    private var mOESSurfaceTexture: SurfaceTexture? = null

    private var mCameraIdList: MutableList<String>? = null
    private var mCameraId: String? = null
    private var mCameraIdIndex = 0
    private var mCamera: CameraHelper? = null

    private var bIsCameraInited = false
    private var mScreemWidth: Int = 1920
    private var mScreemHeight: Int = 1080

    override fun config() {
        viewModel = ViewModelProvider(this).get(CameraViewModel::class.java)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun layoutView(): View {
        binding = FragmentCameraBinding.inflate(layoutInflater).also {
            mCameraGLSurfaceView = it.viewCamera

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

        binding.titleTv.text = version.toString()

        with(mCameraGLSurfaceView) {
            val dm = DisplayMetrics()
            activity?.windowManager?.defaultDisplay?.getMetrics(dm)

            var w = dm.widthPixels
            var h = dm.heightPixels
            this!!.layoutParams.width = w
            this!!.layoutParams.height = h

            val c: Float = 1920 / 1080.toFloat()

            mScreemWidth = w
            mScreemHeight = (w * c).roundToInt()

            /*if (w > h) {
                mScreemHeight = h
                mScreemWidth = (h * c).roundToInt()
            } else {
                mScreemWidth = w
                mScreemHeight = (w * c).roundToInt()
            }*/

            setOnFrameAvailableListener(object : OnSurfaceAvailableListener {
                override fun onSurfaceAvailableListener() {
                    if (mOESSurfaceTexture == null) {
                        post { openCamera() }
                    }
                }

                override fun onFpsChangeListener(fps: String) {
                    post({
                        binding.fpsTv.text = fps
                    })
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
        System.currentTimeMillis()

        mCameraId = mCameraIdList!!.get(mCameraIdIndex);
        mCamera!!.setupCamera(mScreemWidth, mScreemHeight, mCameraId!!)

        mOESSurfaceTexture = mCameraGLSurfaceView?.cameraRender?.surfaceTexture
        if (mOESSurfaceTexture == null) {
            cameraLog("mOESSurfaceTexture == null warning")
            return
        }

        openCamera()
    }

    fun openCamera() {
        if (mCamera!!.mCameraId == null || mCamera!!.isCameraOpen()) {
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
                if (version == CameraVersion.VersionUVC) {
                    notify("opencamera fail. 请检查设备是否有smile等类型应用占用usb")
                } else {
                    notify("opencamera fail")
                }
            }
        })
    }

    fun switchCamera() {
        mCameraIdIndex++;
        if (mCameraIdIndex >= mCameraIdList!!.size) {
            mCameraIdIndex = 0;
        }
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
        if (mCamera!!.isCameraOpen()) {
            mCamera?.startPreview()
        }
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
    }
}
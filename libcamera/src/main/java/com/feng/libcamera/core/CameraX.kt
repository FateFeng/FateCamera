package com.feng.libcamera.core

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.params.StreamConfigurationMap
import android.util.Log
import android.util.Size
import android.view.Surface
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.core.*
import androidx.camera.core.Preview.SurfaceProvider
import androidx.camera.core.impl.CameraValidator
import androidx.camera.lifecycle.ProcessCameraProvider
import com.feng.libcamera.base.BaseFragment
import com.feng.libcamera.interfaces.OnCameraOpenListener
import com.feng.libcamera.render.CameraGLSurfaceView
import com.feng.libcamera.utils.Util
import com.feng.libcamera.utils.Util.getBestSize2
import java.io.File
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


/** Helper type alias used for analysis use case callbacks */
typealias LumaListener = (luma: Double) -> Unit

class CameraX(mContext: BaseFragment?) : CameraHelper(mContext) {

    var displayOrientation: Int = 0

    var cameraManager: CameraManager? = null
    var mSurfaceTexture: SurfaceTexture? = null
    var mGLSurfaceView: CameraGLSurfaceView? = null

    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var mCameraDevice: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null

    /** Blocking camera operations are performed using this executor */
    private lateinit var cameraExecutor: ExecutorService

    override fun initCamera() {
        super.initCamera()
        cameraManager = mBaseFragment!!.requireContext()
            .getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    override fun getCameraIds(): MutableList<String> {
        var cameraIds: MutableList<String> = cameraManager!!.cameraIdList.toMutableList()

        cameraLog("cameraIds number : " + cameraIds.size)
        return cameraIds
    }

    override fun setupCamera(screenWidth: Int, screenHeight: Int, cameraId: String) {
        super.setupCamera(screenWidth, screenHeight, cameraId)
        // Initialize our background executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(mBaseFragment!!.requireContext())
        // CameraProvider
        cameraProvider = cameraProviderFuture.get()
        val hasBack = cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)
        cameraLog("hasBack: $hasBack")
    }

    @SuppressLint("MissingPermission", "RestrictedApi", "UnsafeOptInUsageError")
    override fun openCamera(onCameraOpenListener: OnCameraOpenListener) {
        // CameraProvider
        val cameraProvider = cameraProvider
            ?: throw IllegalStateException("Camera initialization failed.")

        // CameraSelector
        val characteristics: CameraCharacteristics = cameraManager!!.getCameraCharacteristics(
            mCameraId!!
        )

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(characteristics.get(CameraCharacteristics.LENS_FACING)!!).build()
        val map: StreamConfigurationMap? =
            characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        var mPreviewSize: Size = getBestSize2(
            map!!.getOutputSizes(ImageFormat.YUV_420_888).toList(),
            mScreemWidth,
            mScreemHeight
        )

        var builder = Preview.Builder()
        val extender = Camera2Interop.Extender(builder)

        // Preview
        preview = builder
            // We request aspect ratio but no resolution
            .setTargetResolution(mPreviewSize)
            .setMaxResolution(mPreviewSize)
            // Set initial target rotation, we will have to call this again if rotation changes
            // during the lifecycle of this use case
            .setTargetRotation(Surface.ROTATION_90)
            .build()

        videoCapture = VideoCapture.Builder().build()

        // ImageAnalysis
        imageAnalyzer = ImageAnalysis.Builder()
            // We request aspect ratio but no resolution
            .setTargetResolution(mPreviewSize)
            .setMaxResolution(mPreviewSize)
            // Set initial target rotation, we will have to call this again if rotation changes
            // during the lifecycle of this use case
            .setTargetRotation(Surface.ROTATION_90)
            .build()
            // The analyzer can then be assigned to the instance
            .also {
                it.setAnalyzer(cameraExecutor, LuminosityAnalyzer { luma ->
                    // Values returned from our analyzer are passed to the attached listener
                    // We log image analysis results here - you should do something useful
                    // instead!
                    //cameraLog("Average luminosity: $luma")
                })
            }

        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()

        if (mCameraDevice != null) {
            // Must remove observers from the previous camera instance
            removeCameraStateObservers(mCameraDevice!!.cameraInfo)
        }

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            mCameraDevice = cameraProvider.bindToLifecycle(
                mBaseFragment!!.viewLifecycleOwner,
                cameraSelector,
                preview,
                imageAnalyzer
            )

            Log.e("feng", " sfef  : " + imageAnalyzer!!.attachedSurfaceResolution)

            // Attach the viewfinder's surface provider to preview use case
            preview?.setSurfaceProvider(object : SurfaceProvider {
                override fun onSurfaceRequested(request: SurfaceRequest) {

                    val characteristics: CameraCharacteristics = cameraManager!!.getCameraCharacteristics(
                        mCameraId!!
                    )
                    val map: StreamConfigurationMap? =
                        characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    var mPreviewSize: Size = Util.getBestSize2(
                        map!!.getOutputSizes(SurfaceTexture::class.java).toList(),
                        mScreemWidth,
                        mScreemHeight
                    )

                    mSurfaceTexture!!.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight())
                    val surface = Surface(mSurfaceTexture)

                    request.provideSurface(surface, cameraExecutor) {
                        surface.release()
                        mSurfaceTexture?.release()
                    }
                }
            })

            observeCameraState(mCameraDevice?.cameraInfo!!)

            onCameraOpenListener.onOpen()
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
            mCameraDevice = null
            onCameraOpenListener.onFail()
        }
    }

    private fun removeCameraStateObservers(cameraInfo: CameraInfo) {
        cameraInfo.cameraState.removeObservers(mBaseFragment!!.viewLifecycleOwner)
    }

    private fun observeCameraState(cameraInfo: CameraInfo) {
        cameraInfo.cameraState.observe(mBaseFragment!!.viewLifecycleOwner) { cameraState ->
            run {
                when (cameraState.type) {
                    CameraState.Type.PENDING_OPEN -> {
                        // Ask the user to close other camera apps
                        cameraLog(
                            "CameraState: Pending Open"
                        )
                    }
                    CameraState.Type.OPENING -> {
                        // Show the Camera UI
                        cameraLog(
                            "CameraState: Opening"
                        )
                    }
                    CameraState.Type.OPEN -> {
                        // Setup Camera resources and begin processing
                        cameraLog(
                            "CameraState: Open"
                        )
                    }
                    CameraState.Type.CLOSING -> {
                        // Close camera UI
                        cameraLog(
                            "CameraState: Closing"
                        )
                    }
                    CameraState.Type.CLOSED -> {
                        // Free camera resources
                        cameraLog(
                            "CameraState: Closed"
                        )
                    }
                }
            }

            cameraState.error?.let { error ->
                when (error.code) {
                    // Open errors
                    CameraState.ERROR_STREAM_CONFIG -> {
                        // Make sure to setup the use cases properly
                        cameraLog(
                            "Stream config error"
                        )
                    }
                    // Opening errors
                    CameraState.ERROR_CAMERA_IN_USE -> {
                        // Close the camera or ask user to close another camera app that's using the
                        // camera
                        cameraLog(
                            "Camera in use"
                        )
                    }
                    CameraState.ERROR_MAX_CAMERAS_IN_USE -> {
                        // Close another open camera in the app, or ask the user to close another
                        // camera app that's using the camera
                        cameraLog(
                            "Max cameras in use"
                        )
                    }
                    CameraState.ERROR_OTHER_RECOVERABLE_ERROR -> {
                        cameraLog(
                            "Other recoverable error"
                        )
                    }
                    // Closing errors
                    CameraState.ERROR_CAMERA_DISABLED -> {
                        // Ask the user to enable the device's cameras
                        cameraLog(
                            "Camera disabled"
                        )
                    }
                    CameraState.ERROR_CAMERA_FATAL_ERROR -> {
                        // Ask the user to reboot the device to restore camera function
                        cameraLog(
                            "Fatal error"
                        )
                    }
                    // Closed errors
                    CameraState.ERROR_DO_NOT_DISTURB_MODE_ENABLED -> {
                        // Ask the user to disable the "Do Not Disturb" mode, then reopen the camera
                        cameraLog(
                            "Do not disturb mode enabled"
                        )
                    }
                }
            }
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

    override fun startPreview() {
        if (!isCameraOpen()) {
            return
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
        // Shut down our background executor
        cameraExecutor.shutdown()
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

    /**
     * Our custom image analysis class.
     *
     * <p>All we need to do is override the function `analyze` with our desired operations. Here,
     * we compute the average luminosity of the image by looking at the Y plane of the YUV frame.
     */
    private class LuminosityAnalyzer(listener: LumaListener? = null) : ImageAnalysis.Analyzer {
        private val frameRateWindow = 8
        private val frameTimestamps = ArrayDeque<Long>(5)
        private val listeners = ArrayList<LumaListener>().apply { listener?.let { add(it) } }
        private var lastAnalyzedTimestamp = 0L
        var framesPerSecond: Double = -1.0
            private set

        /**
         * Used to add listeners that will be called with each luma computed
         */
        fun onFrameAnalyzed(listener: LumaListener) = listeners.add(listener)

        /**
         * Helper extension function used to extract a byte array from an image plane buffer
         */
        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()    // Rewind the buffer to zero
            val data = ByteArray(remaining())
            get(data)   // Copy the buffer into a byte array
            return data // Return the byte array
        }

        /**
         * Analyzes an image to produce a result.
         *
         * <p>The caller is responsible for ensuring this analysis method can be executed quickly
         * enough to prevent stalls in the image acquisition pipeline. Otherwise, newly available
         * images will not be acquired and analyzed.
         *
         * <p>The image passed to this method becomes invalid after this method returns. The caller
         * should not store external references to this image, as these references will become
         * invalid.
         *
         * @param image image being analyzed VERY IMPORTANT: Analyzer method implementation must
         * call image.close() on received images when finished using them. Otherwise, new images
         * may not be received or the camera may stall, depending on back pressure setting.
         *
         */
        override fun analyze(image: ImageProxy) {
            // If there are no listeners attached, we don't need to perform analysis
            if (listeners.isEmpty()) {
                image.close()
                return
            }

            // Keep track of frames analyzed
            val currentTime = System.currentTimeMillis()
            frameTimestamps.push(currentTime)

            // Compute the FPS using a moving average
            while (frameTimestamps.size >= frameRateWindow) frameTimestamps.removeLast()
            val timestampFirst = frameTimestamps.peekFirst() ?: currentTime
            val timestampLast = frameTimestamps.peekLast() ?: currentTime
            framesPerSecond = 1.0 / ((timestampFirst - timestampLast) /
                    frameTimestamps.size.coerceAtLeast(1).toDouble()) * 1000.0

            // Analysis could take an arbitrarily long amount of time
            // Since we are running in a different thread, it won't stall other use cases

            lastAnalyzedTimestamp = frameTimestamps.first

            // Since format in ImageAnalysis is YUV, image.planes[0] contains the luminance plane
            val buffer = image.planes[0].buffer

            // Extract image data from callback object
            val data = buffer.toByteArray()

            // Convert the data into an array of pixel values ranging 0-255
            val pixels = data.map { it.toInt() and 0xFF }

            // Compute average luminance for the image
            val luma = pixels.average()

            // Call all listeners with new value
            listeners.forEach { it(luma) }

            image.close()
        }
    }

    companion object {
        private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val PHOTO_EXTENSION = ".jpg"
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0

        /** Helper function used to create a timestamped file */
        private fun createFile(baseFolder: File, format: String, extension: String) =
            File(
                baseFolder, SimpleDateFormat(format, Locale.US)
                    .format(System.currentTimeMillis()) + extension
            )
    }
}
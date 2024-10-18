package com.feng.fatecamera.ui

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.feng.fatecamera.R
import com.feng.fatecamera.constant.CameraVersion
import com.feng.fatecamera.databinding.ActivityMainBinding
import com.feng.fatecamera.ui.camera.CameraFragment
import com.feng.fatecamera.ui.login.LoginFragment
import com.feng.fatecamera.ui.main.MainFragment
import com.feng.libcamera.base.BaseActivity
import com.feng.libcamera.base.BaseFragment
import com.feng.libcamera.extension.appContext
import com.feng.libcamera.utils.PermisstionUtil
import java.io.File
import kotlin.concurrent.thread

private const val IMMERSIVE_FLAG_TIMEOUT = 500L

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private val loginFragment: LoginFragment = LoginFragment.newInstance()
    private val mainFragment: MainFragment = MainFragment.newInstance()
    private val cameraFragment: CameraFragment = CameraFragment.newInstance()

    override fun init() {
        val dm = DisplayMetrics()
        windowManager?.defaultDisplay?.getMetrics(dm)

        var w = dm.widthPixels
        var h = dm.heightPixels

        if (w > h) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (requestPermission()) {
            goLoginFragment()
        }
    }

    override fun rootLayoutId(): Int {
        return R.id.container
    }

    override fun fragment(): BaseFragment {
        return mainFragment
    }

    override fun onResume() {
        super.onResume()
        // Before setting full screen flags, we must wait a bit to let UI settle; otherwise, we may
        // be trying to set app to immersive mode before it's ready and the flags do not stick
        binding.root.postDelayed({
            hideSystemUI()
        }, IMMERSIVE_FLAG_TIMEOUT)
    }

    fun goCameraFragment(version: CameraVersion) {
        Log.d(TAG, " openCamera with : " + version)
        cameraFragment.initVersion(version)
        replaceFragment(cameraFragment)
    }

    fun goLoginFragment() {
        replaceFragment(loginFragment)
    }

    private fun requestPermission(): Boolean {
        return PermisstionUtil.checkPermissionsAndRequest(
            this,
            PermisstionUtil.CAMERA.plus(PermisstionUtil.STORAGE).plus(PermisstionUtil.MICROPHONE),
            111,
            "请求相机权限被拒绝"
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (!hasAllPermissionsGranted(grantResults)) {
            notify(" 需要权限啊，小胸弟！！！ ")
        } else {
            goLoginFragment()
        }
    }

    fun hasAllPermissionsGranted(grantResults: IntArray): Boolean {
        for (grantResult in grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false
            }
        }
        return true
    }

    override fun onBackPressed() {
        if (supportFragmentManager.findFragmentById(R.id.container) is CameraFragment) {
            goLoginFragment()
            return
        }
        super.onBackPressed()
    }

    companion object {

        /** Use external media if it is available, our app's file directory otherwise */
        fun getOutputDirectory(context: Context): File {
            val appContext = context.applicationContext
            val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
                File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() }
            }
            return if (mediaDir != null && mediaDir.exists())
                mediaDir else appContext.filesDir
        }
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.container).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
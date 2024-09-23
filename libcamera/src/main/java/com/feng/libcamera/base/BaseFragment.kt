package com.feng.libcamera.base

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.feng.libcamera.extension.appContext

abstract class BaseFragment : Fragment() {

    val TAG = javaClass.simpleName

    abstract fun config()
    abstract fun layoutView(): View
    abstract fun init()

    var isNeedReinit = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            config()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var result = layoutView()
        init()
        return result
    }

    override fun onDestroyView() {
        super.onDestroyView()
        deInit()
    }

    open fun notify(@StringRes message: Int) =
        Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show()

    open fun notify(message: String) {
        Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show()
        cameraLog(message)
    }

    open fun cameraLog(out: String) {
        Log.d(TAG, out)
    }

    open fun deInit() {
        cameraLog("deinit")
    }

}
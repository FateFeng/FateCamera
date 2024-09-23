package com.feng.libcamera.base

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.feng.libcamera.extension.inTransaction

abstract class BaseActivity : AppCompatActivity() {

    val TAG = javaClass.simpleName

    abstract fun init()
    abstract fun rootLayoutId(): Int
    abstract fun fragment(): BaseFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }


    /*internal fun addFragment(savedInstanceState: Bundle?) =
        savedInstanceState ?: supportFragmentManager.inTransaction {
            add(
                initView(),
                fragment()
            )
        }*/

    open fun replaceFragment(fragment: BaseFragment) =
        supportFragmentManager.inTransaction {
            replace(
                rootLayoutId(),
                fragment
            )
        }

    open fun notify(@StringRes message: Int) =
        Toast.makeText(baseContext, message, Toast.LENGTH_SHORT).show()

    open fun notify(message: String) {
        Toast.makeText(baseContext, message, Toast.LENGTH_SHORT).show()
        cameraLog(message)
    }


    open fun cameraLog(out: String) {
        Log.d(TAG, out)
    }
}
package com.feng.fatecamera.ui.login

import androidx.lifecycle.ViewModel
import com.feng.fatecamera.constant.CameraVersion
import com.feng.fatecamera.ui.MainActivity

class LoginViewModel : ViewModel() {

    fun openCamera(activity: MainActivity, version: CameraVersion) {
        activity.goCameraFragment(version)
    }

    override fun onCleared() {
        super.onCleared()
    }

}
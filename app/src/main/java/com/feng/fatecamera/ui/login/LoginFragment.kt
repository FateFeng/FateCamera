package com.feng.fatecamera.ui.login

import android.content.Context
import android.hardware.usb.UsbManager
import android.view.View
import androidx.annotation.MainThread
import androidx.lifecycle.ViewModelProvider
import com.feng.fatecamera.constant.CameraVersion
import com.feng.fatecamera.databinding.FragmentLoginBinding
import com.feng.fatecamera.ui.MainActivity
import com.feng.libcamera.base.BaseFragment
import com.feng.libcamera.utils.Util


class LoginFragment : BaseFragment() {

    companion object {
        fun newInstance() = LoginFragment()
    }

    private lateinit var viewModel: LoginViewModel
    private lateinit var binding: FragmentLoginBinding

    override fun config() {
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
    }

    override fun layoutView(): View {
        binding = FragmentLoginBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.root.postDelayed({
            binding.run {
                val normalView = if (Util.getHasNormalCamera()) View.VISIBLE else View.GONE
                camera1Btn.visibility = normalView
                camera2Btn.visibility = normalView
                cameraXBtn.visibility = normalView
                cameraNDKBtn.visibility = normalView

                val usbManager = requireContext()!!.getSystemService(Context.USB_SERVICE) as UsbManager
                val usbView =
                    if (Util.getHasUsbCamera(usbManager))
                        View.VISIBLE else View.GONE
                cameraUVCBtn.visibility = usbView

                titleTv.text = "动态显示UI界面\n支持camera1/2/x/ndk/uvc"
                noticeTv.text =
                    "设备是否有常规摄像头 ： ${Util.getHasNormalCamera()}  ； 是否有USB摄像头 ： ${Util.getHasUsbCamera(usbManager)}"
            }

        },200)
    }

    override fun init() {
        binding.apply {
            camera1Btn.setOnClickListener {
                viewModel.openCamera(
                    activity as MainActivity,
                    CameraVersion.Version1
                )
            }
            camera2Btn.setOnClickListener {
                viewModel.openCamera(
                    activity as MainActivity,
                    CameraVersion.Version2
                )
            }
            cameraXBtn.setOnClickListener {
                viewModel.openCamera(
                    activity as MainActivity,
                    CameraVersion.VersionX
                )
            }
            cameraNDKBtn.setOnClickListener {
                viewModel.openCamera(
                    activity as MainActivity,
                    CameraVersion.VersionNDK
                )
            }
            cameraUVCBtn.setOnClickListener {
                viewModel.openCamera(
                    activity as MainActivity,
                    CameraVersion.VersionUVC
                )
            }
        }
    }

    override fun deInit() {

    }

}
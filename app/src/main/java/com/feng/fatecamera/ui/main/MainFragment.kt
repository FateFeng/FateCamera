package com.feng.fatecamera.ui.main

import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.feng.fatecamera.databinding.FragmentMainBinding
import com.feng.libcamera.base.BaseFragment

class MainFragment : BaseFragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: FragmentMainBinding

    override fun config() {
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
    }

    override fun layoutView(): View {
        binding = FragmentMainBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun init() {
        cameraLog("init")
    }

    override fun deInit() {

    }

}
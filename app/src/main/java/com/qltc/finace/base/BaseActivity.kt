package com.qltc.finace.base

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.navigation.NavController

abstract class BaseActivity<ViewBinding : ViewDataBinding, ViewModel : BaseViewModel> : AppCompatActivity(){
    protected abstract val viewModel: ViewModel

    protected lateinit var viewBinding: ViewBinding


    @get:LayoutRes
    protected abstract val layoutId: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!::viewBinding.isInitialized) {
            viewBinding = DataBindingUtil.setContentView(this, layoutId)
            viewBinding.lifecycleOwner = this
        }
    }
}
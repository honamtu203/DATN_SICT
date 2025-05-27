package com.qltc.finace.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding


abstract class BaseFragment<ViewBinding : ViewDataBinding, viewModel : BaseViewModel> : Fragment() {

    protected var _binding : ViewBinding? = null
    protected val viewBinding get() = _binding!!
    protected abstract val viewModel: viewModel
    protected abstract val layoutID : Int
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(inflater,layoutID,container,false)
        _binding?.lifecycleOwner = this.viewLifecycleOwner
        return viewBinding.root
    }
    protected fun <T : BaseActivity<*, *>> getOwnerActivity() = activity as? T
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
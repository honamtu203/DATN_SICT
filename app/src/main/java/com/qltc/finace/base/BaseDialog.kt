package com.qltc.finace.base

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager

abstract class BaseDialog<ViewBinding : ViewDataBinding> : DialogFragment() {
    private var isShowing = false
    protected lateinit var viewBinding: ViewBinding

    @get:LayoutRes
    protected abstract val layoutId: Int

    open var canceledOnTouchOutside: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        viewBinding.apply {
            root.isClickable = true
            lifecycleOwner = viewLifecycleOwner
            executePendingBindings()
        }
        if (savedInstanceState != null) {
            if (isAdded) {
                dismiss()
            }
        }
        return viewBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.decorView?.background = ColorDrawable(Color.TRANSPARENT)
        dialog?.setCancelable(canceledOnTouchOutside)
    }

    override fun show(manager: FragmentManager, tag: String?) {
        if (isShowing) return
        isShowing = true
        super.show(manager, tag)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        isShowing = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isShowing = false
    }

    open fun showDialog(
        fragmentManager: FragmentManager?,
        tag: String?
    ) {
        fragmentManager?.let {
            show(fragmentManager, tag)
        }
    }
}
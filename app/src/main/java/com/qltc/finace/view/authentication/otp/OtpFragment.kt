package com.qltc.finace.view.authentication.otp

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.qltc.finace.R
import com.qltc.finace.base.BaseFragment
import com.qltc.finace.databinding.FragmentOtpBinding

class OtpFragment : BaseFragment<FragmentOtpBinding, OtpViewModel>(), OtpListener {
    override val viewModel: OtpViewModel by viewModels()
    override val layoutID: Int = R.layout.fragment_otp

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.apply {
            listener = this@OtpFragment

        }

    }

    fun openHome() {

    }

    override fun senOtp() {
        TODO("Not yet implemented")
    }

    override fun backLoginPhone() {
        findNavController().popBackStack()
    }
}


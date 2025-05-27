package com.qltc.finace.view.authentication.login_phone

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.qltc.finace.base.Constant
import com.qltc.finace.R
import com.qltc.finace.base.BaseFragment
import com.qltc.finace.databinding.FragmentLoginPhoneBinding
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

class LoginPhoneFragment() : BaseFragment<FragmentLoginPhoneBinding, LoginPhoneViewModel>(),
    LoginPhoneListener {
    override val viewModel: LoginPhoneViewModel by viewModels()
    override val layoutID: Int = R.layout.fragment_login_phone
    lateinit var mPhone :String
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.apply {
            listener = this@LoginPhoneFragment

        }
    }

    override fun loginToHome() {
        mPhone = viewBinding.phone.text.toString().trim()
        val regex = Regex("[0-9]{9,12}")
        if(regex.matches(mPhone)) {
            mPhone = "+84${mPhone.substring(1)}"
            viewBinding.loading.setText(R.string.loading)
            val options = PhoneAuthOptions.newBuilder(viewModel.firebaseAuth)
                .setPhoneNumber(mPhone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this.requireActivity())
                .setCallbacks(callbacks)
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        }
        else {

        }
    }

    override fun backSignIn() {
        findNavController().popBackStack()
    }

    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        //nếu veri thành công thì vào home
        override fun onVerificationCompleted(p0: PhoneAuthCredential) {}
        // không thành công thì ..
        override fun onVerificationFailed(p0: FirebaseException) {
            if (p0 is FirebaseAuthInvalidCredentialsException) {
                Toast.makeText(requireContext(), "xử lý không thành công", Toast.LENGTH_SHORT).show()
                // Invalid request
            } else if (p0 is FirebaseTooManyRequestsException) {
                Toast.makeText(requireContext(), "SMS quá nhiều, Vui lòng thử lại sau", Toast.LENGTH_SHORT).show()
                // The SMS quota for the project has been exceeded
            } else if (p0 is FirebaseAuthMissingActivityForRecaptchaException) {
                Toast.makeText(requireContext(), "reCAPCHA không thành công, vui lòng thử lại", Toast.LENGTH_SHORT).show()
                // reCAPTCHA verification attempted with null Activity
            }
        }
        // nhận đc 1 otp
        override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
            super.onCodeSent(p0, p1)
            mPhone = viewBinding.phone.text.toString()
            findNavController().navigate(
                R.id.login_phone_to_otp,
                bundleOf(Constant.PHONE to mPhone, Constant.OTP to p0
                )
            )
        }
    }


}
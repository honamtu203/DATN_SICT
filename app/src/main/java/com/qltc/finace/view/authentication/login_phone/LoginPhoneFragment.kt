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
import com.google.firebase.auth.*
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class LoginPhoneFragment() : BaseFragment<FragmentLoginPhoneBinding, LoginPhoneViewModel>(),
    LoginPhoneListener {
    override val viewModel: LoginPhoneViewModel by viewModels()
    override val layoutID: Int = R.layout.fragment_login_phone
    private lateinit var mPhone: String
    private var verificationId: String? = null
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.apply {
            listener = this@LoginPhoneFragment
        }
    }

    override fun loginToHome() {
        val phoneInput = viewBinding.phone.text.toString().trim()
        
        if (!isValidPhoneNumber(phoneInput)) {
            viewBinding.loading.text = "Số điện thoại không hợp lệ"
            viewBinding.loading.setTextColor(resources.getColor(R.color.red_d61c1c, null))
            return
        }
        
        // Format phone number for Vietnam
        mPhone = formatPhoneNumber(phoneInput)
        
        // Show loading
        viewBinding.loading.text = "Đang gửi OTP..."
        viewBinding.loading.setTextColor(resources.getColor(R.color.orange, null))
        viewBinding.login.isEnabled = false
        
        val options = PhoneAuthOptions.newBuilder(viewModel.firebaseAuth)
            .setPhoneNumber(mPhone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this.requireActivity())
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }
    
    private fun isValidPhoneNumber(phone: String): Boolean {
        // Vietnamese phone number validation
        val cleanPhone = phone.replace(Regex("[^0-9]"), "")
        return when {
            cleanPhone.startsWith("84") && cleanPhone.length == 11 -> true
            cleanPhone.startsWith("0") && cleanPhone.length == 10 -> true
            cleanPhone.length == 9 -> true
            else -> false
        }
    }
    
    private fun formatPhoneNumber(phone: String): String {
        val cleanPhone = phone.replace(Regex("[^0-9]"), "")
        return when {
            cleanPhone.startsWith("84") -> "+$cleanPhone"
            cleanPhone.startsWith("0") -> "+84${cleanPhone.substring(1)}"
            cleanPhone.length == 9 -> "+84$cleanPhone"
            else -> "+84$cleanPhone"
        }
    }

    override fun backSignIn() {
        findNavController().popBackStack()
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // Auto-verification completed
            viewBinding.loading.text = "Xác thực tự động thành công"
            viewBinding.loading.setTextColor(resources.getColor(R.color.green_2D9849, null))
            
            // Sign in with credential
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(exception: FirebaseException) {
            viewBinding.login.isEnabled = true
            
            val errorMessage = when (exception) {
                is FirebaseAuthInvalidCredentialsException -> "Số điện thoại không hợp lệ"
                else -> {
                    when {
                        exception.message?.contains("too-many-requests") == true ||
                        exception.message?.contains("TOO_MANY_REQUESTS") == true -> 
                            "Quá nhiều yêu cầu. Vui lòng thử lại sau"
                        exception.message?.contains("CAPTCHA") == true ||
                        exception.message?.contains("reCAPTCHA") == true -> 
                            "Xác thực reCAPTCHA thất bại. Vui lòng thử lại"
                        else -> "Gửi OTP thất bại: ${exception.message}"
                    }
                }
            }
            
            viewBinding.loading.text = errorMessage
            viewBinding.loading.setTextColor(resources.getColor(R.color.red_d61c1c, null))
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            super.onCodeSent(verificationId, token)
            
            this@LoginPhoneFragment.verificationId = verificationId
            viewBinding.loading.text = "OTP đã được gửi"
            viewBinding.loading.setTextColor(resources.getColor(R.color.green_2D9849, null))
            
            // Navigate to OTP fragment
            findNavController().navigate(
                R.id.login_phone_to_otp,
                bundleOf(
                    Constant.PHONE to mPhone,
                    Constant.OTP to verificationId
                )
            )
            
            viewBinding.login.isEnabled = true
        }
    }
    
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        viewModel.firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    Toast.makeText(requireContext(), "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                    // Navigate to home (you may need to implement this navigation)
                    // findNavController().navigate(R.id.action_to_home)
                } else {
                    // Sign in failed
                    val errorMessage = task.exception?.message ?: "Đăng nhập thất bại"
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                    viewBinding.loading.text = errorMessage
                    viewBinding.loading.setTextColor(resources.getColor(R.color.red_d61c1c, null))
                }
            }
    }
}
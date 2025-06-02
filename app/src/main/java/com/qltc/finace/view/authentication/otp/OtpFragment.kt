package com.qltc.finace.view.authentication.otp

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.qltc.finace.base.Constant
import com.qltc.finace.R
import com.qltc.finace.base.BaseFragment
import com.qltc.finace.databinding.FragmentOtpBinding
import com.qltc.finace.view.activity.authen.AuthenticationActivity
import com.qltc.finace.view.activity.home.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

class OtpFragment : BaseFragment<FragmentOtpBinding, OtpViewModel>(), OtpListener {
    override val viewModel: OtpViewModel by viewModels()
    override val layoutID: Int = R.layout.fragment_otp

    private var verificationId: String? = null
    private var phoneNumber: String? = null
    private var resendTimer: CountDownTimer? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Get data from arguments
        arguments?.let { bundle ->
            phoneNumber = bundle.getString(Constant.PHONE)
            verificationId = bundle.getString(Constant.OTP)
        }

        viewBinding.apply {
            listener = this@OtpFragment
            // Display phone number
            phone.text = phoneNumber ?: ""
        }
        
        setupOtpInput()
        setupResendButton()
        startResendTimer()
    }

    private fun setupOtpInput() {
        viewBinding.otp.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // Enable confirm button when OTP is 6 digits
                val isValidOtp = s?.length == 6
                viewBinding.login.isEnabled = isValidOtp
                
                if (isValidOtp) {
                    viewBinding.login.setBackgroundResource(R.drawable.bg_submit)
                } else {
                    viewBinding.login.setBackgroundColor(resources.getColor(R.color.color_AFB0B6, null))
                }
            }
        })
    }

    private fun setupResendButton() {
        viewBinding.textView23.setOnClickListener {
            if (viewBinding.textView23.isEnabled) {
                resendOtp()
            }
        }
    }

    private fun startResendTimer() {
        viewBinding.textView23.isEnabled = false
        
        resendTimer?.cancel()
        resendTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                viewBinding.textView23.text = "Gửi lại ($seconds)s"
            }
            
            override fun onFinish() {
                viewBinding.textView23.text = "Gửi lại"
                viewBinding.textView23.isEnabled = true
                viewBinding.textView23.setTextColor(resources.getColor(R.color.red, null))
            }
        }.start()
    }

    override fun senOtp() {
        val otpCode = viewBinding.otp.text.toString().trim()
        
        if (otpCode.length != 6) {
            Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ mã OTP", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (verificationId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Lỗi xác thực. Vui lòng thử lại", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Show loading
        viewBinding.login.isEnabled = false
        viewBinding.login.text = "Đang xác thực..."
        
        val credential = PhoneAuthProvider.getCredential(verificationId!!, otpCode)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        viewModel.firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    Toast.makeText(requireContext(), "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                    
                    // Check if this is a new user
                    val isNewUser = task.result.additionalUserInfo?.isNewUser ?: false
                    
                    // Navigate to home
                    navigateToHome(isNewUser)
                } else {
                    // Sign in failed
                    val errorMessage = when {
                        task.exception?.message?.contains("invalid-verification-code") == true -> 
                            "Mã OTP không đúng"
                        task.exception?.message?.contains("session-expired") == true -> 
                            "Mã OTP đã hết hạn"
                        else -> "Xác thực thất bại: ${task.exception?.message}"
                    }
                    
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                    
                    // Reset UI
                    viewBinding.login.isEnabled = true
                    viewBinding.login.text = "Xác nhận"
                    viewBinding.otp.text?.clear()
                }
            }
    }

    private fun navigateToHome(isNewUser: Boolean) {
        // You may want to insert default categories for new users here
        // viewModel.insertDefaultCategory(isNewUser) { ... }
        
        val intent = Intent(requireActivity(), HomeActivity::class.java)
        startActivity(intent)
        getOwnerActivity<AuthenticationActivity>()?.finish()
    }

    override fun backLoginPhone() {
        resendTimer?.cancel()
        findNavController().popBackStack()
    }
    
    override fun resendOtp() {
        if (!phoneNumber.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Đang gửi lại mã OTP...", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            // Navigate back to phone input to resend
        } else {
            Toast.makeText(requireContext(), "Có lỗi xảy ra. Vui lòng thử lại", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        resendTimer?.cancel()
    }
}


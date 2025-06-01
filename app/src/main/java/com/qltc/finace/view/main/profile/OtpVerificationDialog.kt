package com.qltc.finace.view.main.profile

import android.app.Dialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Window
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.qltc.finace.R
import com.qltc.finace.databinding.DialogOtpVerificationBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.TimeUnit

class OtpVerificationDialog(
    private val context: Context,
    private val phoneNumber: String,
    private val onOtpConfirmed: (PhoneAuthCredential) -> Unit,
    private val onOtpCancelled: () -> Unit
) {
    
    private lateinit var binding: DialogOtpVerificationBinding
    private lateinit var dialog: Dialog
    
    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    
    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // Auto-retrieval or instant verification
            onOtpConfirmed(credential)
            dialog.dismiss()
        }
        
        override fun onVerificationFailed(e: FirebaseException) {
            Toast.makeText(context, "Xác thực thất bại: ${e.message}", Toast.LENGTH_LONG).show()
            updateConfirmButtonState(false)
        }
        
        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            this@OtpVerificationDialog.verificationId = verificationId
            this@OtpVerificationDialog.resendToken = token
            
            Toast.makeText(context, "Mã OTP đã được gửi về $phoneNumber", Toast.LENGTH_SHORT).show()
            
            // Kiểm tra lại trạng thái button sau khi có verificationId
            val currentOtpText = binding.etOtpCode.text?.toString() ?: ""
            val isValid = currentOtpText.length == 6 && verificationId.isNotEmpty()
            updateConfirmButtonState(isValid)
        }
    }
    
    fun show() {
        binding = DialogOtpVerificationBinding.inflate(LayoutInflater.from(context))
        
        dialog = Dialog(context).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(binding.root)
            setCancelable(false)
        }
        
        setupUI()
        startPhoneNumberVerification()
        
        dialog.show()
    }
    
    private fun setupUI() {
        // Update message with phone number
        binding.tvOtpMessage.text = "Mã OTP đã được gửi về số điện thoại $phoneNumber. Vui lòng nhập mã để xác thực."
        
        // OTP input text watcher
        binding.etOtpCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val isValid = s?.length == 6 && verificationId != null
                updateConfirmButtonState(isValid)
            }
        })
        
        // Confirm button
        binding.btnConfirmOtp.setOnClickListener {
            val otpCode = binding.etOtpCode.text.toString().trim()
            if (otpCode.length == 6 && verificationId != null) {
                verifyOtpCode(otpCode)
            } else {
                Toast.makeText(context, "Vui lòng nhập đầy đủ mã OTP", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Cancel button
        binding.btnCancelOtp.setOnClickListener {
            onOtpCancelled()
            dialog.dismiss()
        }
        
        // Resend OTP
        binding.tvResendOtp.setOnClickListener {
            resendVerificationCode()
        }
        
        // Set initial state
        updateConfirmButtonState(false)
    }
    
    private fun updateConfirmButtonState(enabled: Boolean) {
        binding.btnConfirmOtp.isEnabled = enabled
        // Update background color programmatically for better visual feedback
        val backgroundTint = if (enabled) {
            ContextCompat.getColor(context, R.color.orange)
        } else {
            ContextCompat.getColor(context, R.color.color_AFB0B6)
        }
        binding.btnConfirmOtp.setBackgroundTintList(
            ContextCompat.getColorStateList(context, 
                if (enabled) R.color.orange else R.color.color_AFB0B6
            )
        )
    }
    
    private fun startPhoneNumberVerification() {
        val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(context as androidx.fragment.app.FragmentActivity)
            .setCallbacks(callbacks)
            .build()
        
        PhoneAuthProvider.verifyPhoneNumber(options)
    }
    
    private fun resendVerificationCode() {
        resendToken?.let { token ->
            val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(context as androidx.fragment.app.FragmentActivity)
                .setCallbacks(callbacks)
                .setForceResendingToken(token)
                .build()
            
            PhoneAuthProvider.verifyPhoneNumber(options)
            Toast.makeText(context, "Đang gửi lại mã OTP...", Toast.LENGTH_SHORT).show()
        } ?: run {
            Toast.makeText(context, "Không thể gửi lại mã OTP lúc này", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun verifyOtpCode(otpCode: String) {
        verificationId?.let { id ->
            val credential = PhoneAuthProvider.getCredential(id, otpCode)
            onOtpConfirmed(credential)
            dialog.dismiss()
        }
    }
} 
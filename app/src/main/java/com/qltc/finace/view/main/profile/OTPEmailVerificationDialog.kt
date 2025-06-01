package com.qltc.finace.view.main.profile

import android.app.Dialog
import android.content.Context
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.qltc.finace.R
import com.qltc.finace.databinding.DialogOtpEmailVerificationBinding
import com.qltc.finace.utils.NetworkUtils
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class OTPEmailVerificationDialog(
    private val context: Context,
    private val newEmail: String,
    private val phoneNumber: String,
    private val onEmailConfirmed: (String) -> Unit,
    private val onEmailCancelled: () -> Unit
) {
    
    private lateinit var binding: DialogOtpEmailVerificationBinding
    private lateinit var dialog: Dialog
    private var verificationJob: Job? = null
    private var resendTimer: CountDownTimer? = null
    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    
    companion object {
        private const val OTP_TIMEOUT = 60L // seconds
        private const val OTP_LENGTH = 6
    }
    
    fun show() {
        binding = DialogOtpEmailVerificationBinding.inflate(LayoutInflater.from(context))
        
        dialog = Dialog(context).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(binding.root)
            setCancelable(false)
        }
        
        setupUI()
        sendOTP()
        
        dialog.show()
    }
    
    private fun setupUI() {
        // Check network connectivity first
        if (!NetworkUtils.isNetworkAvailable(context)) {
            Toast.makeText(context, "Không có kết nối internet. Vui lòng kiểm tra và thử lại.", Toast.LENGTH_LONG).show()
            onEmailCancelled()
            return
        }
        
        // Validate email format
        if (!isValidEmail(newEmail)) {
            Toast.makeText(context, "Định dạng email không hợp lệ", Toast.LENGTH_SHORT).show()
            onEmailCancelled()
            return
        }
        
        // Display email and phone
        binding.etNewEmail.setText(newEmail)
        binding.etPhone.setText(phoneNumber)
        
        // OTP input text watcher
        binding.etOtp.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val isValid = s?.length == OTP_LENGTH
                updateVerifyButtonState(isValid)
            }
        })
        
        // Resend OTP button
        binding.tvResendOtp.setOnClickListener {
            resendOTP()
        }
        
        // Verify button
        binding.btnVerifyOtp.setOnClickListener {
            val otp = binding.etOtp.text.toString().trim()
            if (otp.length == OTP_LENGTH) {
                verifyOTPAndUpdateEmail(otp)
            } else {
                Toast.makeText(context, "Vui lòng nhập đầy đủ mã OTP", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Cancel button
        binding.btnCancelOtp.setOnClickListener {
            onEmailCancelled()
            dismiss()
        }
        
        // Set initial state
        updateVerifyButtonState(false)
        startResendTimer()
    }
    
    private fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    private fun sendOTP() {
        Log.d("OTPEmailVerification", "Sending OTP to: $phoneNumber")
        
        val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
            .setPhoneNumber(phoneNumber)
            .setTimeout(OTP_TIMEOUT, TimeUnit.SECONDS)
            .setActivity(context as androidx.fragment.app.FragmentActivity)
            .setCallbacks(phoneAuthCallbacks)
            .build()
        
        PhoneAuthProvider.verifyPhoneNumber(options)
    }
    
    private fun resendOTP() {
        if (resendToken == null) {
            Toast.makeText(context, "Chưa thể gửi lại OTP. Vui lòng thử lại sau.", Toast.LENGTH_SHORT).show()
            return
        }
        
        Log.d("OTPEmailVerification", "Resending OTP to: $phoneNumber")
        
        val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
            .setPhoneNumber(phoneNumber)
            .setTimeout(OTP_TIMEOUT, TimeUnit.SECONDS)
            .setActivity(context as androidx.fragment.app.FragmentActivity)
            .setCallbacks(phoneAuthCallbacks)
            .setForceResendingToken(resendToken!!)
            .build()
        
        PhoneAuthProvider.verifyPhoneNumber(options)
        startResendTimer()
    }
    
    private val phoneAuthCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            Log.d("OTPEmailVerification", "Verification completed automatically")
            // Auto-verification completed, extract OTP
            val smsCode = credential.smsCode
            if (!smsCode.isNullOrEmpty()) {
                binding.etOtp.setText(smsCode)
                verifyOTPAndUpdateEmail(smsCode)
            }
        }
        
        override fun onVerificationFailed(e: FirebaseException) {
            Log.e("OTPEmailVerification", "Verification failed", e)
            val errorMessage = when (e) {
                is FirebaseAuthInvalidCredentialsException -> "Số điện thoại không hợp lệ"
                else -> {
                    when {
                        e.message?.contains("too-many-requests") == true || 
                        e.message?.contains("TOO_MANY_REQUESTS") == true -> 
                            "Quá nhiều yêu cầu. Vui lòng thử lại sau"
                        else -> "Gửi OTP thất bại: ${e.message}"
                    }
                }
            }
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        }
        
        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            Log.d("OTPEmailVerification", "OTP sent successfully")
            this@OTPEmailVerificationDialog.verificationId = verificationId
            this@OTPEmailVerificationDialog.resendToken = token
            Toast.makeText(context, "Mã OTP đã được gửi tới $phoneNumber", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun verifyOTPAndUpdateEmail(otp: String) {
        val verificationId = this.verificationId
        if (verificationId == null) {
            Toast.makeText(context, "Lỗi xác thực. Vui lòng thử lại.", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Show loading state
        setLoadingState(true)
        
        // Cancel previous job if exists
        verificationJob?.cancel()
        
        verificationJob = CoroutineScope(Dispatchers.Main).launch {
            try {
                // Create credential from OTP
                val credential = PhoneAuthProvider.getCredential(verificationId, otp)
                
                // Re-authenticate user with phone credential
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser == null) {
                    throw Exception("Không tìm thấy thông tin người dùng")
                }
                
                // Re-authenticate with phone credential
                currentUser.reauthenticate(credential).await()
                Log.d("OTPEmailVerification", "Phone re-authentication successful")
                
                // Now update email
                currentUser.verifyBeforeUpdateEmail(newEmail).await()
                Log.d("OTPEmailVerification", "Email verification sent")
                
                // Success
                Toast.makeText(context, 
                    "Xác thực thành công! Email xác minh đã được gửi tới $newEmail. Vui lòng kiểm tra hộp thư để hoàn tất việc thay đổi.", 
                    Toast.LENGTH_LONG).show()
                
                onEmailConfirmed(newEmail)
                dismiss()
                
            } catch (e: Exception) {
                Log.e("OTPEmailVerification", "Error during OTP verification", e)
                handleVerificationError(e)
            }
        }
    }
    
    private fun handleVerificationError(exception: Exception) {
        setLoadingState(false)
        
        val errorMessage = when (exception) {
            is FirebaseAuthInvalidCredentialsException -> {
                "Mã OTP không đúng. Vui lòng thử lại."
            }
            is FirebaseAuthInvalidUserException -> {
                "Người dùng không tồn tại hoặc đã bị vô hiệu hóa."
            }
            else -> {
                when {
                    exception.message?.contains("email-already-in-use") == true -> 
                        "Email này đã được sử dụng bởi tài khoản khác."
                    exception.message?.contains("invalid-email") == true -> 
                        "Định dạng email không hợp lệ."
                    exception.message?.contains("operation-not-allowed") == true -> 
                        "Thao tác không được phép. Vui lòng liên hệ hỗ trợ."
                    exception.message?.contains("too-many-requests") == true -> 
                        "Quá nhiều yêu cầu. Vui lòng thử lại sau."
                    else -> 
                        "Có lỗi xảy ra: ${exception.message}"
                }
            }
        }
        
        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        Log.e("OTPEmailVerification", "Verification error: $errorMessage", exception)
    }
    
    private fun updateVerifyButtonState(enabled: Boolean) {
        binding.btnVerifyOtp.isEnabled = enabled
        binding.btnVerifyOtp.setBackgroundTintList(
            ContextCompat.getColorStateList(context, 
                if (enabled) R.color.orange else R.color.color_AFB0B6
            )
        )
    }
    
    private fun setLoadingState(loading: Boolean) {
        binding.btnVerifyOtp.isEnabled = !loading
        binding.etOtp.isEnabled = !loading
        binding.tvResendOtp.isEnabled = !loading
        binding.btnVerifyOtp.text = if (loading) "Đang xử lý..." else "Xác Nhận"
    }
    
    private fun startResendTimer() {
        binding.tvResendOtp.isEnabled = false
        
        resendTimer?.cancel()
        resendTimer = object : CountDownTimer(OTP_TIMEOUT * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                binding.tvResendOtp.text = "Gửi lại OTP (${seconds}s)"
            }
            
            override fun onFinish() {
                binding.tvResendOtp.text = "Gửi lại mã OTP"
                binding.tvResendOtp.isEnabled = true
            }
        }.start()
    }
    
    fun dismiss() {
        verificationJob?.cancel()
        resendTimer?.cancel()
        if (::dialog.isInitialized && dialog.isShowing) {
            dialog.dismiss()
        }
    }
} 
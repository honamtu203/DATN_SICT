package com.qltc.finace.view.main.profile

import android.app.Dialog
import android.content.Context
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
import com.qltc.finace.databinding.DialogEmailVerificationBinding
import com.qltc.finace.utils.NetworkUtils
import com.qltc.finace.utils.AuthProviderUtils
import com.google.firebase.FirebaseException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class EmailVerificationDialog(
    private val context: Context,
    private val newEmail: String,
    private val onEmailConfirmed: (String) -> Unit,
    private val onEmailCancelled: () -> Unit
) {
    
    private lateinit var binding: DialogEmailVerificationBinding
    private lateinit var dialog: Dialog
    private var verificationJob: Job? = null
    
    fun show() {
        binding = DialogEmailVerificationBinding.inflate(LayoutInflater.from(context))
        
        dialog = Dialog(context).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(binding.root)
            setCancelable(false)
        }
        
        setupUI()
        
        dialog.show()
    }
    
    private fun setupUI() {
        // Check network connectivity first
        if (!NetworkUtils.isNetworkAvailable(context)) {
            Toast.makeText(context, "Không có kết nối internet. Vui lòng kiểm tra và thử lại.", Toast.LENGTH_LONG).show()
            onEmailCancelled()
            return
        }
        
        // Validate email format first
        if (!isValidEmail(newEmail)) {
            Toast.makeText(context, "Định dạng email không hợp lệ", Toast.LENGTH_SHORT).show()
            onEmailCancelled()
            return
        }
        
        // Display new email
        binding.tvNewEmail.setText(newEmail)
        binding.tvNewEmail.isEnabled = false // Email field should be read-only
        
        // Check user provider and authentication method using AuthProviderUtils
        val currentUser = FirebaseAuth.getInstance().currentUser
        val providerType = AuthProviderUtils.getPrimaryAuthProvider(currentUser)
        val capabilities = AuthProviderUtils.getProviderCapabilities(providerType)
        
        Log.d("EmailVerification", "Provider type: $providerType")
        Log.d("EmailVerification", "Can change email: ${capabilities.canChangeEmail}")
        Log.d("EmailVerification", "Requires password: ${capabilities.requiresPasswordForEmailChange}")
        
        // Setup UI based on provider capabilities
        setupUIForProvider(providerType, capabilities)
    }
    
    private fun setupUIForProvider(
        providerType: AuthProviderUtils.AuthProviderType,
        capabilities: AuthProviderUtils.AuthProviderCapabilities
    ) {
        // Set appropriate message
        binding.tvEmailMessage.text = AuthProviderUtils.getEmailChangeDescription(providerType)
        
        if (!capabilities.canChangeEmail) {
            // User cannot change email (Google, Facebook, etc.)
            setupDisabledEmailChangeUI()
        } else if (capabilities.requiresPasswordForEmailChange) {
            // Email/Password user - requires password verification
            setupPasswordRequiredUI()
        } else {
            // Phone user - no password required
            setupNoPasswordRequiredUI()
        }
    }
    
    private fun setupDisabledEmailChangeUI() {
        binding.tilEmail.isEnabled = false
        binding.tilPassword.visibility = View.GONE
        binding.etPassword.isEnabled = false
        binding.btnConfirmEmail.text = "Đóng"
        binding.btnCancelEmail.text = "Hủy"
        
        binding.btnConfirmEmail.setOnClickListener {
            dialog.dismiss()
        }
        
        binding.btnCancelEmail.setOnClickListener {
            onEmailCancelled()
            dialog.dismiss()
        }
    }
    
    private fun setupPasswordRequiredUI() {
        // For email/password users, normal flow
        binding.tilEmail.helperText = "Email mới sẽ được cập nhật sau khi xác thực"
        
        // Ensure password field is visible for email/password users
        binding.tilPassword.visibility = View.VISIBLE
        binding.etPassword.isEnabled = true
        
        // Password input text watcher
        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val isValid = s?.isNotEmpty() == true
                updateConfirmButtonState(isValid)
            }
        })
        
        // Confirm button
        binding.btnConfirmEmail.setOnClickListener {
            val password = binding.etPassword.text.toString().trim()
            if (password.isNotEmpty()) {
                verifyEmailWithPassword(password)
            } else {
                Toast.makeText(context, "Vui lòng nhập mật khẩu", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Cancel button
        binding.btnCancelEmail.setOnClickListener {
            onEmailCancelled()
            dialog.dismiss()
        }
        
        // Set initial state
        updateConfirmButtonState(false)
    }
    
    private fun setupNoPasswordRequiredUI() {
        // For phone users, they can change email but don't need password
        binding.tilEmail.helperText = "Email mới sẽ được cập nhật sau khi xác thực"
        binding.tilEmail.isEnabled = false // Email field is read-only
        
        // Hide password field completely for phone users
        binding.tilPassword.visibility = View.GONE
        binding.etPassword.isEnabled = false
        
        // Update button text
        binding.btnConfirmEmail.text = "Xác Nhận Email"
        binding.btnConfirmEmail.isEnabled = true
        updateConfirmButtonState(true)
        
        binding.btnConfirmEmail.setOnClickListener {
            verifyEmailForPhoneUser()
        }
        
        binding.btnCancelEmail.setOnClickListener {
            onEmailCancelled()
            dialog.dismiss()
        }
    }
    
    private fun verifyEmailForPhoneUser() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        
        if (currentUser == null) {
            Toast.makeText(context, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Check network before starting verification
        if (!NetworkUtils.isNetworkAvailable(context)) {
            Toast.makeText(context, "Không có kết nối internet. Vui lòng kiểm tra và thử lại.", Toast.LENGTH_LONG).show()
            return
        }
        
        // Log network type for debugging
        val networkType = NetworkUtils.getNetworkTypeDescription(context)
        Log.d("EmailVerification", "Network type: $networkType")
        Log.d("EmailVerification", "Phone user email verification started")
        
        // Show loading state
        setLoadingState(true)
        
        // Cancel previous job if exists
        verificationJob?.cancel()
        
        // For phone users, we can directly use verifyBeforeUpdateEmail without re-authentication
        // because they're already authenticated via phone OTP
        verificationJob = CoroutineScope(Dispatchers.Main).launch {
            try {
                // Use verifyBeforeUpdateEmail directly for phone users
                currentUser.verifyBeforeUpdateEmail(newEmail).await()
                Log.d("EmailVerification", "Verification email sent for phone user")
                
                // Success - verification email sent
                Toast.makeText(context, 
                    "Email xác minh đã được gửi tới $newEmail. Vui lòng kiểm tra hộp thư và xác nhận để hoàn tất việc thay đổi.", 
                    Toast.LENGTH_LONG).show()
                
                // Note: Email is not immediately updated until user verifies
                onEmailConfirmed(newEmail)
                dialog.dismiss()
                
            } catch (e: Exception) {
                Log.e("EmailVerification", "Error during phone user email verification", e)
                handleVerificationError(e)
            }
        }
    }
    
    private fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    private fun updateConfirmButtonState(enabled: Boolean) {
        binding.btnConfirmEmail.isEnabled = enabled
        // Update background color programmatically for better visual feedback
        binding.btnConfirmEmail.setBackgroundTintList(
            ContextCompat.getColorStateList(context, 
                if (enabled) R.color.orange else R.color.color_AFB0B6
            )
        )
    }
    
    private fun verifyEmailWithPassword(password: String) {
        // Check network before starting verification
        if (!NetworkUtils.isNetworkAvailable(context)) {
            Toast.makeText(context, "Không có kết nối internet. Vui lòng kiểm tra và thử lại.", Toast.LENGTH_LONG).show()
            return
        }
        
        // Log network type for debugging
        val networkType = NetworkUtils.getNetworkTypeDescription(context)
        Log.d("EmailVerification", "Network type: $networkType")
        
        val currentUser = FirebaseAuth.getInstance().currentUser
        val currentEmail = currentUser?.email
        
        if (currentUser == null || currentEmail == null) {
            Toast.makeText(context, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Create credential for re-authentication
        val credential = EmailAuthProvider.getCredential(currentEmail, password)
        
        // Show loading state
        setLoadingState(true)
        
        // Cancel previous job if exists
        verificationJob?.cancel()
        
        // Use coroutine for async operation
        verificationJob = CoroutineScope(Dispatchers.Main).launch {
            try {
                // Re-authenticate user first
                currentUser.reauthenticate(credential).await()
                Log.d("EmailVerification", "Re-authentication successful")
                
                // Use verifyBeforeUpdateEmail instead of updateEmail
                currentUser.verifyBeforeUpdateEmail(newEmail).await()
                Log.d("EmailVerification", "Verification email sent")
                
                // Success - verification email sent
                Toast.makeText(context, 
                    "Email xác minh đã được gửi tới $newEmail. Vui lòng kiểm tra hộp thư và xác nhận để hoàn tất việc thay đổi.", 
                    Toast.LENGTH_LONG).show()
                
                // Note: Email is not immediately updated until user verifies
                onEmailConfirmed(newEmail)
                dialog.dismiss()
                
            } catch (e: Exception) {
                Log.e("EmailVerification", "Error during email verification process", e)
                handleVerificationError(e)
            }
        }
    }
    
    private fun setLoadingState(loading: Boolean) {
        binding.btnConfirmEmail.isEnabled = !loading
        binding.etPassword.isEnabled = !loading
        binding.btnConfirmEmail.text = if (loading) "Đang xử lý..." else "Xác Nhận"
    }
    
    private fun handleVerificationError(exception: Exception) {
        // Reset button state
        setLoadingState(false)
        
        val errorMessage = when (exception) {
            is FirebaseAuthInvalidCredentialsException -> {
                "Mật khẩu không đúng. Vui lòng thử lại."
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
        Log.e("EmailVerification", "Verification error: $errorMessage", exception)
    }
    
    fun dismiss() {
        verificationJob?.cancel()
        if (::dialog.isInitialized && dialog.isShowing) {
            dialog.dismiss()
        }
    }
} 
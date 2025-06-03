package com.qltc.finace.view.authentication.forgot_password

import android.content.Context
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.MutableLiveData
import com.qltc.finace.base.BaseViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    @ApplicationContext private val applicationContext: Context
) : BaseViewModel() {
    
    private val auth = FirebaseAuth.getInstance()
    
    var emailInput = ""
        set(value) {
            field = value
            validateEmail()
        }
    
    var isEnableButton = MutableLiveData<Boolean>(false)
    var errorEmail = MutableLiveData<String>("")
    
    init {
        // Set language code to Vietnamese for reset password email
        auth.setLanguageCode("vi")
    }
    
    private fun validateEmail() {
        val isValid = emailInput.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()
        isEnableButton.value = isValid
        errorEmail.value = if (isValid || emailInput.isEmpty()) "" else "Email không hợp lệ"
    }
    
    fun sendPasswordResetEmail(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            onError("Email không hợp lệ")
            return
        }
        
        auth.sendPasswordResetEmail(emailInput)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("ForgotPassword", "Password reset email sent successfully")
                    onSuccess()
                } else {
                    Log.e("ForgotPassword", "Failed to send password reset email", task.exception)
                    val errorMessage = when {
                        task.exception?.message?.contains("no user record") == true -> 
                            "Email không tồn tại trong hệ thống"
                        task.exception?.message?.contains("badly formatted") == true -> 
                            "Email không hợp lệ"
                        else -> "Không thể gửi email đặt lại mật khẩu. Vui lòng thử lại sau."
                    }
                    onError(errorMessage)
                }
            }
    }
} 
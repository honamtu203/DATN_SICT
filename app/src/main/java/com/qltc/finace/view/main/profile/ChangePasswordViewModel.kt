package com.qltc.finace.view.main.profile

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.qltc.finace.base.BaseViewModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChangePasswordViewModel @Inject constructor() : BaseViewModel() {
    
    val oldPassword = MutableLiveData<String>()
    val newPassword = MutableLiveData<String>()
    val confirmPassword = MutableLiveData<String>()
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _isPasswordChangeAllowed = MutableLiveData<Boolean>()
    val isPasswordChangeAllowed: LiveData<Boolean> = _isPasswordChangeAllowed
    
    private val _authProviderType = MutableLiveData<String>()
    val authProviderType: LiveData<String> = _authProviderType
    
    // LiveData để kiểm tra tính hợp lệ của form
    val isValidToSubmit = MediatorLiveData<Boolean>().apply {
        addSource(oldPassword) { validateForm() }
        addSource(newPassword) { validateForm() }
        addSource(confirmPassword) { validateForm() }
        addSource(_isPasswordChangeAllowed) { validateForm() }
    }
    
    init {
        checkAuthenticationProvider()
    }
    
    private fun checkAuthenticationProvider() {
        val user = FirebaseAuth.getInstance().currentUser
        
        if (user != null) {
            val providers = user.providerData?.map { it.providerId } ?: emptyList()
            val email = user.email
            val phone = user.phoneNumber
            
            val isGoogleLogin = providers.contains("google.com")
            val isOnlyPhoneLogin = email.isNullOrEmpty() && !phone.isNullOrEmpty()
            
            _isPasswordChangeAllowed.value = when {
                isGoogleLogin -> {
                    _authProviderType.value = "google"
                    false
                }
                isOnlyPhoneLogin -> {
                    _authProviderType.value = "phone"
                    false
                }
                else -> {
                    _authProviderType.value = "email"
                    true // Có email hoặc email + phone
                }
            }
        } else {
            _isPasswordChangeAllowed.value = false
            _authProviderType.value = "none"
        }
    }
    
    private fun validateForm() {
        // Nếu không được phép đổi mật khẩu thì không cho submit
        if (_isPasswordChangeAllowed.value != true) {
            isValidToSubmit.value = false
            return
        }
        
        val oldPass = oldPassword.value?.trim() ?: ""
        val newPass = newPassword.value?.trim() ?: ""
        val confirmPass = confirmPassword.value?.trim() ?: ""
        
        val isValid = isFormValid(oldPass, newPass, confirmPass)
        isValidToSubmit.value = isValid
    }
    
    private fun isFormValid(oldPass: String, newPass: String, confirmPass: String): Boolean {
        // Kiểm tra tất cả trường không trống
        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            return false
        }
        
        // Kiểm tra mật khẩu mới phải ít nhất 6 ký tự, có chữ, số và ký tự đặc biệt
        if (!isPasswordStrong(newPass)) {
            return false
        }
        
        // Kiểm tra mật khẩu mới khác mật khẩu cũ
        if (newPass == oldPass) {
            return false
        }
        
        // Kiểm tra mật khẩu mới khớp với xác nhận
        if (newPass != confirmPass) {
            return false
        }
        
        return true
    }
    
    private fun isPasswordStrong(password: String): Boolean {
        if (password.length < 6) return false
        
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecialChar = password.any { !it.isLetterOrDigit() }
        
        return hasLetter && hasDigit && hasSpecialChar
    }
    
    fun changePassword(
        oldPass: String,
        newPass: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // Kiểm tra lại một lần nữa trước khi thực hiện
        if (_isPasswordChangeAllowed.value != true) {
            onError("Không thể thay đổi mật khẩu với phương thức đăng nhập hiện tại")
            return
        }
        
        _isLoading.value = true
        
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null || currentUser.email == null) {
            _isLoading.value = false
            onError("Không tìm thấy thông tin người dùng")
            return
        }
        
        // Tạo credential để xác thực lại
        val credential = EmailAuthProvider.getCredential(currentUser.email!!, oldPass)
        
        // Xác thực lại người dùng
        currentUser.reauthenticate(credential)
            .addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    // Xác thực thành công, tiến hành đổi mật khẩu
                    currentUser.updatePassword(newPass)
                        .addOnCompleteListener { updateTask ->
                            _isLoading.value = false
                            if (updateTask.isSuccessful) {
                                // Đổi mật khẩu thành công
                                clearFields()
                                onSuccess()
                            } else {
                                // Đổi mật khẩu thất bại
                                onError("Đã xảy ra lỗi, vui lòng thử lại")
                            }
                        }
                } else {
                    // Xác thực thất bại
                    _isLoading.value = false
                    onError("Mật khẩu hiện tại không chính xác")
                }
            }
    }
    
    private fun clearFields() {
        oldPassword.value = ""
        newPassword.value = ""
        confirmPassword.value = ""
    }
    
    fun getPasswordRequirements(): String {
        return "Mật khẩu phải có ít nhất 6 ký tự, bao gồm chữ cái, số và ký tự đặc biệt"
    }
    
    fun getAuthProviderErrorMessage(): String {
        return when (_authProviderType.value) {
            "google" -> "Không thể thay đổi mật khẩu khi đăng nhập bằng Google"
            "phone" -> "Không thể thay đổi mật khẩu nếu chỉ đăng nhập bằng số điện thoại"
            "unknown" -> "Không thể thay đổi mật khẩu với phương thức đăng nhập hiện tại"
            "none" -> "Vui lòng đăng nhập để thay đổi mật khẩu"
            else -> ""
        }
    }
}
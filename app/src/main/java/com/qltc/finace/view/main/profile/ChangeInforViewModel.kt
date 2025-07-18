package com.qltc.finace.view.main.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.qltc.finace.base.BaseViewModel
import com.qltc.finace.data.entity.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChangeInforViewModel : BaseViewModel() {
    
    private val _currentUser = MutableLiveData<User>()
    val currentUser: LiveData<User> = _currentUser
    
    private val _currentUserName = MutableLiveData<String>()
    val currentUserName: LiveData<String> = _currentUserName
    
    val username = MutableLiveData<String>()
    val phone = MutableLiveData<String>()
    val email = MutableLiveData<String>()
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _updateResult = MutableLiveData<String>()
    val updateResult: LiveData<String> = _updateResult
    
    private val _showOtpDialog = MutableLiveData<String>()
    val showOtpDialog: LiveData<String> = _showOtpDialog
    
    private val _showEmailDialog = MutableLiveData<String>()
    val showEmailDialog: LiveData<String> = _showEmailDialog
    
    init {
        loadCurrentUserInfo()
    }
    
    private fun loadCurrentUserInfo() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        firebaseUser?.let { user ->
            // Create User object for display
            val userProfile = User(
                idUser = 0,
                name = user.displayName ?: user.email ?: "Người dùng",
                password = "",
                accountType = "google",
                phone = user.phoneNumber,
                mail = user.email
            )
            
            _currentUser.value = userProfile
            _currentUserName.value = userProfile.name
            
            // Set initial values for editing
            username.value = user.displayName ?: ""
            phone.value = user.phoneNumber ?: ""
            email.value = user.email ?: ""
        }
    }
    
    fun updateUserInfo(newUsername: String, newPhone: String, newEmail: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                if (firebaseUser == null) {
                    _updateResult.value = "Không tìm thấy thông tin người dùng"
                    return@launch
                }
                
                // Check changes for each field
                val hasUsernameChange = newUsername.isNotEmpty() && newUsername != (firebaseUser.displayName ?: "")
                val hasEmailChange = newEmail.isNotEmpty() && newEmail != (firebaseUser.email ?: "")
                val hasPhoneChange = newPhone.isNotEmpty() && newPhone != (firebaseUser.phoneNumber ?: "")
                
                // Validate phone number format if changed
                if (hasPhoneChange && !isValidPhoneNumber(newPhone)) {
                    _updateResult.value = "Số điện thoại không hợp lệ. Vui lòng nhập theo định dạng +84xxxxxxxxx"
                    return@launch
                }
                
                // Validate email format if changed
                if (hasEmailChange && !isValidEmail(newEmail)) {
                    _updateResult.value = "Định dạng email không hợp lệ"
                    return@launch
                }
                
                if (!hasUsernameChange && !hasEmailChange && !hasPhoneChange) {
                    _updateResult.value = "Không có thay đổi nào để lưu"
                    return@launch
                }
                
                // Priority: Phone > Email > Username
                when {
                    hasPhoneChange -> {
                        // If phone number changed, show OTP dialog
                        _showOtpDialog.value = newPhone
                    }
                    hasEmailChange -> {
                        // If email changed but not phone, show email verification dialog
                        _showEmailDialog.value = newEmail
                    }
                    else -> {
                        // Only username changed, update directly
                        updateWithoutPhoneAndEmail(newUsername, hasUsernameChange)
                    }
                }
                
            } catch (e: Exception) {
                _updateResult.value = "Lỗi cập nhật: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateWithPhoneCredential(
        phoneCredential: PhoneAuthCredential,
        newUsername: String,
        newEmail: String
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                if (firebaseUser == null) {
                    _updateResult.value = "Không tìm thấy thông tin người dùng"
                    return@launch
                }
                
                val results = mutableListOf<String>()
                val errors = mutableListOf<String>()
                
                // Update phone number first
                try {
                    firebaseUser.updatePhoneNumber(phoneCredential).await()
                    results.add("số điện thoại")
                } catch (e: Exception) {
                    errors.add("số điện thoại: ${e.message}")
                }
                
                // Update username if changed
                val hasUsernameChange = newUsername.isNotEmpty() && newUsername != (firebaseUser.displayName ?: "")
                if (hasUsernameChange) {
                    try {
                        val profileUpdates = userProfileChangeRequest {
                            displayName = newUsername
                        }
                        firebaseUser.updateProfile(profileUpdates).await()
                        results.add("tên người dùng")
                    } catch (e: Exception) {
                        errors.add("tên người dùng: ${e.message}")
                    }
                }
                
                // Check if email also needs to be updated
                val hasEmailChange = newEmail.isNotEmpty() && newEmail != firebaseUser.email
                if (hasEmailChange) {
                    // If email also changed, trigger email dialog after phone update
                    if (results.contains("số điện thoại")) {
                        _showEmailDialog.value = newEmail
                        return@launch
                    } else {
                        errors.add("email: Cần xác thực để thay đổi email")
                    }
                }
                
                // Reload user info
                loadCurrentUserInfo()
                
                // Create result message
                val message = when {
                    results.isNotEmpty() && errors.isEmpty() -> "Cập nhật thành công!"
                    results.isNotEmpty() && errors.isNotEmpty() -> {
                        "Đã cập nhật: ${results.joinToString(", ")}. Lỗi: ${errors.joinToString(", ")}"
                    }
                    errors.isNotEmpty() -> "Cập nhật thất bại: ${errors.joinToString(", ")}"
                    else -> "Không có thay đổi nào được thực hiện"
                }
                
                _updateResult.value = message
                
            } catch (e: Exception) {
                _updateResult.value = "Lỗi cập nhật: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateWithEmailCredential(
        newEmail: String,
        newUsername: String
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                if (firebaseUser == null) {
                    _updateResult.value = "Không tìm thấy thông tin người dùng"
                    return@launch
                }
                
                val results = mutableListOf<String>()
                val errors = mutableListOf<String>()
                
                // Email is already updated in the dialog, just add to results
                results.add("email")
                
                // Update username if changed
                val hasUsernameChange = newUsername.isNotEmpty() && newUsername != (firebaseUser.displayName ?: "")
                if (hasUsernameChange) {
                    try {
                        val profileUpdates = userProfileChangeRequest {
                            displayName = newUsername
                        }
                        firebaseUser.updateProfile(profileUpdates).await()
                        results.add("tên người dùng")
                    } catch (e: Exception) {
                        errors.add("tên người dùng: ${e.message}")
                    }
                }
                
                // Reload user info
                loadCurrentUserInfo()
                
                // Create result message
                val message = when {
                    results.isNotEmpty() && errors.isEmpty() -> "Cập nhật thành công!"
                    results.isNotEmpty() && errors.isNotEmpty() -> {
                        "Đã cập nhật: ${results.joinToString(", ")}. Lỗi: ${errors.joinToString(", ")}"
                    }
                    errors.isNotEmpty() -> "Cập nhật thất bại: ${errors.joinToString(", ")}"
                    else -> "Không có thay đổi nào được thực hiện"
                }
                
                _updateResult.value = message
                
            } catch (e: Exception) {
                _updateResult.value = "Lỗi cập nhật: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateWithoutPhoneAndEmail(newUsername: String, hasUsernameChange: Boolean) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                if (firebaseUser == null) {
                    _updateResult.value = "Không tìm thấy thông tin người dùng"
                    return@launch
                }
                
                val results = mutableListOf<String>()
                val errors = mutableListOf<String>()
                
                // Update username if changed
                if (hasUsernameChange) {
                    try {
                        val profileUpdates = userProfileChangeRequest {
                            displayName = newUsername
                        }
                        firebaseUser.updateProfile(profileUpdates).await()
                        results.add("tên người dùng")
                    } catch (e: Exception) {
                        errors.add("tên người dùng: ${e.message}")
                    }
                }
                
                // Reload user info
                loadCurrentUserInfo()
                
                // Create result message
                val message = when {
                    results.isNotEmpty() && errors.isEmpty() -> "Cập nhật thành công!"
                    errors.isNotEmpty() -> "Cập nhật thất bại: ${errors.joinToString(", ")}"
                    else -> "Không có thay đổi nào hợp lệ để cập nhật"
                }
                
                _updateResult.value = message
                
            } catch (e: Exception) {
                _updateResult.value = "Lỗi cập nhật: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateWithoutPhone(newUsername: String, newEmail: String) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser == null) {
            _updateResult.value = "Không tìm thấy thông tin người dùng"
            return
        }
        
        val hasUsernameChange = newUsername.isNotEmpty() && newUsername != (firebaseUser.displayName ?: "")
        val hasEmailChange = newEmail.isNotEmpty() && newEmail != (firebaseUser.email ?: "")
        
        when {
            hasEmailChange -> {
                // Show email dialog for email change
                _showEmailDialog.value = newEmail
            }
            hasUsernameChange -> {
                // Only username change
                updateWithoutPhoneAndEmail(newUsername, hasUsernameChange)
            }
            else -> {
                _updateResult.value = "Không có thay đổi nào hợp lệ để cập nhật"
            }
        }
    }
    
    private fun isValidPhoneNumber(phone: String): Boolean {
        // Basic validation for Vietnamese phone numbers with country code
        return phone.matches(Regex("^\\+84[0-9]{9,10}$"))
    }
    
    fun getCurrentUsername(): String {
        return FirebaseAuth.getInstance().currentUser?.displayName ?: ""
    }
    
    fun getCurrentPhone(): String {
        return FirebaseAuth.getInstance().currentUser?.phoneNumber ?: ""
    }
    
    fun getCurrentEmail(): String {
        return FirebaseAuth.getInstance().currentUser?.email ?: ""
    }
    
    fun isGoogleAuthUser(): Boolean {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return currentUser?.providerData?.any { it.providerId == "google.com" } == true
    }
    
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
} 
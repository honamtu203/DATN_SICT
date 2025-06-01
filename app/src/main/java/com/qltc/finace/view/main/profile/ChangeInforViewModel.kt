package com.qltc.finace.view.main.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.qltc.finace.base.BaseViewModel
import com.qltc.finace.data.entity.User
import com.google.firebase.auth.FirebaseAuth
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
                
                val currentUser = _currentUser.value
                var hasChanges = false
                
                // Check if there are any changes
                if (newUsername.isNotEmpty() && newUsername != (firebaseUser.displayName ?: "")) {
                    hasChanges = true
                }
                if (newPhone.isNotEmpty() && newPhone != (firebaseUser.phoneNumber ?: "")) {
                    hasChanges = true
                }
                if (newEmail.isNotEmpty() && newEmail != (firebaseUser.email ?: "")) {
                    hasChanges = true
                }
                
                if (!hasChanges) {
                    _updateResult.value = "Không có thay đổi nào để lưu"
                    return@launch
                }
                
                // Update display name if changed
                if (newUsername.isNotEmpty() && newUsername != (firebaseUser.displayName ?: "")) {
                    val profileUpdates = userProfileChangeRequest {
                        displayName = newUsername
                    }
                    firebaseUser.updateProfile(profileUpdates).await()
                }
                
                // Update email if changed (requires re-authentication in real app)
                if (newEmail.isNotEmpty() && newEmail != firebaseUser.email) {
                    // TODO: Implement email update with re-authentication
                    // firebaseUser.updateEmail(newEmail).await()
                }
                
                // TODO: Update phone number (requires verification in real app)
                // if (newPhone.isNotEmpty() && newPhone != firebaseUser.phoneNumber) {
                //     // Implement phone number update with verification
                // }
                
                // Reload user info after update
                loadCurrentUserInfo()
                
                _updateResult.value = "Cập nhật thành công!"
                
            } catch (e: Exception) {
                _updateResult.value = "Lỗi cập nhật: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
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
} 
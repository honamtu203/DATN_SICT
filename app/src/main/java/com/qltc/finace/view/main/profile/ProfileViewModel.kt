package com.qltc.finace.view.main.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.qltc.finace.base.BaseViewModel
import com.qltc.finace.data.entity.User
import com.google.firebase.auth.FirebaseAuth

class ProfileViewModel : BaseViewModel() {
    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    init {
        updateUserProfile()
    }

    fun updateUserProfile() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        
        // Create new User instance with required parameters
        val userProfile = User(
            idUser = 0, // Default value since it's auto-generated
            name = currentUser?.displayName ?: currentUser?.email ?: "Người dùng",
            password = "", // Empty since we're using Firebase auth
            accountType = "google", // Default to google auth
            phone = currentUser?.phoneNumber,
            mail = currentUser?.email
        )
        
        _user.value = userProfile
    }
}
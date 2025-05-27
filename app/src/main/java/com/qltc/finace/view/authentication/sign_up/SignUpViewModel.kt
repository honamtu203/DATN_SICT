package com.qltc.finace.view.authentication.sign_up

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.qltc.finace.R
import com.qltc.finace.base.BaseViewModel
import com.qltc.finace.data.repository.local.category.CategoryRepository
import com.qltc.finace.extension.isPasswordValid
import com.qltc.finace.extension.isValidEmail
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    @ApplicationContext var applicationContext: Context,
    private val categoryRepository: CategoryRepository
): BaseViewModel() {
    val auth = FirebaseAuth.getInstance()
    var emailInput = ""
        set(value) {
            field = value
            checkValidEmail()
            checkEnableButtonSignUp()
        }
    var passwordInput = ""
        set(value) {
            field = value
            checkValidPassword()
            checkValidConfirmPassword()
            checkEnableButtonSignUp()
        }
    var confirmPassword = ""
        set(value) {
            field = value
            checkValidConfirmPassword()
            checkEnableButtonSignUp()
        }

    var errorEmail = MutableLiveData("")
    var errorPassword = MutableLiveData("")
    var errorConfirmPassword = MutableLiveData("")
    var isEnableButtonSignUp = MutableLiveData(false)

    private fun checkValidEmail() {
        errorEmail.value = if (emailInput.isValidEmail()) "" else applicationContext.getString(R.string.error_email)
    }
    private fun checkValidPassword() {
        errorPassword.value = if (passwordInput.isPasswordValid()) "" else applicationContext.getString(R.string.error_password)
    }
    private fun checkValidConfirmPassword() {
        if ((confirmPassword.isPasswordValid()
            && passwordInput.isPasswordValid()
            && passwordInput == confirmPassword)
            || confirmPassword.isEmpty())
        {
            errorConfirmPassword.value = ""
        }
        else
            errorConfirmPassword.value = applicationContext.getString(R.string.error_confirm_password)
    }
    private fun checkEnableButtonSignUp(){
        isEnableButtonSignUp.value = (
                emailInput.isValidEmail()
                && passwordInput.isPasswordValid()
                && confirmPassword.isPasswordValid()
                && passwordInput == confirmPassword)
    }
    fun signUp(callback: (Boolean, String) -> Unit) {
        if(auth.currentUser == null) {
            auth.createUserWithEmailAndPassword(emailInput, passwordInput).addOnCompleteListener{task ->
                if (task.isSuccessful) {
                    callback(true, "")
                }
                else {
                    callback(false, task.exception?.message.toString())
                }
            }
        }
        else {

        }
    }
    fun insertDefaultCategory(success : () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            categoryRepository.importCategoryDefault()
            withContext(Dispatchers.Main) {
                success()
            }

        }
    }
}
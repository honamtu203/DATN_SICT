package com.qltc.finace.view.authentication.sign_in

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.qltc.finace.base.BaseViewModel
import com.qltc.finace.data.repository.local.category.CategoryRepository
import com.google.android.material.snackbar.ContentViewCallback
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.internal.operators.maybe.MaybeDoAfterSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val categoryRepository: CategoryRepository
): BaseViewModel() {
    val auth = FirebaseAuth.getInstance()
    var emailInput = ""
        set(value) {
            field = value
            checkInput()
        }
    var passwordInput = ""
        set(value) {
            field = value
            checkInput()
        }
    var isEnableButton = MutableLiveData<Boolean>(false)
    private fun checkInput() {
        if (emailInput.isNotEmpty() && passwordInput.isNotEmpty()) {
            isEnableButton.value = true
        }
        else {
            isEnableButton.value = false
        }
    }
    fun insertDefaultCategory(isNewsUser : Boolean,success : () -> Unit) {
        if (!isNewsUser) {
            success()
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            categoryRepository.importCategoryDefault()
            withContext(Dispatchers.Main) {
                success()
            }

        }
    }
    fun signInWithEmail(callback: (Boolean, String) -> Unit) {
        if(auth.currentUser == null) {
            auth.signInWithEmailAndPassword(emailInput, passwordInput).addOnCompleteListener{task ->
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

}
package com.qltc.finace.view.authentication.sign_in

interface SignInListener {
    fun openSignInGoogle()
    fun openSignInFacebook()
    fun openSignInPhone()
    fun openApp()
    fun openSignUp()
    fun signUpWithEmail()
    fun onForgotPasswordClick()
}
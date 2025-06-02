package com.qltc.finace.view.authentication.sign_up

interface SignUpListener {
    fun signUp()
    fun openSignInGoogle()
    fun openSignInFacebook()
    fun openSignInPhone()
    fun openApp()
    fun backSignUp()
}
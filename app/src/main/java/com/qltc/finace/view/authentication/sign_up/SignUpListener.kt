package com.qltc.finace.view.authentication.sign_up

interface SignUpListener {
    fun signUp()
    fun openSignInGoogle()
    fun openSignInFacebook()
    fun openApp()
    fun backSignUp()
}
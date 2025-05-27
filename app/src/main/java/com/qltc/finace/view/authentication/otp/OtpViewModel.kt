package com.qltc.finace.view.authentication.otp

import com.qltc.finace.base.BaseViewModel
import com.google.firebase.auth.FirebaseAuth

class OtpViewModel : BaseViewModel() {
    val firebaseAuth = FirebaseAuth.getInstance()
}
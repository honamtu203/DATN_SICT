package com.qltc.finace.view.activity.splash

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.qltc.finace.view.activity.authen.AuthenticationActivity
import com.qltc.finace.view.activity.home.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RoutingActivity : AppCompatActivity(){
    private val viewModel : SplashViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen =  installSplashScreen()
        super.onCreate(savedInstanceState)

        val content: View = findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            ViewTreeObserver.OnPreDrawListener { // Check whether the initial data is ready.
                if (viewModel.checkUser()) {
                    splashScreen.setKeepOnScreenCondition { true }
                    this.startActivityHome()
                    true
                }
                else {
                    splashScreen.setKeepOnScreenCondition { true }
                    this.startActivityAuthentication()
                    true
                }
            }
        )


    }
    private fun startActivityHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }
    private fun startActivityAuthentication() {
        val intent = Intent(this, AuthenticationActivity::class.java)
        startActivity(intent)
    }
}
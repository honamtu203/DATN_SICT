package com.qltc.finace.view.activity.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.qltc.finace.R
import com.qltc.finace.view.activity.authen.AuthenticationActivity
import com.qltc.finace.view.activity.home.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val SPLASH_DELAY = 5000L // 5 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Sử dụng Handler để delay chuyển màn hình
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToNextScreen()
        }, SPLASH_DELAY)
    }

    private fun navigateToNextScreen() {
        // Kiểm tra trạng thái đăng nhập
        val currentUser = auth.currentUser
        val intent = if (currentUser != null) {
            // Nếu đã đăng nhập, chuyển đến MainActivity
            Intent(this, HomeActivity::class.java)
        } else {
            // Nếu chưa đăng nhập, chuyển đến AuthenticationActivity
            Intent(this, AuthenticationActivity::class.java)
        }

        startActivity(intent)
        finish() // Đóng SplashActivity để không quay lại được
        
        // Thêm animation cho việc chuyển màn hình
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
}
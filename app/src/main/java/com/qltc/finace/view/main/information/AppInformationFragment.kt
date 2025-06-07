package com.qltc.finace.view.main.information

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.qltc.finace.BuildConfig
import com.qltc.finace.R
import com.qltc.finace.base.BaseFragment
import com.qltc.finace.databinding.FragmentAppInformationBinding

class AppInformationFragment : BaseFragment<FragmentAppInformationBinding, AppInformationViewModel>(), AppInfoListener {
    
    override val layoutID: Int = R.layout.fragment_app_information
    override val viewModel: AppInformationViewModel by viewModels()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.listener = this
        
        // Hiển thị phiên bản ứng dụng
        val versionName = BuildConfig.VERSION_NAME
        val versionCode = BuildConfig.VERSION_CODE
        viewBinding.tvVersion.text = "Phiên bản $versionName ($versionCode)"
    }
    
    override fun onBackClicked() {
        try {
            findNavController().popBackStack()
        } catch (e: Exception) {
            // Fallback nếu popBackStack() gặp lỗi
            try {
                findNavController().popBackStack(R.id.frag_home, false)
            } catch (e2: Exception) {
                try {
                    // Phương pháp thay thế cuối cùng
                    findNavController().navigate(R.id.frag_home)
                } catch (e3: Exception) {
                    // Ghi log lỗi
                    e3.printStackTrace()
                }
            }
        }
    }
    
    override fun onPrivacyPolicyClicked() {
        // Mở trang web chính sách bảo mật
        val url = "https://yourdomain.com/privacy-policy"
        openWebPage(url)
    }
    
    override fun onTermsOfServiceClicked() {
        // Mở trang web điều khoản sử dụng
        val url = "https://yourdomain.com/terms-of-service"
        openWebPage(url)
    }
    
    override fun onRateAppClicked() {
        // Mở Google Play để đánh giá ứng dụng
        val packageName = requireContext().packageName
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (e: android.content.ActivityNotFoundException) {
            // Nếu không có Google Play, mở trang web
            startActivity(Intent(Intent.ACTION_VIEW, 
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
        }
    }
    
    private fun openWebPage(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
} 
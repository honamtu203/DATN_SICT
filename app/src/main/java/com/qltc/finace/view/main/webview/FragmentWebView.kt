package com.qltc.finace.view.main.webview

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.qltc.finace.R
import com.qltc.finace.base.BaseFragment
import com.qltc.finace.databinding.FragmentWebviewBinding

class FragmentWebView : BaseFragment<FragmentWebviewBinding, WebViewViewModel>(), WebViewListener {
    override val layoutID: Int = R.layout.fragment_webview
    override val viewModel: WebViewViewModel by viewModels()
    
    private var urlToLoad: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.lifecycleOwner = this
        viewBinding.listener = this
        
        // Get URL from arguments
        urlToLoad = arguments?.getString("url") ?: "https://notebooklm.google.com/"
        
        setupWebView()
        loadUrl()
    }

    private fun setupWebView() {
        viewBinding.webView.apply {
            // Enable JavaScript
            settings.javaScriptEnabled = true
            
            // Enable DOM storage
            settings.domStorageEnabled = true
            
            // Set WebViewClient to keep navigation within the app
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    return false // Stay within the WebView
                }
                
                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    viewBinding.progressBar.visibility = View.VISIBLE
                }
                
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    viewBinding.progressBar.visibility = View.GONE
                }
            }
            
            // Enable zoom controls
            settings.setSupportZoom(true)
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
            
            // Set user agent to desktop for better experience
            settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
        }
    }

    private fun loadUrl() {
        urlToLoad?.let { url ->
            viewBinding.webView.loadUrl(url)
        }
    }

    override fun onBackClick() {
        // Handle back navigation - check if WebView can go back
        if (viewBinding.webView.canGoBack()) {
            viewBinding.webView.goBack()
        } else {
            findNavController().navigate(R.id.fag_home)
        }
    }

    override fun onRefreshClick() {
        viewBinding.webView.reload()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up WebView
        viewBinding.webView.destroy()
    }
} 
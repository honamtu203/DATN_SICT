package com.qltc.finace.view.main.webview

import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebSettings
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
        urlToLoad = arguments?.getString("url") ?: "https://notebooklm.google.com/notebook/9f6ff707-0fbb-44ce-bf94-358fc51fc0b9?_gl=1*nxffyy*_ga*OTE5NDI3NzQ2LjE3NDg2NzI4NzI.*_ga_W0LDH41ZCB*czE3NDg2NzI4NzIkbzEkZzEkdDE3NDg2NzI4NzIkajYwJGwwJGgw&original_referer=https:%2F%2Fnotebooklm.google%23&pli=1"
        
        setupWebView()
        loadUrl()
    }

    private fun setupWebView() {
        // Enable cookies
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(viewBinding.webView, true)
        
        viewBinding.webView.apply {
            settings.apply {
                // Enable JavaScript (with caution for security)
                javaScriptEnabled = true
                
                // Enable DOM storage
                domStorageEnabled = true
                
                // Enable database storage
                databaseEnabled = true
                
                // Allow file access (restricted)
                allowFileAccess = false // Set to false for security
                allowContentAccess = true
                
                // Enable mixed content (with restrictions)
                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                
                // Enable zoom controls
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                
                // Enable viewport
                useWideViewPort = true
                loadWithOverviewMode = true
                
                // Set cache mode
                cacheMode = WebSettings.LOAD_DEFAULT
                
                // Set more realistic user agent (Chrome on Android)
                userAgentString = "Mozilla/5.0 (Linux; Android 10; SM-G973F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                
                // Enable safe browsing
                safeBrowsingEnabled = true
                
                // Security: Disable universal access from file URLs
                allowUniversalAccessFromFileURLs = false
                allowFileAccessFromFileURLs = false
                
                // Enable geolocation
                setGeolocationEnabled(true)
                
                // Additional security settings
                javaScriptCanOpenWindowsAutomatically = false
                mediaPlaybackRequiresUserGesture = false
            }
            
            // Set WebViewClient to keep navigation within the app
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    // Allow Google authentication URLs
                    return if (url?.contains("accounts.google.com") == true || 
                              url?.contains("9f6ff707-0fbb-44ce-bf94-358fc51fc0b9") == true ||
                              url?.contains("google.com") == true) {
                        false // Stay within the WebView
                    } else {
                        false // Stay within the WebView for all URLs
                    }
                }
                
                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    viewBinding.progressBar.visibility = View.VISIBLE
                }
                
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    viewBinding.progressBar.visibility = View.GONE
                    
                    // Inject JavaScript to improve compatibility (with security considerations)
                    url?.let {
                        if (it.contains("google.com") || it.contains("notebooklm")) {
                            view?.evaluateJavascript("""
                                // Make the webview appear more like a regular browser
                                try {
                                    Object.defineProperty(navigator, 'webdriver', { 
                                        get: () => false,
                                        configurable: true 
                                    });
                                } catch(e) { /* ignore */ }
                            """, null)
                        }
                    }
                }
                
                override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                    super.onReceivedError(view, errorCode, description, failingUrl)
                    // Handle errors gracefully
                    viewBinding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun loadUrl() {
        urlToLoad?.let { url ->
            // Set additional headers to make request more authentic
            val headers = mapOf(
                "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8",
                "Accept-Language" to "vi-VN,vi;q=0.9,en-US;q=0.8,en;q=0.7",
                "Accept-Encoding" to "gzip, deflate, br",
                "DNT" to "1",
                "Upgrade-Insecure-Requests" to "1",
                "Sec-Fetch-Site" to "none",
                "Sec-Fetch-Mode" to "navigate",
                "Sec-Fetch-User" to "?1"
            )
            
            viewBinding.webView.loadUrl(url, headers)
        }
    }

    override fun onBackClick() {
        // Handle back navigation - check if WebView can go back
        if (viewBinding.webView.canGoBack()) {
            viewBinding.webView.goBack()
        } else {
            findNavController().navigate(R.id.frag_home)
        }
    }

    override fun onRefreshClick() {
        viewBinding.webView.reload()
    }

    override fun onCloseClick() {
        findNavController().navigate(R.id.frag_enter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up WebView
        viewBinding.webView.destroy()
    }
} 
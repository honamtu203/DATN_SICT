package com.qltc.finace.view.main.webview

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import com.qltc.finace.R

object ChromeCustomTabHelper {
    
    /**
     * Opens URL in Chrome Custom Tab if available, otherwise fallback to system browser
     */
    fun openUrlInCustomTab(context: Context, url: String) {
        try {
            if (isChromeCustomTabsSupported(context)) {
                // Open with Chrome Custom Tabs
                val customTabsIntent = CustomTabsIntent.Builder()
                    .setToolbarColor(ContextCompat.getColor(context, R.color.orange))
                    .setShowTitle(true)
                    .setStartAnimations(context, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .setExitAnimations(context, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .build()
                
                customTabsIntent.launchUrl(context, Uri.parse(url))
            } else {
                // Fallback to system browser
                openInSystemBrowser(context, url)
            }
        } catch (e: Exception) {
            // If anything fails, fallback to system browser
            openInSystemBrowser(context, url)
        }
    }
    
    /**
     * Check if Chrome Custom Tabs is supported
     */
    private fun isChromeCustomTabsSupported(context: Context): Boolean {
        val serviceIntent = Intent("android.support.customtabs.action.CustomTabsService")
        serviceIntent.setPackage("com.android.chrome")
        val resolveInfos = context.packageManager.queryIntentServices(serviceIntent, PackageManager.MATCH_ALL)
        return resolveInfos.isNotEmpty()
    }
    
    /**
     * Fallback to system browser
     */
    private fun openInSystemBrowser(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            // Handle error if no browser is available
            e.printStackTrace()
        }
    }
} 
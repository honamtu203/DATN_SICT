package com.qltc.finace.view.main.webview

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import com.qltc.finace.R

object NotebookLMOptionDialog {
    
    fun showOpenOptions(context: Context, navController: NavController?) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Mở NotebookLM")
        builder.setMessage("Chọn cách mở NotebookLM:\n\n🌐 Chrome Custom Tabs: Bảo mật cao, đăng nhập Google dễ dàng\n\n📱 WebView: Mở trong ứng dụng")
        
        builder.setPositiveButton("🌐 Chrome Tabs") { dialog, _ ->
            // Open with Chrome Custom Tabs (Recommended)
            ChromeCustomTabHelper.openUrlInCustomTab(context, "https://notebooklm.google.com/notebook/9f6ff707-0fbb-44ce-bf94-358fc51fc0b9?_gl=1*nxffyy*_ga*OTE5NDI3NzQ2LjE3NDg2NzI4NzI.*_ga_W0LDH41ZCB*czE3NDg2NzI4NzIkbzEkZzEkdDE3NDg2NzI4NzIkajYwJGwwJGgw&original_referer=https:%2F%2Fnotebooklm.google%23&pli=1")
            dialog.dismiss()
        }
        
        builder.setNegativeButton("📱 WebView") { dialog, _ ->
            // Open with improved WebView
            val bundle = bundleOf("url" to "https://notebooklm.google.com/notebook/9f6ff707-0fbb-44ce-bf94-358fc51fc0b9?_gl=1*nxffyy*_ga*OTE5NDI3NzQ2LjE3NDg2NzI4NzI.*_ga_W0LDH41ZCB*czE3NDg2NzI4NzIkbzEkZzEkdDE3NDg2NzI4NzIkajYwJGwwJGgw&original_referer=https:%2F%2Fnotebooklm.google%23&pli=1")
            navController?.navigate(R.id.frg_webview, bundle)
            dialog.dismiss()
        }
        
        builder.setNeutralButton("Hủy") { dialog, _ ->
            dialog.dismiss()
        }
        
        builder.show()
    }
} 
package com.template

import android.os.Bundle
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.template.databinding.ActivityWebBinding

@Suppress("DEPRECATION")
class WebActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebBinding

    private lateinit var webView: WebView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebBinding.inflate(layoutInflater)
        setContentView(binding.root)

        webView.settings.javaScriptEnabled = true

        CookieManager.getInstance().setAcceptCookie(true)

        webView.settings.javaScriptCanOpenWindowsAutomatically = true

        webView.isSaveEnabled = true

        val url = intent.getStringExtra("url")
        url?.let { binding.webView.loadUrl(url) }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()){
            webView.goBack()
            } else{
                Log.d("onBackPressed","nowhere to go back")
        }
    }
}

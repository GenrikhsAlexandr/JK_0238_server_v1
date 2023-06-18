package com.template

import android.os.Bundle
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.template.databinding.ActivityWebBinding

class WebActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebBinding
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val webView: WebView = binding.webView
        webView.settings.javaScriptEnabled = true

        CookieManager.getInstance().setAcceptCookie(true)

        webView.settings.javaScriptCanOpenWindowsAutomatically = true

        webView.isSaveEnabled = true

        val url = intent.getStringExtra("url")
        url?.let { binding.webView.loadUrl(url) } ?: kotlin.run {
            Toast.makeText(this, "Url not found", Toast.LENGTH_LONG).show()
        }

        onBackPressedDispatcher.addCallback(this,onBackInvokeCallBack)
    }

    private val onBackInvokeCallBack = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (webView.canGoBack()) {
                webView.goBack()
            } else {
                Log.d("onBackPressed", "nowhere to go back")
            }
        }
    }
}

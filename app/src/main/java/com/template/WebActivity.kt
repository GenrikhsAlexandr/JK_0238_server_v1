package com.template

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.template.databinding.ActivityWebBinding

@Suppress("DEPRECATION")
class WebActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebBinding
    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val webView: WebView = binding.webView

        webView.settings.javaScriptEnabled = true

        webView.webViewClient = WebViewClient()

        CookieManager.getInstance().setAcceptCookie(true)

        webView.settings.javaScriptCanOpenWindowsAutomatically = true

        webView.isSaveEnabled = true

        val url = intent.getStringExtra(KEY_EXTRA_URL)
        binding.webView.loadUrl(url!!)

        onBackPressedDispatcher.addCallback(this, onBackInvokeCallBack)
    }

    private val onBackInvokeCallBack = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (webView.canGoBack()) {
                webView.goBack()
            } else {
            }
        }
    }

    companion object {
        private const val KEY_EXTRA_URL = "url"

        fun start(context: Context, url: String) {
            val intent = Intent(context, WebActivity::class.java)
            intent.putExtra(KEY_EXTRA_URL, url)
            context.startActivity(intent)
        }
    }
}

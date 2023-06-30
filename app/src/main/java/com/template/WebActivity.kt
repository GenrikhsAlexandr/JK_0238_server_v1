package com.template

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.template.databinding.ActivityWebBinding

@Suppress("DEPRECATION")
class WebActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebBinding
    private var webView: WebView? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         binding = ActivityWebBinding.inflate(layoutInflater)
        setContentView(binding.root)

        webView = binding.webView

        webView?.settings?.javaScriptEnabled = true
        webView?.settings?.domStorageEnabled = true
        webView?.settings?.javaScriptCanOpenWindowsAutomatically = true
        webView!!.isSaveEnabled = true
        CookieManager.getInstance().setAcceptCookie(true)
        webView!!.webViewClient = WebViewClient()

        val url = intent.getStringExtra(KEY_EXTRA_URL)
        webView!!.loadUrl(url!!)

        onBackPressedDispatcher.addCallback(onBackInvokeCallBack)
    }

    private val onBackInvokeCallBack = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (webView?.canGoBack() == true) {
                webView?.goBack()
            } else {
                Log.d("webView", "WebActivity handleOnBackPressed: No history")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webView?.destroy()
        webView = null
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
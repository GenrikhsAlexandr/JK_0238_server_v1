package com.template

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
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
    private var previousUrl: String? = null
    private var currentUrl: String? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebBinding.inflate(layoutInflater)
        setContentView(binding.root)

        webView = binding.webView
        webView?.settings?.javaScriptEnabled = true
        webView?.settings?.domStorageEnabled = true
        webView?.settings?.javaScriptCanOpenWindowsAutomatically = true
        webView?.isSaveEnabled = true
        CookieManager.getInstance().setAcceptCookie(true)
        webView?.webViewClient = WebViewClient()

        val url = intent.getStringExtra(KEY_EXTRA_URL)
        webView?.loadUrl(url!!)

        val onBackInvokeCallBack = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                currentUrl = webView?.url
                Log.d("webView", "getUrl  = $currentUrl")

                if (previousUrl == null) {
                    previousUrl = webView?.url
                    Log.d("webView", "previousUrlUrlNull = $previousUrl")
                    return
                }

                if (currentUrl != previousUrl) {
                    if (webView?.canGoBack() == true) {
                        webView?.goBack()
                        currentUrl = webView?.url
                        Log.d("webView", "canGoBack = ${webView!!.id}")
                    } else {
                        Log.d(
                            "webView",
                            "WebActivity handleOnBackPressed: No history  ${webView.toString()}"
                        )
                    }
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackInvokeCallBack)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
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
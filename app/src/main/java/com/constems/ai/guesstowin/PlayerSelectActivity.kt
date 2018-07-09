package com.constems.ai.guesstowin

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_player_select.*
import okhttp3.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.longToast
import org.jetbrains.anko.noButton
import org.jetbrains.anko.yesButton
import java.io.IOException

class PlayerSelectActivity : AppCompatActivity() {

    private val tag = PlayerSelectActivity::class.java.simpleName
    private var userName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_select)

        setSupportActionBar(toolbar_playerSelect)

        val builder = intent.extras
        userName = builder.getString("username", "")
        Log.i(tag, userName)
    }

    private fun getPostRequest(userName: String) {

        val client = OkHttpClient()
        val body = FormBody.Builder()
                .add("userName", userName)
                .build()

        val request = Request.Builder()
                .url("http://192.168.1.82/game_script/logout.php")
                .post(body)
                .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: okhttp3.Call?, e: IOException?) {
                val message = e?.message.toString()
                Log.w(tag, "Failure Response: $message")

                this@PlayerSelectActivity.runOnUiThread {
                    longToast("Please check your internet Connection!")
                }
            }

            override fun onResponse(call: okhttp3.Call?, response: Response?) {
                val message = response?.body()?.string()
                Log.d(tag, "Message received $message")
                this@PlayerSelectActivity.runOnUiThread {
                    if (message != "[]" && message != "0") {
                        finish()
                    } else {
                        longToast("Error occurred")
                    }
                }
            }
        })
    }

    override fun onBackPressed() {
        alert("Do you want to Logout?") {
            yesButton {
                getPostRequest(userName!!)
            }
            noButton { }
        }.show()
    }
}

package com.constems.ai.guesstowin

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_waiting_for_players.*
import okhttp3.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.longToast
import org.jetbrains.anko.noButton
import org.jetbrains.anko.yesButton
import java.io.IOException

class WaitingForPlayersActivity : AppCompatActivity() {

    private val tag = WaitingForPlayersActivity::class.java.simpleName
    private var userId: Int = 0
    private var myFirebaseId: String = String()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting_for_players)

        setSupportActionBar(toolbar_waitingForPlayers)

        val builder = intent.extras
        userId = builder.getInt("user_id", 0)
        myFirebaseId = builder.getString("firebase_id", "")
    }

    private fun getPostRequest(userId: Int) {
        val client = OkHttpClient()
        val body = FormBody.Builder()
                .add("userId", userId.toString())
                .build()

        val request = Request.Builder()
                .url("http://192.168.1.82/game_script/logout.php")
                .post(body)
                .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: okhttp3.Call?, e: IOException?) {
                val message = e?.message.toString()
                Log.w(tag, "Failure Response: $message")

                this@WaitingForPlayersActivity.runOnUiThread {
                    longToast("Please check your internet Connection!")
                }
            }

            override fun onResponse(call: okhttp3.Call?, response: Response?) {
                val message = response?.body()?.string()
                Log.d(tag, "Message received $message")
                this@WaitingForPlayersActivity.runOnUiThread {
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
        alert("Do you want to stop hosting?") {
            yesButton {
                getPostRequest(userId)
            }
            noButton { }
        }.show()
    }
}

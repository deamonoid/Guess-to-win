package com.constems.ai.guesstowin

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_multiplayer_mode.*
import okhttp3.*
import org.jetbrains.anko.longToast
import java.io.IOException

class MultiPlayerModeActivity : AppCompatActivity() {

    private val tag = MultiPlayerModeActivity::class.java.simpleName
    private var userId: Int = 0
    private var myFirebaseId: String = String()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multiplayer_mode)

        setSupportActionBar(toolbar_multiplayerMode)

        val builder = intent.extras
        userId = builder.getInt("user_id", 0)
        myFirebaseId = builder.getString("firebase_id", "")


        button_multiplayerMode_host.setOnClickListener {
            button_multiplayerMode_host.isEnabled = false
            getPostRequest(userId)
        }

        button_multiplayerMode_join.setOnClickListener {
            val intent = Intent(this@MultiPlayerModeActivity, PlayerSelectActivity::class.java)
            intent.putExtra("user_id", userId)
            intent.putExtra("firebase_id", myFirebaseId)
            startActivity(intent)
        }
    }

    private fun getPostRequest(userId: Int) {
        val client = OkHttpClient()
        val body = FormBody.Builder()
                .add("userId", userId.toString())
                .build()

        val request = Request.Builder()
                .url("http://192.168.1.82/game_script/host_game.php")
                .post(body)
                .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: okhttp3.Call?, e: IOException?) {
                val message = e?.message.toString()
                Log.w(tag, "Failure Response: $message")

                this@MultiPlayerModeActivity.runOnUiThread {
                    longToast("Please check your internet Connection!")
                    button_multiplayerMode_host.isEnabled = true
                }
            }

            override fun onResponse(call: okhttp3.Call?, response: Response?) {
                val message = response?.body()?.string()
                Log.d(tag, "Message received $message")
                this@MultiPlayerModeActivity.runOnUiThread {
                    if (message != "[]" && message != "0") {
                        button_multiplayerMode_host.isEnabled = true
                        val intent = Intent(this@MultiPlayerModeActivity, WaitingForPlayersActivity::class.java)
                        intent.putExtra("user_id", userId)
                        intent.putExtra("firebase_id", myFirebaseId)
                        startActivity(intent)
                    } else {
                        longToast("Couldn't host now")
                        button_multiplayerMode_host.isEnabled = true
                    }
                }
            }
        })
    }
}

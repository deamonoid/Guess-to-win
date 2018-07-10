package com.constems.ai.guesstowin

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_login.*
import okhttp3.*
import org.jetbrains.anko.longToast
import org.json.JSONArray
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private var tag = LoginActivity::class.java.simpleName
    private var myFirebaseId: String = String()
    private var userId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setSupportActionBar(toolbar_loginActivity)

        button_loginActivity_logIn.setOnClickListener {
            val userName = textInputEditText_loginActivity_userName.text.toString()
            val password = textInputEditText_loginActivity_password.text.toString()
            getPostRequest(userName = userName, password = password)
        }
    }

    private fun getPostRequest(userName: String, password: String) {

        val client = OkHttpClient()
        val body = FormBody.Builder()
                .add("userName", userName)
                .add("password", password)
                .build()

        val request = Request.Builder()
                .url("http://192.168.1.82/game_script/login.php")
                .post(body)
                .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: okhttp3.Call?, e: IOException?) {
                val message = e?.message.toString()
                Log.w(tag, "Failure Response: $message")

                this@LoginActivity.runOnUiThread {
                    longToast("Please check your internet Connection!")
                }
            }

            override fun onResponse(call: okhttp3.Call?, response: Response?) {
                val message = response?.body()?.string()
                Log.d(tag, "Message received $message")
                this@LoginActivity.runOnUiThread {
                    if (message != "[]" && message != "0") {
                        val array = JSONArray(message)
                        for (i in 0 until array.length()) {
                            val temp = array.getJSONObject(i)
                            userId = temp.getString("id").toInt()
                            myFirebaseId = temp.getString("firebase_id")
                        }
                        val intent = Intent(this@LoginActivity, PlayerSelectActivity::class.java)
                        intent.putExtra("user_id", userId)
                        intent.putExtra("firebase_id", myFirebaseId)
                        startActivity(intent)
                        finish()
                    } else {
                        longToast("Login Failed")
                    }
                }
            }
        })
    }
}

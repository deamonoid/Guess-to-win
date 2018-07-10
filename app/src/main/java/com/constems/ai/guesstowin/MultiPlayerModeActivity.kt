package com.constems.ai.guesstowin

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_multiplayer_mode.*
import org.jetbrains.anko.toast

class MultiPlayerModeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multiplayer_mode)

        setSupportActionBar(toolbar_multiplayerMode)

        val builder = intent.extras
        val userId = builder.getInt("user_id", 0)
        val myFirebaseId = builder.getString("firebase_id", "")


        button_multiplayerMode_host.setOnClickListener {
           toast("Hosting will apply soon")
        }

        button_multiplayerMode_join.setOnClickListener{
            val intent = Intent(this@MultiPlayerModeActivity, PlayerSelectActivity::class.java)
            intent.putExtra("user_id", userId)
            intent.putExtra("firebase_id", myFirebaseId)
            startActivity(intent)
        }
    }
}

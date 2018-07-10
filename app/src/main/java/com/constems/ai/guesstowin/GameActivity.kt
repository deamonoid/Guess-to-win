package com.constems.ai.guesstowin

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_game.*

class GameActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        setSupportActionBar(toolbar_gameActivity)

        val builder = intent.extras
        val opponentName = builder.getString("opponent_name", "")
        val myFirebaseId = builder.getString("my_firebase_id", "")
        val playerFirebaseId = builder.getString("player_firebase_id", "")

        textView_game_opponentName.text = opponentName.capitalize()

        Log.v("GameActivity", "FirebaseID: $myFirebaseId, PlayerFirebaseId: $playerFirebaseId")
    }
}

package com.constems.ai.guesstowin

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_player_select.*

class PlayerSelectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_select)

        setSupportActionBar(toolbar_playerSelect)
    }
}

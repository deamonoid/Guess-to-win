package com.constems.ai.guesstowin

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.activity_multiplayer_mode.*
import okhttp3.*
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import java.io.IOException

class MultiPlayerModeActivity : AppCompatActivity() {

    private val tag = MultiPlayerModeActivity::class.java.simpleName
    private var db: FirebaseFirestore? = null
    private var myFirebaseId: String = String()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multiplayer_mode)

        setSupportActionBar(toolbar_multiplayerMode)

        val builder = intent.extras
        myFirebaseId = builder.getString("firebase_id", "")


        button_multiplayerMode_host.setOnClickListener {
            button_multiplayerMode_host.isEnabled = false
            updateStatus()
        }

        button_multiplayerMode_join.setOnClickListener {
            val intent = Intent(this@MultiPlayerModeActivity, PlayerSelectActivity::class.java)
            intent.putExtra("firebase_id", myFirebaseId)
            startActivity(intent)
        }
    }

    private fun updateStatus() {
        db = FirebaseFirestore.getInstance()
        db?.collection("login")?.whereEqualTo("firebase", myFirebaseId)
                ?.get()
                ?.addOnSuccessListener { task ->
                    if (!task.isEmpty) {
                        val documentSnapshot = task.documents
                        val document = documentSnapshot[0]

                        val data = HashMap<String, Any>()
                        data["status"] = true
                        document.reference.set(data, SetOptions.merge())

                        button_multiplayerMode_host.isEnabled = true
                        val intent = Intent(this@MultiPlayerModeActivity, WaitingForPlayersActivity::class.java)
                        intent.putExtra("firebase_id", myFirebaseId)
                        startActivity(intent)
                    } else {
                        toast("Please check your Internet connection")
                        button_multiplayerMode_host.isEnabled = true
                    }
                }
    }
}

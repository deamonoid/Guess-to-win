package com.constems.ai.guesstowin

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.activity_game.*
import okhttp3.*
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import java.io.IOException

class GameActivity : AppCompatActivity() {

    private val tag = GameActivity::class.java.simpleName

    private var listenerRegistration: ListenerRegistration? = null
    private var db: FirebaseFirestore? = null

    private var myFirebaseId: String? = null
    private var playerFirebaseId: String? = null

    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        setSupportActionBar(toolbar_gameActivity)

        val builder = intent.extras
        val opponentName = builder.getString("opponent_name", "")
        myFirebaseId = builder.getString("my_firebase_id", "")
        playerFirebaseId = builder.getString("player_firebase_id", "")
        val isHost = builder.getBoolean("isHost", false)

        textView_game_opponentName.text = opponentName.capitalize()

        startListening()

        if (isHost) {
            button_game_send.isEnabled = true
            textView_game_turn.text = "Your turn"
        }

        button_game_send.setOnClickListener {
            val message = editText_game_sendMessage.text
            editText_game_sendMessage.setText("")
            sendMessage(message.toString())
        }
    }

    private fun sendMessage(message: String) {
        db = FirebaseFirestore.getInstance()

        val data = HashMap<String, Any>()
        data["move"] = message
        db?.collection("users")?.document(myFirebaseId!!)
                ?.set(data, SetOptions.merge())

        button_game_send.isEnabled = false
        textView_game_turn.text = "Their turn"
    }

    private fun startListening() {
        db = FirebaseFirestore.getInstance()

        //Listener starts from here
        val docRef = db?.collection("users")?.document(playerFirebaseId!!)
        listenerRegistration = docRef?.addSnapshotListener { snapshot, firebaseFirestoreException ->
            if (firebaseFirestoreException != null) {
                Log.w(tag, "Listen failed.", firebaseFirestoreException)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val data = snapshot.data!!
                val move = data["move"]

                if (move != "") {
                    textView_game_recieveMessage.text = move.toString()
                    button_game_send.isEnabled = true
                    textView_game_turn.text = "Your turn"
                }

            } else {
                Log.d(tag, "Current data: null")
            }
        }
    }

    private fun removeMoveData() {
        db = FirebaseFirestore.getInstance()

        val data = HashMap<String, Any>()
        data["move"] = ""
        data["opponentId"] = ""
        db?.collection("users")?.document(myFirebaseId!!)
                ?.set(data, SetOptions.merge())
    }

    private fun getPostRequest(firebaseId: String) {
        val client = OkHttpClient()
        val body = FormBody.Builder()
                .add("firebaseId", firebaseId)
                .build()

        val request = Request.Builder()
                .url("http://192.168.1.82/game_script/logout_game.php")
                .post(body)
                .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: okhttp3.Call?, e: IOException?) {
                val message = e?.message.toString()
                Log.w(tag, "Failure Response: $message")

                this@GameActivity.runOnUiThread {
                    longToast("Please check your internet Connection!")
                }
            }

            override fun onResponse(call: okhttp3.Call?, response: Response?) {
                val message = response?.body()?.string()
                Log.d(tag, "Message received $message")
                this@GameActivity.runOnUiThread {
                    if (message != "[]" && message != "0") {
                        listenerRegistration?.remove()
                        removeMoveData()
                        finish()
                    } else {
                        longToast("Error occurred")
                    }
                }
            }
        })
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            getPostRequest(myFirebaseId!!)
        }

        this.doubleBackToExitPressedOnce = true
        toast("Please click BACK again to exit")

        Handler().postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)
    }
}

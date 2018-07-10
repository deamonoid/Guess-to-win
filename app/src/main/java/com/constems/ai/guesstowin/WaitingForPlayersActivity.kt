package com.constems.ai.guesstowin

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.activity_waiting_for_players.*
import okhttp3.*
import java.io.IOException
import com.google.firebase.firestore.FirebaseFirestoreSettings
import org.jetbrains.anko.*
import org.json.JSONArray


class WaitingForPlayersActivity : AppCompatActivity() {

    private val tag = WaitingForPlayersActivity::class.java.simpleName
    private var userId: Int = 0
    private var myFirebaseId: String = String()
    private var listenerRegistration: ListenerRegistration? = null
    private var db: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting_for_players)

        setSupportActionBar(toolbar_waitingForPlayers)

        val builder = intent.extras
        userId = builder.getInt("user_id", 0)
        myFirebaseId = builder.getString("firebase_id", "")

        listenToFireStore()
    }

    private fun listenToFireStore() {
        db = FirebaseFirestore.getInstance()

        //Added because in documentation
        val settings = FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build()
        db?.firestoreSettings = settings

        //Listener starts from here
        val docRef = db?.collection("users")?.document(myFirebaseId)
        listenerRegistration = docRef?.addSnapshotListener { snapshot, firebaseFirestoreException ->
            if (firebaseFirestoreException != null) {
                Log.w(tag, "Listen failed.", firebaseFirestoreException)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val timestamp = snapshot.getTimestamp("created_at")
                val date = timestamp?.toDate()
                Log.d(tag, "Current date: $date")

                val data = snapshot.data!!
                val opponentId = data["opponentId"]

                if (opponentId != "") {
                    getPostPlayerNameRequest(opponentId.toString())
                }

            } else {
                Log.d(tag, "Current data: null")
            }
        }
    }

    private fun removeOpponentId() {
        db = FirebaseFirestore.getInstance()

        val data = HashMap<String, Any>()
        data["opponentId"] = ""
        db?.collection("users")?.document(myFirebaseId)
                ?.set(data, SetOptions.merge())
    }

    private fun getPostPlayerNameRequest(firebaseId: String) {
        val client = OkHttpClient()
        val body = FormBody.Builder()
                .add("firebaseId", firebaseId)
                .build()

        val request = Request.Builder()
                .url("http://192.168.1.82/game_script/get_name.php")
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
                        val array = JSONArray(message)
                        var opponentName:String? = null
                        for (i in 0 until array.length()) {
                            val temp = array.getJSONObject(i)
                            opponentName = temp.getString("user_name")
                        }
                        val intent = Intent(this@WaitingForPlayersActivity, GameActivity::class.java)
                        intent.putExtra("my_firebase_id", myFirebaseId)
                        intent.putExtra("player_firebase_id", firebaseId)
                        intent.putExtra("opponent_name", opponentName)
                        intent.putExtra("isHost", true)
                        startActivity(intent)
                        finish()
                    } else {
                        longToast("Error occurred")

                    }
                }
            }
        })
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
                        removeOpponentId()
                        listenerRegistration?.remove()
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

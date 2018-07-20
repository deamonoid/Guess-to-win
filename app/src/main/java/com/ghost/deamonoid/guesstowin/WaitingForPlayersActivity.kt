package com.ghost.deamonoid.guesstowin

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.activity_waiting_for_players.*
import org.jetbrains.anko.*


class WaitingForPlayersActivity : AppCompatActivity() {

    private val tag = WaitingForPlayersActivity::class.java.simpleName
    private var myFirebaseId: String = String()
    private var opponentName: String = String()
    private var listenerRegistration: ListenerRegistration? = null
    private var db: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting_for_players)

        setSupportActionBar(toolbar_waitingForPlayers)

        val builder = intent.extras
        myFirebaseId = builder.getString("firebase_id", "")

        listenToFireStore()
    }

    private fun listenToFireStore() {
        db = FirebaseFirestore.getInstance()

        //Listener starts from here
        val docRef = db?.collection("users")?.document(myFirebaseId)
        listenerRegistration = docRef?.addSnapshotListener { snapshot, firebaseFirestoreException ->
            if (firebaseFirestoreException != null) {
                Log.w(tag, "Listen failed.", firebaseFirestoreException)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val data = snapshot.data!!
                val opponentId = data["opponentId"]

                if (opponentId != "") {
                    val query = db!!.collection("login").whereEqualTo("firebase", opponentId)
                    query.get().addOnSuccessListener { querySnapshot ->
                        querySnapshot.forEach {
                            opponentName = it.getString("name")!!
                        }
                        startGame(opponentId.toString())
                    }
                }

            } else {
                Log.d(tag, "Current data: null")
            }
        }
    }

    private fun startGame(opponentId: String) {
        listenerRegistration?.remove()
        val intent = Intent(this@WaitingForPlayersActivity, GameActivity::class.java)
        intent.putExtra("my_firebase_id", myFirebaseId)
        intent.putExtra("player_firebase_id", opponentId)
        intent.putExtra("opponent_name", opponentName)
        intent.putExtra("isHost", true)
        startActivity(intent)
        finish()
    }

    private fun removeOpponentId() {
        db = FirebaseFirestore.getInstance()

        val data = HashMap<String, Any>()
        data["opponentId"] = ""
        db?.collection("users")?.document(myFirebaseId)
                ?.set(data, SetOptions.merge())
    }

    private fun removeStatus() {
        db = FirebaseFirestore.getInstance()
        db?.collection("login")?.whereEqualTo("firebase", myFirebaseId)
                ?.get()?.addOnSuccessListener { task ->
                    if (!task.isEmpty) {
                        val documentSnapshot = task.documents
                        val document = documentSnapshot[0]

                        val data = HashMap<String, Any>()
                        data["status"] = false
                        document.reference.set(data, SetOptions.merge())

                        listenerRegistration?.remove()
                        removeOpponentId()
                        finish()
                    } else {
                        toast("Please check your Internet connection")
                    }
                }
    }

    override fun onBackPressed() {
        alert("Do you want to stop hosting?") {
            yesButton {
                removeStatus()
            }
            noButton { }
        }.show()
    }
}

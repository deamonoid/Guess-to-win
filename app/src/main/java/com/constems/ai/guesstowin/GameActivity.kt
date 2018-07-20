package com.constems.ai.guesstowin

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.activity_game.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import org.jetbrains.anko.yesButton

class GameActivity : AppCompatActivity() {

    private val tag = GameActivity::class.java.simpleName

    private var listenerRegistration: ListenerRegistration? = null
    private var db: FirebaseFirestore? = null

    private var myFirebaseId: String? = null
    private var playerFirebaseId: String? = null

    private var doubleBackToExitPressedOnce = false

    private var isHost = false

    private var targetBox: String? = null
    private var opponentTargetBox: String? = null

    private var targetMode = true
    private var targetAcquired = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        setSupportActionBar(toolbar_gameActivity)

        val builder = intent.extras
        val opponentName = builder.getString("opponent_name", "")
        myFirebaseId = builder.getString("my_firebase_id", "")
        playerFirebaseId = builder.getString("player_firebase_id", "")
        isHost = builder.getBoolean("isHost", false)

        textView_game_opponentName.text = opponentName.capitalize()

        initializeAllOnClickListener()
        setTargetBox()
    }

    private fun setTargetBox() {
        alert("Select a box in 'Select the area' grid to set a target") {
            yesButton { }
        }.show()
    }

    private fun initializeAllOnClickListener() {
        x0x0x.setOnClickListener {
            sendTargetOrMove(it)
        }
        x0x1x.setOnClickListener {
            sendTargetOrMove(it)
        }
        x0x2x.setOnClickListener {
            sendTargetOrMove(it)
        }
        x1x0x.setOnClickListener {
            sendTargetOrMove(it)
        }
        x1x1x.setOnClickListener {
            sendTargetOrMove(it)
        }
        x1x2x.setOnClickListener {
            sendTargetOrMove(it)
        }
        x2x0x.setOnClickListener {
            sendTargetOrMove(it)
        }
        x2x1x.setOnClickListener {
            sendTargetOrMove(it)
        }
        x2x2x.setOnClickListener {
            sendTargetOrMove(it)
        }
    }

    private fun sendTargetOrMove(it: View) {
        if (targetMode) {
            sendTarget(it.tag.toString())
            targetBox = it.tag.toString()
            val chosenBox = selectPlayerImageView(targetBox!!)
            chosenBox.setImageResource(R.drawable.ic_target_icon)
            startListening()
            targetMode = false
            disableGridTouch()
        } else {
            sendMove(it.tag.toString())
            val selectedBox = it.tag.toString()
            val chosenBox = selectPlayerImageView(selectedBox)
            if (it.tag.toString() == opponentTargetBox) {
                chosenBox.setImageResource(R.drawable.ic_hit_icon)
                longToast("You WON!!")
            } else {
                chosenBox.setImageResource(R.drawable.ic_miss_icon)
            }
        }
    }

    private fun sendTarget(message: String) {
        db = FirebaseFirestore.getInstance()

        val data = HashMap<String, Any>()
        data["target"] = message
        db?.collection("users")?.document(myFirebaseId!!)
                ?.set(data, SetOptions.merge())
    }

    private fun sendMove(message: String) {
        db = FirebaseFirestore.getInstance()

        val data = HashMap<String, Any>()
        data["move"] = message
        db?.collection("users")?.document(myFirebaseId!!)
                ?.set(data, SetOptions.merge())

        disableGridTouch()
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
                val targetData = data["target"]
                val move = data["move"]

                if (targetData != "") {
                    val message = targetData.toString()
                    opponentTargetBox = message
                    targetAcquired = true
                    firstTurn()
                }

                if (move != "") {
                    val message = move.toString()
                    val chosenBox = selectOpponentImageView(message)

                    if (targetBox == message) {
                        chosenBox.setImageResource(R.drawable.ic_hit_icon)
                        longToast("GAME OVER")
                    } else {
                        chosenBox.setImageResource(R.drawable.ic_miss_icon)
                        enableGridTouch()
                    }
                }
            } else {
                Log.d(tag, "Current data: null")
            }
        }
    }

    private fun firstTurn() {
        if (isHost) {
            enableGridTouch()
        } else {
            disableGridTouch()
        }
    }

    private fun selectPlayerImageView(resIdName: String): ImageView {
        return when (resIdName) {
            "x0x0x" -> w_x0x0x
            "x0x1x" -> w_x0x1x
            "x0x2x" -> w_x0x2x
            "x1x0x" -> w_x1x0x
            "x1x1x" -> w_x1x1x
            "x1x2x" -> w_x1x2x
            "x2x0x" -> w_x2x0x
            "x2x1x" -> w_x2x1x
            else -> {
                w_x2x2x
            }
        }
    }

    private fun selectOpponentImageView(resIdName: String): ImageView {
        return when (resIdName) {
            "x0x0x" -> i_x0x0x
            "x0x1x" -> i_x0x1x
            "x0x2x" -> i_x0x2x
            "x1x0x" -> i_x1x0x
            "x1x1x" -> i_x1x1x
            "x1x2x" -> i_x1x2x
            "x2x0x" -> i_x2x0x
            "x2x1x" -> i_x2x1x
            else -> {
                i_x2x2x
            }
        }
    }

    private fun removeMoveData() {
        db = FirebaseFirestore.getInstance()

        val data = HashMap<String, Any>()
        data["move"] = ""
        data["opponentId"] = ""
        data["target"] = ""
        db?.collection("users")?.document(myFirebaseId!!)
                ?.set(data, SetOptions.merge())
    }

    private fun exitGame() {
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
                        removeMoveData()
                        finish()
                    } else {
                        toast("Please check your Internet connection")
                    }
                }
    }

    private fun disableGridTouch() {
        frameLayout_game_mask.visibility = View.VISIBLE
    }

    private fun enableGridTouch() {
        frameLayout_game_mask.visibility = View.GONE
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            exitGame()
        }

        this@GameActivity.doubleBackToExitPressedOnce = true
        toast("Please click BACK again to exit")

        Handler().postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)
    }
}

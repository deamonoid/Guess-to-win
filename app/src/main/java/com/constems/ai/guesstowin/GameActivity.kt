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
import okhttp3.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import org.jetbrains.anko.yesButton
import java.io.IOException

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
        startListening()
        setTargetBox()
    }

    private fun setTargetBox() {
        alert("Select a box in 'Select the area' grid to set a target") {
            yesButton { }
        }
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
            val resId = baseContext.resources.getIdentifier("w_$targetBox", "layout", baseContext.packageName)
            val chosenBox = findViewById<ImageView>(resId)
            chosenBox.setImageResource(R.drawable.ic_target_icon)
            targetMode = false
            disableGridTouch()
        } else {
            sendMove(it.tag.toString())
            val selectedBox = it.tag.toString()
            val resId = baseContext.resources.getIdentifier("w_$selectedBox", "layout", baseContext.packageName)
            val chosenBox = findViewById<ImageView>(resId)
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
                    val resId = baseContext.resources.getIdentifier("i_$message", "layout", baseContext.packageName)
                    val chosenBox = findViewById<ImageView>(resId)

                    if (targetBox == message) {
                        chosenBox.setImageResource(R.drawable.ic_hit_icon)
                        longToast("GAME OVER")
                    } else {
                        chosenBox.setImageResource(R.drawable.ic_miss_icon)
                        enableGridTouch()
                        textView_game_turn.text = "Your turn"
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
            textView_game_turn.text = "Your turn"
        } else {
            disableGridTouch()
            textView_game_turn.text = "Their turn"
        }
    }

    private fun removeMoveData() {
        db = FirebaseFirestore.getInstance()

        val data = HashMap<String, Any>()
        data["move"] = ""
        data["opponentId"] = ""
        data["target"]
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

    private fun disableGridTouch() {
        frameLayout_game_mask.visibility = View.VISIBLE
    }

    private fun enableGridTouch() {
        frameLayout_game_mask.visibility = View.GONE
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            getPostRequest(myFirebaseId!!)
        }

        this@GameActivity.doubleBackToExitPressedOnce = true
        toast("Please click BACK again to exit")

        Handler().postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)
    }
}

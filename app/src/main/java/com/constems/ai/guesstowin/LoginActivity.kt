package com.constems.ai.guesstowin

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_login.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import org.jetbrains.anko.toast


class LoginActivity : AppCompatActivity() {

    private var tag = LoginActivity::class.java.simpleName
    private var myFirebaseId: String = String()
    private var db: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setSupportActionBar(toolbar_loginActivity)

        db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false)
                .build()
        db!!.firestoreSettings = settings

        button_loginActivity_logIn.setOnClickListener {
            val userName = textInputEditText_loginActivity_userName.text.toString()
            val password = textInputEditText_loginActivity_password.text.toString()
            getUserAndPassword(userName = userName, password = password)
            button_loginActivity_logIn.isEnabled = false
        }
    }

    private fun getUserAndPassword(userName: String, password: String) {
        val docRef = db?.collection("login")?.document(userName)
        docRef?.get()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document.exists()) {
                    val data = document.data!!
                    val pass = data["password"].toString()
                    if (pass == password) {
                        myFirebaseId = data["firebase"].toString()
                        val intent = Intent(this@LoginActivity, MultiPlayerModeActivity::class.java)
                        intent.putExtra("firebase_id", myFirebaseId)
                        startActivity(intent)
                        finish()
                    } else {
                        toast("Wrong Password")
                        button_loginActivity_logIn.isEnabled = true
                    }
                } else {
                    Log.d(tag, "No such document")
                    toast("No user found")
                    button_loginActivity_logIn.isEnabled = true
                }
            } else {
                Log.d(tag, "get failed with ", task.exception)
                toast("${task.exception}")
                button_loginActivity_logIn.isEnabled = true
            }
        }
    }
}

package com.ghost.deamonoid.guesstowin

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_player_select.*
import org.jetbrains.anko.*
import com.google.firebase.firestore.SetOptions


class PlayerSelectActivity : AppCompatActivity() {

    private var playerArrayList: ArrayList<String> = ArrayList()
    private var playerFirebaseId: ArrayList<String> = ArrayList()

    private var db: FirebaseFirestore? = null

    private var mAdapter: ListAdapter? = null
    private var myFirebaseId: String = String()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_select)

        setSupportActionBar(toolbar_playerSelect)

        val builder = intent.extras
        myFirebaseId = builder.getString("firebase_id", "")
        getPlayerList()

        swipeRefreshLayout_playerSelect.setOnRefreshListener {
            getPlayerList()
        }

        val layoutManager = LinearLayoutManager(this@PlayerSelectActivity)
        recyclerView_playerSelect.layoutManager = layoutManager
        mAdapter = ListAdapter(playerArrayList)
        recyclerView_playerSelect.adapter = mAdapter
    }

    inner class ListAdapter(private val playerList: ArrayList<String>) : RecyclerView.Adapter<ListAdapter.MyViewHolder>() {
        inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val playerTextView: TextView = view.findViewById(R.id.textView_recyclerComponent_activeOpponentName)
            val playerListItem: ConstraintLayout = view.findViewById(R.id.constraintLayout_recyclerComponent)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.recyclerview_component_opponent, parent, false)
            return MyViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            holder.playerTextView.text = playerList[position]
            holder.playerListItem.setOnClickListener {
                val db = FirebaseFirestore.getInstance()

                val data = HashMap<String, Any>()
                data["opponentId"] = myFirebaseId
                db.collection("users").document(playerFirebaseId[position])
                        .set(data, SetOptions.merge())

                val intent = Intent(this@PlayerSelectActivity, GameActivity::class.java)
                intent.putExtra("my_firebase_id", myFirebaseId)
                intent.putExtra("player_firebase_id", playerFirebaseId[position])
                intent.putExtra("opponent_name", playerList[position])
                intent.putExtra("isHost", false)
                startActivity(intent)
                finish()
            }
        }

        override fun getItemCount(): Int {
            return playerList.size
        }
    }

    private fun getPlayerList() {
        db = FirebaseFirestore.getInstance()
        val query = db!!.collection("login").whereEqualTo("status", true)
        query.get().addOnSuccessListener { querySnapshot ->
            playerArrayList.clear()
            if (!querySnapshot.isEmpty) {
                querySnapshot.forEach {
                    playerArrayList.add(it.getString("name")!!)
                    playerFirebaseId.add(it.getString("firebase")!!)
                }
                mAdapter!!.notifyDataSetChanged()
                swipeRefreshLayout_playerSelect.isRefreshing = false
            } else {
                toast("No player online")
                swipeRefreshLayout_playerSelect.isRefreshing = false
            }
        }
    }
}

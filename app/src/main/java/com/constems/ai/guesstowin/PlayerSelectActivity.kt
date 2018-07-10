package com.constems.ai.guesstowin

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_player_select.*
import okhttp3.*
import org.jetbrains.anko.*
import org.json.JSONArray
import java.io.IOException

class PlayerSelectActivity : AppCompatActivity() {

    private var playerArrayList: ArrayList<String> = ArrayList()
    private var playerFirebaseId: ArrayList<String> = ArrayList()

    private var mAdapter: ListAdapter? = null
    private val tag = PlayerSelectActivity::class.java.simpleName
    private var userId: Int = 0
    private var myFirebaseId: String = String()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_select)

        setSupportActionBar(toolbar_playerSelect)

        val builder = intent.extras
        userId = builder.getInt("user_id", 0)
        myFirebaseId = builder.getString("firebase_id", "")
        getPostPlayerListRequest(userId)

        swipeRefreshLayout_playerSelect.setOnRefreshListener {
            getPostPlayerListRequest(userId)
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
                val intent = Intent(this@PlayerSelectActivity, GameActivity::class.java)
                intent.putExtra("my_firebase_id", myFirebaseId)
                intent.putExtra("player_firebase_id", playerFirebaseId[position])
                startActivity(intent)
                finish()
            }
        }

        override fun getItemCount(): Int {
            return playerList.size
        }
    }

    private fun getPostPlayerListRequest(userId: Int) {
        val client = OkHttpClient()
        val body = FormBody.Builder()
                .add("userId", userId.toString())
                .build()

        val request = Request.Builder()
                .url("http://192.168.1.82/game_script/list_player.php")
                .post(body)
                .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: okhttp3.Call?, e: IOException?) {
                val message = e?.message.toString()
                Log.w(tag, "Failure Response: $message")

                this@PlayerSelectActivity.runOnUiThread {
                    longToast("Please check your internet Connection!")
                    swipeRefreshLayout_playerSelect.isRefreshing = false
                }
            }

            override fun onResponse(call: okhttp3.Call?, response: Response?) {
                val message = response?.body()?.string()
                Log.d(tag, "Message received $message")
                this@PlayerSelectActivity.runOnUiThread {
                    if (message != "[]" && message != "0") {
                        playerArrayList.clear()
                        val array = JSONArray(message)
                        for (i in 0 until array.length()) {
                            val temp = array.getJSONObject(i)
                            playerArrayList.add(temp.getString("user_name").capitalize())
                            playerFirebaseId.add(temp.getString("firebase_id"))
                        }
                        Log.v(tag, "PlayerList: " + playerArrayList.size)
                        mAdapter!!.notifyDataSetChanged()
                        swipeRefreshLayout_playerSelect.isRefreshing = false
                    } else {
                        longToast("No player online")
                        swipeRefreshLayout_playerSelect.isRefreshing = false
                    }
                }
            }
        })
    }
}

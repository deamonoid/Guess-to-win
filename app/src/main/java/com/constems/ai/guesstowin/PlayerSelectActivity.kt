package com.constems.ai.guesstowin

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
import org.jetbrains.anko.alert
import org.jetbrains.anko.longToast
import org.jetbrains.anko.noButton
import org.jetbrains.anko.yesButton
import org.json.JSONArray
import java.io.IOException

class PlayerSelectActivity : AppCompatActivity() {

    private var playerArrayList: ArrayList<String> = ArrayList()

    private var mAdapter: ListAdapter? = null
    private val tag = PlayerSelectActivity::class.java.simpleName
    private var userName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_select)

        setSupportActionBar(toolbar_playerSelect)

        val builder = intent.extras
        userName = builder.getString("username", "")
        getPostPLayerListRequest(userName!!)

        swipeRefreshLayout_playerSelect.setOnRefreshListener {
            getPostPLayerListRequest(userName!!)
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
            holder.playerListItem.setOnClickListener {}
        }

        override fun getItemCount(): Int {
            return playerList.size
        }
    }

    private fun getPostPLayerListRequest(userName: String) {
        val client = OkHttpClient()
        val body = FormBody.Builder()
                .add("userName", userName)
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
                        }
                        Log.v(tag, "PlayerList: " + playerArrayList.size)
                        mAdapter!!.notifyDataSetChanged()
                        swipeRefreshLayout_playerSelect.isRefreshing = false
                    } else {
                        longToast("No player online")
                    }
                }
            }
        })
    }

    private fun getPostRequest(userName: String) {
        val client = OkHttpClient()
        val body = FormBody.Builder()
                .add("userName", userName)
                .build()

        val request = Request.Builder()
                .url("http://192.168.1.82/game_script/logout.php")
                .post(body)
                .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: okhttp3.Call?, e: IOException?) {
                val message = e?.message.toString()
                Log.w(tag, "Failure Response: $message")

                this@PlayerSelectActivity.runOnUiThread {
                    longToast("Please check your internet Connection!")
                }
            }

            override fun onResponse(call: okhttp3.Call?, response: Response?) {
                val message = response?.body()?.string()
                Log.d(tag, "Message received $message")
                this@PlayerSelectActivity.runOnUiThread {
                    if (message != "[]" && message != "0") {
                        finish()
                    } else {
                        longToast("Error occurred")
                    }
                }
            }
        })
    }

    override fun onBackPressed() {
        alert("Do you want to Logout?") {
            yesButton {
                getPostRequest(userName!!)
            }
            noButton { }
        }.show()
    }
}

package com.example.assignment1

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonParser

class FollowListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FollowListAdapter
    private lateinit var titleText: TextView
    private lateinit var sessionManager: SessionManager
    private val userList = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follow_list)

        sessionManager = SessionManager(this)
        recyclerView = findViewById(R.id.followRecyclerView)
        titleText = findViewById(R.id.titleText)
        adapter = FollowListAdapter(userList, sessionManager.getUserId())

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        findViewById<ImageView>(R.id.backButton).setOnClickListener { finish() }

        val type = intent.getStringExtra("type") ?: "followers"
        val userId = intent.getIntExtra("user_id", 0)

        titleText.text = when (type) {
            "followers" -> "Followers"
            "following" -> "Following"
            else -> "Followers"
        }

        if (userId > 0) {
            if (type == "followers") {
                loadFollowers(userId)
            } else {
                loadFollowing(userId)
            }
        }
    }

    private fun loadFollowers(userId: Int) {
        ApiService.getFollowers(userId, object : ApiService.ApiCallback {
            override fun onSuccess(response: String) {
                runOnUiThread {
                    try {
                        val jsonObject = JsonParser.parseString(response).asJsonObject
                        if (jsonObject.get("status")?.asString == "success") {
                            val followersArray = jsonObject.getAsJsonArray("followers")
                            userList.clear()

                            followersArray?.forEach { element ->
                                val userJson = element.asJsonObject
                                val user = User(
                                    uid = userJson.get("id")?.asInt?.toString() ?: "",
                                    username = userJson.get("username")?.asString ?: "",
                                    fullName = "${userJson.get("first_name")?.asString ?: ""} ${userJson.get("last_name")?.asString ?: ""}".trim(),
                                    profileImage = userJson.get("profile_photo_url")?.asString
                                )
                                userList.add(user)
                            }

                            adapter.notifyDataSetChanged()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onError(error: String) {
                // Handle error silently
            }
        })
    }

    private fun loadFollowing(userId: Int) {
        ApiService.getFollowing(userId, object : ApiService.ApiCallback {
            override fun onSuccess(response: String) {
                runOnUiThread {
                    try {
                        val jsonObject = JsonParser.parseString(response).asJsonObject
                        if (jsonObject.get("status")?.asString == "success") {
                            val followingArray = jsonObject.getAsJsonArray("following")
                            userList.clear()

                            followingArray?.forEach { element ->
                                val userJson = element.asJsonObject
                                val user = User(
                                    uid = userJson.get("id")?.asInt?.toString() ?: "",
                                    username = userJson.get("username")?.asString ?: "",
                                    fullName = "${userJson.get("first_name")?.asString ?: ""} ${userJson.get("last_name")?.asString ?: ""}".trim(),
                                    profileImage = userJson.get("profile_photo_url")?.asString
                                )
                                userList.add(user)
                            }

                            adapter.notifyDataSetChanged()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onError(error: String) {
                // Handle error silently
            }
        })
    }
}

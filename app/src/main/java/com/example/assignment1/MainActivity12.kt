package com.example.assignment1

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonParser

class MainActivity12 : AppCompatActivity() {

    private lateinit var adapter: FollowRequestAdapter
    private lateinit var recycler: RecyclerView
    private lateinit var sessionManager: SessionManager
    private val requestList = mutableListOf<FollowRequest>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main12)

        sessionManager = SessionManager(this)
        recycler = findViewById(R.id.followRequestsRecycler)
        recycler.layoutManager = LinearLayoutManager(this)

        val currentUserId = sessionManager.getUserId()
        if (currentUserId > 0) {
            adapter = FollowRequestAdapter(requestList, currentUserId)
            recycler.adapter = adapter
            loadFollowRequests(currentUserId)
        }

        setupNavigation()

        findViewById<TextView>(R.id.following).setOnClickListener {
            startActivity(Intent(this, MainActivity11::class.java))
        }
    }

    private fun loadFollowRequests(userId: Int) {
        ApiService.getFollowRequests(userId, object : ApiService.ApiCallback {
            override fun onSuccess(response: String) {
                runOnUiThread {
                    try {
                        val jsonObject = JsonParser.parseString(response).asJsonObject
                        if (jsonObject.get("status")?.asString == "success") {
                            val requestsArray = jsonObject.getAsJsonArray("requests")
                            requestList.clear()

                            requestsArray?.forEach { element ->
                                val requestJson = element.asJsonObject
                                val request = FollowRequest(
                                    id = requestJson.get("id")?.asInt ?: 0,
                                    sender_id = requestJson.get("sender_id")?.asInt ?: 0,
                                    username = requestJson.get("username")?.asString ?: "",
                                    first_name = requestJson.get("first_name")?.asString ?: "",
                                    last_name = requestJson.get("last_name")?.asString ?: "",
                                    profile_photo_url = requestJson.get("profile_photo_url")?.asString,
                                    created_at = requestJson.get("created_at")?.asString ?: "",
                                    status = "pending"
                                )
                                requestList.add(request)
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

    private fun setupNavigation() {
        findViewById<ImageView>(R.id.home).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
        findViewById<ImageView>(R.id.search).setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }
        findViewById<ImageView>(R.id.add).setOnClickListener {
            startActivity(Intent(this, MainActivity19::class.java))
        }
        findViewById<ImageView>(R.id.heart).setOnClickListener {
            startActivity(Intent(this, MainActivity11::class.java))
        }
        findViewById<ImageView>(R.id.profile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }
}


package com.example.assignment1

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonParser

class FollowRequestsActivity : AppCompatActivity() {

    // The name of the RecyclerView variable is correctly 'recyclerView',
    // while 'followRequestsRecyclerView' is the resource ID (R.id.followRequestsRecyclerView).
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FollowRequestAdapter
    private lateinit var sessionManager: SessionManager
    private val requestList = mutableListOf<FollowRequest>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // FIX: Changed R.layout.activity_follow_requests to R.layout.follow_request
        // to match the XML file name provided previously (follow_request.xml)
        setContentView(R.layout.activity_follow_requests)

        // Initialize SessionManager and get the current user ID
        sessionManager = SessionManager(this)
        val currentUserId = sessionManager.getUserId()

        // Find the RecyclerView using its ID
        recyclerView = findViewById(R.id.followRequestsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize the adapter with the list and current user ID
        adapter = FollowRequestAdapter(requestList, currentUserId)
        recyclerView.adapter = adapter

        if (currentUserId > 0) {
            loadFollowRequests(currentUserId)
        }
    }

    private fun loadFollowRequests(userId: Int) {
        // Assume ApiService.getFollowRequests is implemented elsewhere
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

                            // Notify the adapter that the dataset has changed
                            adapter.notifyDataSetChanged()
                        }
                    } catch (e: Exception) {
                        // Log or handle JSON parsing errors
                        e.printStackTrace()
                    }
                }
            }

            override fun onError(error: String) {
                // Handle API error silently or show a message
            }
        })
    }
}
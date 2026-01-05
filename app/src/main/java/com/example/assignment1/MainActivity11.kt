package com.example.assignment1

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonParser

class MainActivity11 : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var followingRecyclerView: RecyclerView
    private lateinit var youRecyclerView: RecyclerView
    private lateinit var followingAdapter: FollowListAdapter
    private lateinit var youAdapter: FollowRequestAdapter
    private lateinit var followingTab: TextView
    private lateinit var youTab: TextView
    private lateinit var feedContainer: View
    
    private val followingList = mutableListOf<User>()
    private val requestList = mutableListOf<FollowRequest>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main11)

        sessionManager = SessionManager(this)
        val currentUserId = sessionManager.getUserId()

        // Find views
        followingTab = findViewById(R.id.following)
        youTab = findViewById(R.id.you)
        feedContainer = findViewById(R.id.feedContainer)

        // Create RecyclerViews programmatically
        if (currentUserId > 0) {
            setupRecyclerViews(currentUserId)
            
            // Setup tab switching
            setupTabs()
            
            // Show "Following" tab by default
            showFollowingTab()
            
            // Load initial data
            loadFollowing(currentUserId)
            loadFollowRequests(currentUserId)
        }

        setupNavigation()
    }

    private fun setupRecyclerViews(currentUserId: Int) {
        // Get the container and remove all existing views
        val container = feedContainer as? android.view.ViewGroup
        container?.removeAllViews()
        
        // Create RecyclerView for Following
        followingRecyclerView = RecyclerView(this)
        followingRecyclerView.id = View.generateViewId()
        followingRecyclerView.layoutManager = LinearLayoutManager(this)
        followingAdapter = FollowListAdapter(followingList, currentUserId)
        followingRecyclerView.adapter = followingAdapter
        
        val followingParams = android.widget.FrameLayout.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT
        )
        followingRecyclerView.layoutParams = followingParams
        followingRecyclerView.visibility = View.VISIBLE

        // Create RecyclerView for You (Follow Requests)
        youRecyclerView = RecyclerView(this)
        youRecyclerView.id = View.generateViewId()
        youRecyclerView.layoutManager = LinearLayoutManager(this)
        youAdapter = FollowRequestAdapter(requestList, currentUserId)
        youRecyclerView.adapter = youAdapter
        
        val youParams = android.widget.FrameLayout.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT
        )
        youRecyclerView.layoutParams = youParams
        youRecyclerView.visibility = View.GONE

        // Add RecyclerViews to the container
        container?.addView(followingRecyclerView)
        container?.addView(youRecyclerView)
    }

    private fun setupTabs() {
        followingTab.setOnClickListener {
            showFollowingTab()
        }

        youTab.setOnClickListener {
            showYouTab()
        }
    }

    private fun showFollowingTab() {
        if (!::followingRecyclerView.isInitialized || !::youRecyclerView.isInitialized) {
            return
        }
        
        followingTab.setTextColor(getColor(android.R.color.black))
        followingTab.setTypeface(null, android.graphics.Typeface.BOLD)
        youTab.setTextColor(getColor(android.R.color.darker_gray))
        youTab.setTypeface(null, android.graphics.Typeface.NORMAL)
        followingRecyclerView.visibility = View.VISIBLE
        youRecyclerView.visibility = View.GONE
        
        // Update indicator
        findViewById<View>(R.id.followingIndicator)?.setBackgroundColor(getColor(android.R.color.holo_orange_dark))
        findViewById<View>(R.id.youIndicator)?.setBackgroundColor(getColor(android.R.color.darker_gray))
    }

    private fun showYouTab() {
        if (!::followingRecyclerView.isInitialized || !::youRecyclerView.isInitialized) {
            return
        }
        
        youTab.setTextColor(getColor(android.R.color.black))
        youTab.setTypeface(null, android.graphics.Typeface.BOLD)
        followingTab.setTextColor(getColor(android.R.color.darker_gray))
        followingTab.setTypeface(null, android.graphics.Typeface.NORMAL)
        youRecyclerView.visibility = View.VISIBLE
        followingRecyclerView.visibility = View.GONE
        
        // Update indicator
        findViewById<View>(R.id.followingIndicator)?.setBackgroundColor(getColor(android.R.color.darker_gray))
        findViewById<View>(R.id.youIndicator)?.setBackgroundColor(getColor(android.R.color.holo_orange_dark))
    }

    private fun loadFollowing(userId: Int) {
        ApiService.getFollowing(userId, object : ApiService.ApiCallback {
            override fun onSuccess(response: String) {
                runOnUiThread {
                    try {
                        val jsonObject = JsonParser.parseString(response).asJsonObject
                        if (jsonObject.get("status")?.asString == "success") {
                            val followingArray = jsonObject.getAsJsonArray("following")
                            followingList.clear()

                            followingArray?.forEach { element ->
                                val userJson = element.asJsonObject
                                val userId = userJson.get("id")?.asInt ?: 0
                                if (userId > 0) {
                                    val user = User(
                                        uid = userId.toString(),
                                        username = userJson.get("username")?.asString ?: "",
                                        fullName = "${userJson.get("first_name")?.asString ?: ""} ${userJson.get("last_name")?.asString ?: ""}".trim(),
                                        profileImage = userJson.get("profile_photo_url")?.asString
                                    )
                                    followingList.add(user)
                                }
                            }

                            if (::followingAdapter.isInitialized) {
                                followingAdapter.notifyDataSetChanged()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onError(error: String) {
                runOnUiThread {
                    android.util.Log.e("MainActivity11", "Error loading following: $error")
                }
            }
        })
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
                                val requestId = requestJson.get("id")?.asInt ?: 0
                                val senderId = requestJson.get("sender_id")?.asInt ?: 0
                                if (requestId > 0 && senderId > 0) {
                                    val request = FollowRequest(
                                        id = requestId,
                                        sender_id = senderId,
                                        username = requestJson.get("username")?.asString ?: "",
                                        first_name = requestJson.get("first_name")?.asString ?: "",
                                        last_name = requestJson.get("last_name")?.asString ?: "",
                                        profile_photo_url = requestJson.get("profile_photo_url")?.asString,
                                        created_at = requestJson.get("created_at")?.asString ?: "",
                                        status = "pending"
                                    )
                                    requestList.add(request)
                                }
                            }

                            if (::youAdapter.isInitialized) {
                                youAdapter.notifyDataSetChanged()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onError(error: String) {
                runOnUiThread {
                    android.util.Log.e("MainActivity11", "Error loading follow requests: $error")
                }
            }
        })
    }

    private fun setupNavigation() {
        val home = findViewById<ImageView>(R.id.home)
        val search = findViewById<ImageView>(R.id.search)
        val add = findViewById<ImageView>(R.id.add)
        val heart = findViewById<ImageView>(R.id.heart)
        val profile = findViewById<ImageView>(R.id.profile)

        home.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        search.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        add.setOnClickListener {
            startActivity(Intent(this, MainActivity19::class.java))
        }

        heart.setOnClickListener {
            startActivity(Intent(this, MainActivity11::class.java))
        }

        profile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }
}

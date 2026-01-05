package com.example.assignment1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.assignment1.databinding.ActivityHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.JsonParser
import java.io.ByteArrayOutputStream

// NOTE: You must ensure the following classes exist in your project:
// SessionManager, ApiService, StoryAdapter, PostAdapter,
// Post, Story, userStory, SearchActivity, AddPostActivity, MainActivity11,
// ProfileActivity, MainActivity9, StoryViewerActivity

class HomeActivity : AppCompatActivity() {
    private val binding: ActivityHomeBinding by lazy{
        ActivityHomeBinding.inflate(layoutInflater)
    }

    private lateinit var storyAdapter: StoryAdapter
    private val userStories = mutableListOf<userStory>()

    private lateinit var postAdapter: PostAdapter
    private val postList = mutableListOf<Post>()

    private lateinit var sessionManager: SessionManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // ** 1. CRITICAL CHECK: Redirect to Login if session is invalid **
        if (!sessionManager.isLoggedIn()) {
            Log.d("HomeActivity", "Session invalid. Redirecting to Login.")
            val intent = Intent(this, LoginActivity::class.java)
            // Clear the activity stack to prevent navigating back here
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        Log.d("HomeActivity", "onCreate called. User is logged in.")

        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            val userId = sessionManager.getUserId()
            if (userId != -1) {
                // Handle FCM token storage if needed
            }
        }

        // ************************************************************
        // ** THE FIX: camera_brown ICON IS NOW THE EXPLICIT LOGOUT BUTTON **
        // ************************************************************
        binding.cameraBrown.setOnClickListener {
            Log.d("HomeActivity", "Camera/Logout icon tapped. Initiating explicit logout.")

            // 1. Clear session data
            sessionManager.logout()

            // 2. Clear Firebase Auth session (if applicable)
            FirebaseAuth.getInstance().signOut()

            // 3. Redirect to Login screen and clear the activity stack
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
        // ************************************************************

        // Bottom Navigation (Links to other activities)
        binding.home.setOnClickListener {
            loadStories()
        }
        binding.search.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }
        binding.add.setOnClickListener {
            startActivity(Intent(this, AddPostActivity::class.java))
        }
        binding.heart.setOnClickListener {
            startActivity(Intent(this, MainActivity11::class.java))
        }
        binding.profile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        binding.sharebrown.setOnClickListener {
            // Open conversation list to see all messages
            startActivity(Intent(this, ChatListActivity::class.java))
        }

        // RecyclerView setup for Stories
        binding.storyRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        storyAdapter = StoryAdapter(this, userStories) { userStory ->
            openStoryViewer(userStory)
        }
        binding.storyRecyclerView.adapter = storyAdapter
        
        // Load all stories from database (all users)
        loadStories()

        binding.postRecyclerView.setHasFixedSize(true)
        binding.postRecyclerView.layoutManager = LinearLayoutManager(this)
        postAdapter = PostAdapter(this, postList)
        binding.postRecyclerView.adapter = postAdapter
        readPosts()
    }

    // ************************************************************
    // ** CRITICAL FIX: REMOVED onDestroy() and onStop() LOGIC **
    // The session will now persist for all navigation paths.
    // ************************************************************


    fun readPosts() {
        val userId = sessionManager.getUserId()

        if (userId == -1) {
            // Should not happen if the initial session check passes, but good safeguard.
            return
        }

        ApiService.getPosts(userId, object : ApiService.ApiCallback {
            override fun onSuccess(response: String) {
                runOnUiThread {
                    try {
                        val jsonObject = JsonParser.parseString(response).asJsonObject
                        val status = jsonObject.get("status")?.asString

                        if (status == "success") {
                            val postsArray = jsonObject.getAsJsonArray("posts")
                            postList.clear()

                            postsArray?.forEach { element ->
                                val postJson = element.asJsonObject
                                val post = Post(
                                    id = postJson.get("id")?.asInt ?: 0,
                                    user_id = postJson.get("user_id")?.asInt ?: 0,
                                    image_url = postJson.get("image_url")?.asString ?: "",
                                    caption = postJson.get("caption")?.asString ?: "",
                                    username = postJson.get("username")?.asString ?: "",
                                    first_name = postJson.get("first_name")?.asString ?: "",
                                    last_name = postJson.get("last_name")?.asString ?: "",
                                    profile_photo_url = postJson.get("profile_photo_url")?.asString,
                                    like_count = postJson.get("like_count")?.asInt ?: 0,
                                    comment_count = postJson.get("comment_count")?.asInt ?: 0,
                                    is_liked = postJson.get("is_liked")?.asBoolean ?: false
                                )
                                postList.add(post)
                            }

                            postAdapter.notifyDataSetChanged()
                        }
                    } catch (e: Exception) {
                        Log.e("HomeActivity", "Error parsing posts JSON: ${e.message}", e)
                    }
                }
            }

            override fun onError(error: String) {
                Log.e("HomeActivity", "Error reading posts: $error")
            }
        })
    }

    override fun onResume() {
        super.onResume()
        Log.d("HomeActivity", "onResume - reloading stories and posts")
        // onResume is the correct place to refresh data after returning from another activity
        if (sessionManager.isLoggedIn()) {
            // Reload all stories from database (all users)
            loadStories()
            readPosts()
        }
    }

    private fun openStoryViewer(userStory: userStory) {
        val intent = Intent(this, StoryViewerActivity::class.java)
        intent.putExtra("username", userStory.username)
        intent.putExtra("storyCount", userStory.stories.size)

        val imageUrls = userStory.stories.map { it.imageUrl }.toTypedArray()
        intent.putExtra("imageUrls", imageUrls)

        startActivity(intent)
    }

    private fun loadStories() {
        val currentUserId = sessionManager.getUserId()
        if (currentUserId == -1) {
            Log.e("HomeActivity", "User not logged in")
            return
        }

        Log.d("HomeActivity", "Loading stories from web service...")
        ApiService.getStories(currentUserId, object : ApiService.ApiCallback {
            override fun onSuccess(response: String) {
                runOnUiThread {
                    try {
                        val jsonObject = JsonParser.parseString(response).asJsonObject
                        if (jsonObject.get("status")?.asString == "success") {
                            val storiesArray = jsonObject.getAsJsonArray("stories")
                            userStories.clear()

                            storiesArray?.forEach { element ->
                                val userStoryJson = element.asJsonObject
                                val userId = userStoryJson.get("user_id")?.asInt ?: 0
                                val username = userStoryJson.get("username")?.asString ?: ""
                                val firstName = userStoryJson.get("first_name")?.asString ?: ""
                                val lastName = userStoryJson.get("last_name")?.asString ?: ""
                                val profilePhotoUrl = userStoryJson.get("profile_photo_url")?.asString
                                
                                val storiesJsonArray = userStoryJson.getAsJsonArray("stories")
                                val stories = mutableListOf<Story>()
                                
                                storiesJsonArray?.forEach { storyElement ->
                                    val storyJson = storyElement.asJsonObject
                                    val story = Story(
                                        id = storyJson.get("id")?.asInt ?: 0,
                                        user_id = userId,
                                        media_url = storyJson.get("media_url")?.asString ?: "",
                                        media_type = storyJson.get("media_type")?.asString ?: "image",
                                        created_at = storyJson.get("created_at")?.asString ?: "",
                                        expires_at = storyJson.get("expires_at")?.asString ?: ""
                                    )
                                    stories.add(story)
                                }

                                if (stories.isNotEmpty()) {
                                    val sortedStories = stories.sortedByDescending { it.timestamp }
                                    val latestImageUrl = sortedStories.first().imageUrl
                                    
                                    userStories.add(
                                        userStory(
                                            userId = userId.toString(),
                                            username = username,
                                            stories = sortedStories,
                                            latestImageUrl = latestImageUrl,
                                            profilePhotoUrl = profilePhotoUrl
                                        )
                                    )
                                }
                            }

                            // Sort by latest story timestamp
                            userStories.sortByDescending {
                                it.stories.maxOfOrNull { story -> story.timestamp } ?: 0L
                            }

                            Log.d("HomeActivity", "Total users with stories: ${userStories.size}")
                            if (userStories.isEmpty()) {
                                Log.d("HomeActivity", "No stories found in database")
                            } else {
                                Log.d("HomeActivity", "Stories loaded successfully for ${userStories.size} users")
                            }
                            storyAdapter.notifyDataSetChanged()
                        } else {
                            Log.e("HomeActivity", "Failed to load stories: ${jsonObject.get("message")?.asString}")
                        }
                    } catch (e: Exception) {
                        Log.e("HomeActivity", "Error parsing stories: ${e.message}", e)
                        e.printStackTrace()
                    }
                }
            }

            override fun onError(error: String) {
                runOnUiThread {
                    Log.e("HomeActivity", "Error loading stories: $error")
                }
            }
        })
    }
}
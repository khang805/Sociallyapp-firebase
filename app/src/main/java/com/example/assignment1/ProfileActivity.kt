package com.example.assignment1

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.JsonParser
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ProfileActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var postsRecyclerView: RecyclerView
    private lateinit var postAdapter: ProfilePostAdapter
    private val postList = mutableListOf<Post>()
    private lateinit var followersCount: TextView
    private lateinit var followingCount: TextView
    private var storyImageUri: Uri? = null
    private lateinit var profileStoryRecyclerView: RecyclerView
    private lateinit var profileStoryAdapter: ProfileStoryAdapter
    private val userStoriesList = mutableListOf<Story>()
    
    // Activity Result launcher for picking story from gallery
    private val pickStoryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            storyImageUri = it
            uploadStory(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        sessionManager = SessionManager(this)

        val dp = findViewById<ImageView>(R.id.dp)
        val nameTextView = findViewById<TextView>(R.id.name)
        val usernameTextView = findViewById<TextView>(R.id.username)
        val postsCountTextView = findViewById<TextView>(R.id.post_count)
        postsRecyclerView = findViewById<RecyclerView>(R.id.postsGridRecyclerView)
        followersCount = findViewById(R.id.followersCount)
        followingCount = findViewById(R.id.followingCount)

        postsRecyclerView.layoutManager = GridLayoutManager(this, 3)
        postAdapter = ProfilePostAdapter(this, postList)
        postsRecyclerView.adapter = postAdapter

        // Setup Profile Stories RecyclerView
        profileStoryRecyclerView = findViewById(R.id.profileStoryRecyclerView)
        profileStoryRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        profileStoryAdapter = ProfileStoryAdapter(this, userStoriesList) { story ->
            openSingleStoryViewer(story)
        }
        profileStoryRecyclerView.adapter = profileStoryAdapter

        val userId = sessionManager.getUserId()
        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadUserProfile(userId, dp, nameTextView, usernameTextView, postsCountTextView)
        loadUserPosts(userId)
        loadFollowCounts(userId)
        loadUserStories(userId)

        findViewById<LinearLayout>(R.id.followersLayout).setOnClickListener {
            val intent = Intent(this, FollowListActivity::class.java)
            intent.putExtra("user_id", userId)
            intent.putExtra("type", "followers")
            startActivity(intent)
        }

        findViewById<LinearLayout>(R.id.followingLayout).setOnClickListener {
            val intent = Intent(this, FollowingListActivity::class.java)
            intent.putExtra("user_id", userId)
            intent.putExtra("type", "following")
            startActivity(intent)
        }

        setupNavigation()
    }

    private fun loadUserProfile(userId: Int, dp: ImageView, nameTextView: TextView, usernameTextView: TextView, postsCountTextView: TextView) {
        ApiService.getUserProfile(userId, object : ApiService.ApiCallback {
            override fun onSuccess(response: String) {
                runOnUiThread {
                    try {
                        val jsonObject = JsonParser.parseString(response).asJsonObject
                        if (jsonObject.get("status")?.asString == "success") {
                            val user = jsonObject.getAsJsonObject("user")
                            val firstName = user.get("first_name")?.asString ?: ""
                            val lastName = user.get("last_name")?.asString ?: ""
                            val username = user.get("username")?.asString ?: ""
                            val profilePhotoUrl = user.get("profile_photo_url")?.asString
                            val postsCount = user.get("posts_count")?.asInt ?: 0

                            nameTextView.text = "$firstName $lastName".trim()
                            usernameTextView.text = username
                            postsCountTextView.text = postsCount.toString()

                            if (!profilePhotoUrl.isNullOrEmpty()) {
                                Glide.with(this@ProfileActivity)
                                    .load(profilePhotoUrl)
                                    .placeholder(R.drawable.ic_profile_vector)
                                    .error(R.drawable.ic_profile_vector)
                                    .circleCrop()
                                    .into(dp)
                            } else {
                                dp.setImageResource(R.drawable.ic_profile_vector)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onError(error: String) {
                runOnUiThread {
                    Toast.makeText(this@ProfileActivity, "Failed to load profile: $error", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun loadUserPosts(userId: Int) {
        // Get posts for this user
        ApiService.getPosts(userId, object : ApiService.ApiCallback {
            override fun onSuccess(response: String) {
                runOnUiThread {
                    try {
                        val jsonObject = JsonParser.parseString(response).asJsonObject
                        if (jsonObject.get("status")?.asString == "success") {
                            val postsArray = jsonObject.getAsJsonArray("posts")
                            postList.clear()

                            postsArray?.forEach { element ->
                                val postJson = element.asJsonObject
                                val postUserId = postJson.get("user_id")?.asInt ?: 0
                                // Only add posts from this user
                                if (postUserId == userId) {
                                    val post = Post(
                                        id = postJson.get("id")?.asInt ?: 0,
                                        user_id = postUserId,
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
                            }

                            postAdapter.notifyDataSetChanged()
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

    private fun loadFollowCounts(userId: Int) {
        ApiService.getFollowCounts(userId, object : ApiService.ApiCallback {
            override fun onSuccess(response: String) {
                runOnUiThread {
                    try {
                        val jsonObject = JsonParser.parseString(response).asJsonObject
                        if (jsonObject.get("status")?.asString == "success") {
                            followersCount.text = jsonObject.get("followers_count")?.asInt?.toString() ?: "0"
                            followingCount.text = jsonObject.get("following_count")?.asInt?.toString() ?: "0"
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

    private fun openStoryGallery() {
        pickStoryLauncher.launch("image/*")
    }

    private fun loadUserStories(userId: Int) {
        Log.d("ProfileActivity", "Loading stories for user $userId")
        ApiService.getStories(userId, object : ApiService.ApiCallback {
            override fun onSuccess(response: String) {
                runOnUiThread {
                    try {
                        Log.d("ProfileActivity", "API Response: $response")
                        val jsonObject = JsonParser.parseString(response).asJsonObject
                        if (jsonObject.get("status")?.asString == "success") {
                            val storiesArray = jsonObject.getAsJsonArray("stories")
                            Log.d("ProfileActivity", "Found ${storiesArray?.size() ?: 0} user story groups in response")
                            userStoriesList.clear()

                            storiesArray?.forEach { element ->
                                val userStoryJson = element.asJsonObject
                                val storyUserId = userStoryJson.get("user_id")?.asInt ?: 0
                                
                                // Only load current user's stories
                                if (storyUserId == userId) {
                                    val storiesJsonArray = userStoryJson.getAsJsonArray("stories")
                                    
                                    storiesJsonArray?.forEach { storyElement ->
                                        val storyJson = storyElement.asJsonObject
                                        val story = Story(
                                            id = storyJson.get("id")?.asInt ?: 0,
                                            user_id = storyUserId,
                                            media_url = storyJson.get("media_url")?.asString ?: "",
                                            media_type = storyJson.get("media_type")?.asString ?: "image",
                                            created_at = storyJson.get("created_at")?.asString ?: "",
                                            expires_at = storyJson.get("expires_at")?.asString ?: ""
                                        )
                                        userStoriesList.add(story)
                                    }
                                }
                            }

                            // Sort by timestamp (newest first)
                            userStoriesList.sortByDescending { it.timestamp }

                            Log.d("ProfileActivity", "Loaded ${userStoriesList.size} stories for user $userId")
                            if (userStoriesList.isEmpty()) {
                                Log.d("ProfileActivity", "No stories found for user $userId - check database")
                            } else {
                                Log.d("ProfileActivity", "Stories loaded: ${userStoriesList.map { "ID:${it.id}, URL:${it.imageUrl.take(50)}" }}")
                            }
                            
                            profileStoryAdapter.notifyDataSetChanged()
                            Log.d("ProfileActivity", "Adapter notified. Item count: ${profileStoryAdapter.itemCount}")
                            Log.d("ProfileActivity", "RecyclerView visibility: ${profileStoryRecyclerView.visibility}, height: ${profileStoryRecyclerView.height}")
                        } else {
                            val message = jsonObject.get("message")?.asString ?: "Unknown error"
                            Log.e("ProfileActivity", "Failed to load stories: $message")
                            Toast.makeText(this@ProfileActivity, "Failed to load stories: $message", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e("ProfileActivity", "Error parsing stories: ${e.message}", e)
                        e.printStackTrace()
                        Toast.makeText(this@ProfileActivity, "Error parsing stories: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onError(error: String) {
                runOnUiThread {
                    Log.e("ProfileActivity", "Error loading stories: $error")
                    Toast.makeText(this@ProfileActivity, "Error loading stories: $error", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun openSingleStoryViewer(story: Story) {
        // Create a userStory with just this one story for the viewer
        val singleUserStory = userStory(
            userId = story.user_id.toString(),
            username = sessionManager.getUsername() ?: "You",
            stories = listOf(story),
            latestImageUrl = story.imageUrl,
            profilePhotoUrl = null
        )
        openStoryViewer(singleUserStory)
    }

    private fun openStoryViewer(userStory: userStory) {
        val intent = Intent(this, StoryViewerActivity::class.java)
        intent.putExtra("username", userStory.username)
        intent.putExtra("storyCount", userStory.stories.size)
        val imageUrls = userStory.stories.map { it.imageUrl }.toTypedArray()
        intent.putExtra("imageUrls", imageUrls)
        startActivity(intent)
    }

    private fun uploadStory(uri: Uri) {
        val userId = sessionManager.getUserId()
        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // Convert URI to File
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val file = File(cacheDir, "temp_story_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            // Show loading toast
            Toast.makeText(this, "Uploading story...", Toast.LENGTH_SHORT).show()

            ApiService.uploadStory(userId, file, object : ApiService.ApiCallback {
                override fun onSuccess(response: String) {
                    runOnUiThread {
                        try {
                            val jsonObject = JsonParser.parseString(response).asJsonObject
                            if (jsonObject.get("status")?.asString == "success") {
                                Toast.makeText(this@ProfileActivity, "Story uploaded successfully!", Toast.LENGTH_SHORT).show()
                                // Clean up temp file
                                file.delete()
                                // Reload stories to show the new one
                                loadUserStories(userId)
                            } else {
                                val message = jsonObject.get("message")?.asString ?: "Failed to upload story"
                                Toast.makeText(this@ProfileActivity, message, Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(this@ProfileActivity, "Error uploading story", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onError(error: String) {
                    runOnUiThread {
                        Toast.makeText(this@ProfileActivity, "Upload failed: $error", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error processing image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupNavigation() {
        val home = findViewById<ImageView>(R.id.home)
        val search = findViewById<ImageView>(R.id.search)
        val add = findViewById<ImageView>(R.id.add)
        val heart = findViewById<ImageView>(R.id.heart)
        val profile = findViewById<ImageView>(R.id.profile)
        val addNew = findViewById<ImageView>(R.id.addnew)
        val editProfile = findViewById<TextView>(R.id.edit)

        addNew.setOnClickListener {
            openStoryGallery()
        }
        editProfile.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }
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

    override fun onResume() {
        super.onResume()
        val userId = sessionManager.getUserId()
        if (userId != -1) {
            // Reload stories when returning to profile
            loadUserStories(userId)
        }
    }
}

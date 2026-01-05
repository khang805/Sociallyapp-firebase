package com.example.assignment1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.assignment1.databinding.ActivityCommentsBinding
import com.google.gson.JsonParser

class CommentsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommentsBinding
    private lateinit var sessionManager: SessionManager
    private var postId: Int = 0
    private var publisherId: Int = 0
    private var commentAdapter: CommentAdapter? = null
    private var commentList: MutableList<Comment>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        val intent = intent
        postId = intent.getIntExtra("postId", 0)
        publisherId = intent.getIntExtra("publisherId", 0)

        if (postId == 0) {
            Toast.makeText(this, "Invalid post", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.toolbar.title = "Comments"
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        val recyclerView: RecyclerView = binding.recyclerView
        recyclerView.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager

        commentList = ArrayList()
        commentAdapter = CommentAdapter(this, commentList as ArrayList<Comment>)
        recyclerView.adapter = commentAdapter

        binding.post.setOnClickListener {
            val commentText = binding.addComment.text.toString().trim()
            if (commentText.isEmpty()) {
                Toast.makeText(this@CommentsActivity, "You can't send empty message", Toast.LENGTH_SHORT).show()
            } else {
                addComment(commentText)
            }
        }

        // Set profile image placeholder
        binding.profileImage.setImageResource(R.drawable.ic_profile_vector)
        
        readComments()
    }

    private fun addComment(commentText: String) {
        val userId = sessionManager.getUserId()
        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        ApiService.addComment(userId, postId, commentText, object : ApiService.ApiCallback {
            override fun onSuccess(response: String) {
                runOnUiThread {
                    try {
                        val jsonObject = JsonParser.parseString(response).asJsonObject
                        val status = jsonObject.get("status")?.asString

                        if (status == "success") {
                            binding.addComment.setText("")
                            readComments() // Refresh comments
                        } else {
                            val message = jsonObject.get("message")?.asString ?: "Failed to add comment"
                            Toast.makeText(this@CommentsActivity, message, Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this@CommentsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onError(error: String) {
                runOnUiThread {
                    Toast.makeText(this@CommentsActivity, error, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun readComments() {
        ApiService.getComments(postId, object : ApiService.ApiCallback {
            override fun onSuccess(response: String) {
                runOnUiThread {
                    try {
                        val jsonObject = JsonParser.parseString(response).asJsonObject
                        val status = jsonObject.get("status")?.asString

                        if (status == "success") {
                            val commentsArray = jsonObject.getAsJsonArray("comments")
                            commentList?.clear()

                            commentsArray?.forEach { element ->
                                val commentJson = element.asJsonObject
                                val comment = Comment(
                                    id = commentJson.get("id")?.asInt ?: 0,
                                    user_id = commentJson.get("user_id")?.asInt ?: 0,
                                    comment = commentJson.get("comment")?.asString ?: "",
                                    username = commentJson.get("username")?.asString ?: "",
                                    first_name = commentJson.get("first_name")?.asString ?: "",
                                    last_name = commentJson.get("last_name")?.asString ?: ""
                                )
                                commentList?.add(comment)
                            }

                            commentAdapter?.notifyDataSetChanged()
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
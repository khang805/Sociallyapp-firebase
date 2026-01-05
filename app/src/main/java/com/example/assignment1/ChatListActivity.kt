package com.example.assignment1

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonParser

class ChatListActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ConversationAdapter
    private lateinit var emptyStateText: TextView
    private lateinit var sessionManager: SessionManager
    private val conversations = mutableListOf<Conversation>()
    private var currentUserId: Int = 0
    private val handler = Handler(Looper.getMainLooper())
    private var refreshRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        sessionManager = SessionManager(this)
        currentUserId = sessionManager.getUserId()

        if (currentUserId == -1) {
            // Log for clarity:
            Log.e("ChatListActivity", "Current User ID is -1. Session invalid or not found.")
            finish()
            return
        }

        Log.d("ChatListActivity", "Current User ID: $currentUserId")

        recyclerView = findViewById(R.id.conversationsRecyclerView)
        emptyStateText = findViewById(R.id.emptyStateText)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // NOTE: ConversationAdapter and Conversation data class are assumed to exist and be correct.
        adapter = ConversationAdapter(conversations) { conversation ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("other_user_id", conversation.other_user_id)
            intent.putExtra("other_username", conversation.other_username)
            intent.putExtra("other_first_name", conversation.other_first_name)
            intent.putExtra("other_last_name", conversation.other_last_name)
            intent.putExtra("other_profile_photo_url", conversation.other_profile_photo_url)
            startActivity(intent)
        }

        recyclerView.adapter = adapter

        loadConversations()
        startAutoRefresh()
    }

    private fun loadConversations() {
        Log.d("ChatListActivity", "Fetching conversations for user $currentUserId...")
        ApiService.getConversations(currentUserId, object : ApiService.ApiCallback {
            override fun onSuccess(response: String) {
                runOnUiThread {
                    try {
                        // LOGGING THE RESPONSE - CRITICAL FOR DEBUGGING SERVER ISSUE
                        Log.d("ChatListActivity_API", "Raw Conversation Response: ${response.take(500)}")

                        val jsonObject = JsonParser.parseString(response).asJsonObject
                        if (jsonObject.get("status")?.asString == "success") {
                            val conversationsArray = jsonObject.getAsJsonArray("conversations")
                            conversations.clear()

                            var successfulLoads = 0
                            conversationsArray?.forEach { element ->
                                val convJson = element.asJsonObject
                                try {
                                    val conversation = Conversation(
                                        message_id = convJson.get("message_id")?.asInt ?: 0,
                                        other_user_id = convJson.get("other_user_id")?.asInt ?: 0,
                                        other_username = convJson.get("other_username")?.asString ?: "",
                                        other_first_name = convJson.get("other_first_name")?.asString ?: "",
                                        other_last_name = convJson.get("other_last_name")?.asString ?: "",
                                        other_profile_photo_url = convJson.get("other_profile_photo_url")?.asString,
                                        last_message_text = convJson.get("last_message_text")?.asString,
                                        last_message_media_type = convJson.get("last_message_media_type")?.asString ?: "text",
                                        last_message_media_url = convJson.get("last_message_media_url")?.asString,
                                        last_message_time = convJson.get("last_message_time")?.asString ?: "",
                                        unread_count = convJson.get("unread_count")?.asInt ?: 0
                                    )
                                    conversations.add(conversation)
                                    successfulLoads++
                                } catch (e: Exception) {
                                    // LOGGING PARSING FAILURE
                                    Log.e("ChatListActivity_Parsing", "Failed to parse conversation element: ${convJson.toString()} - Error: ${e.message}")
                                }
                            }

                            Log.d("ChatListActivity", "Successfully loaded $successfulLoads conversations.")

                            adapter.updateConversations(conversations)
                            updateEmptyState()
                        } else {
                            Log.e("ChatListActivity_API", "API Status Failed: ${jsonObject.get("message")?.asString}")
                        }
                    } catch (e: Exception) {
                        Log.e("ChatListActivity", "Error processing conversation response: ${e.message}", e)
                    }
                }
            }

            override fun onError(error: String) {
                runOnUiThread {
                    Log.e("ChatListActivity", "Error loading conversations: $error")
                }
            }
        })
    }

    private fun updateEmptyState() {
        emptyStateText.visibility = if (conversations.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun startAutoRefresh() {
        refreshRunnable = object : Runnable {
            override fun run() {
                loadConversations()
                handler.postDelayed(this, 5000) // Refresh every 5 seconds
            }
        }
        handler.post(refreshRunnable!!)
    }

    override fun onResume() {
        super.onResume()
        // Ensure the list refreshes when the user returns to this activity
        loadConversations()
        startAutoRefresh()
    }

    override fun onPause() {
        super.onPause()
        refreshRunnable?.let { handler.removeCallbacks(it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        refreshRunnable?.let { handler.removeCallbacks(it) }
    }
}
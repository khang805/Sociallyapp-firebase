package com.example.assignment1

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.JsonParser
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ChatActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChatMessageAdapter
    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageView
    private lateinit var attachButton: ImageView
    private lateinit var backButton: ImageView
    private lateinit var otherUserProfileImage: CircleImageView
    private lateinit var otherUserName: android.widget.TextView
    private lateinit var vanishModeToggle: ImageView

    private lateinit var sessionManager: SessionManager
    private lateinit var dbHelper: MessageDatabaseHelper
    private val messages = mutableListOf<ChatMessage>()

    private var currentUserId: Int = 0
    private var otherUserId: Int = 0
    private var otherUsername: String = ""
    private var otherFirstName: String = ""
    private var otherLastName: String = ""
    private var otherProfilePhotoUrl: String? = null

    private var isVanishMode: Boolean = false
    private val handler = Handler(Looper.getMainLooper())
    private var refreshRunnable: Runnable? = null

    private val pickMediaLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { uploadMedia(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        sessionManager = SessionManager(this)
        currentUserId = sessionManager.getUserId()
        dbHelper = MessageDatabaseHelper(this)

        if (currentUserId == -1) {
            finish()
            return
        }

        otherUserId = intent.getIntExtra("other_user_id", 0)
        otherUsername = intent.getStringExtra("other_username") ?: ""
        otherFirstName = intent.getStringExtra("other_first_name") ?: ""
        otherLastName = intent.getStringExtra("other_last_name") ?: ""
        otherProfilePhotoUrl = intent.getStringExtra("other_profile_photo_url")

        if (otherUserId == 0) {
            finish()
            return
        }

        initViews()
        setupRecyclerView()
        loadMessages()
        markMessagesAsSeen()
        startAutoRefresh()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.messagesRecyclerView)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
        attachButton = findViewById(R.id.attachButton)
        backButton = findViewById(R.id.backButton)
        otherUserProfileImage = findViewById(R.id.otherUserProfileImage)
        otherUserName = findViewById(R.id.otherUserName)

        // 💡 FIX: Initialize vanishModeToggle here
        vanishModeToggle = findViewById(R.id.vanishModeToggle)

        otherUserName.text = "$otherFirstName $otherLastName".trim().ifEmpty { otherUsername }

        if (otherProfilePhotoUrl != null) {
            Glide.with(this)
                .load(otherProfilePhotoUrl)
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .circleCrop()
                .into(otherUserProfileImage)
        }

        backButton.setOnClickListener { finish() }

        sendButton.setOnClickListener { sendMessage() }
        attachButton.setOnClickListener { pickMediaLauncher.launch("*/*") }

        // This click listener will now execute without crashing because the view is initialized.
        vanishModeToggle.setOnClickListener {
            isVanishMode = !isVanishMode
            updateVanishModeUI()
        }

        messageInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                sendButton.isEnabled = !s.isNullOrBlank()
            }
        })
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        adapter = ChatMessageAdapter(messages, currentUserId) { message ->
            showMessageOptions(message)
        }
        recyclerView.adapter = adapter
    }

    private fun loadMessages() {
        // Load from local cache first
        val cachedMessages = dbHelper.getMessages(currentUserId, otherUserId)
        if (cachedMessages.isNotEmpty()) {
            messages.clear()
            messages.addAll(cachedMessages)
            adapter.updateMessages(messages)
            scrollToBottom()
        }

        // Then fetch from server
        Log.d("ChatActivity", "Loading messages between user $currentUserId and $otherUserId")
        ApiService.getMessages(currentUserId, otherUserId, 50, 0, object : ApiService.ApiCallback {
            override fun onSuccess(response: String) {
                runOnUiThread {
                    try {
                        Log.d("ChatActivity", "Received response: ${response.take(200)}")
                        val jsonObject = JsonParser.parseString(response).asJsonObject
                        if (jsonObject.get("status")?.asString == "success") {
                            val messagesArray = jsonObject.getAsJsonArray("messages")
                            val newMessages = mutableListOf<ChatMessage>()

                            messagesArray?.forEach { element ->
                                val msgJson = element.asJsonObject
                                val message = ChatMessage(
                                    id = msgJson.get("id")?.asInt ?: 0,
                                    sender_id = msgJson.get("sender_id")?.asInt ?: 0,
                                    receiver_id = msgJson.get("receiver_id")?.asInt ?: 0,
                                    message_text = msgJson.get("message_text")?.asString,
                                    media_type = msgJson.get("media_type")?.asString ?: "text",
                                    media_url = msgJson.get("media_url")?.asString,
                                    is_seen = msgJson.get("is_seen")?.asBoolean ?: false,
                                    is_vanish_mode = msgJson.get("is_vanish_mode")?.asBoolean ?: false,
                                    created_at = msgJson.get("created_at")?.asString ?: "",
                                    updated_at = msgJson.get("updated_at")?.asString ?: "",
                                    sender_username = msgJson.get("sender_username")?.asString ?: "",
                                    sender_first_name = msgJson.get("sender_first_name")?.asString ?: "",
                                    sender_last_name = msgJson.get("sender_last_name")?.asString ?: "",
                                    sender_profile_photo_url = msgJson.get("sender_profile_photo_url")?.asString
                                )
                                newMessages.add(message)
                                // Cache locally
                                dbHelper.insertOrUpdateMessage(message)
                            }

                            // Remove temporary messages (id = -1) before updating
                            messages.removeAll { it.id == -1 }

                            // Update messages list
                            val previousCount = messages.size
                            messages.clear()
                            messages.addAll(newMessages)

                            Log.d("ChatActivity", "Loaded ${messages.size} messages (previous: $previousCount)")

                            // Always update adapter to ensure UI is refreshed
                            adapter.updateMessages(messages)
                            scrollToBottom()
                            markMessagesAsSeen()
                        } else {
                            Log.e("ChatActivity", "Failed to load messages: ${jsonObject.get("message")?.asString}")
                        }
                    } catch (e: Exception) {
                        Log.e("ChatActivity", "Error parsing messages: ${e.message}", e)
                        e.printStackTrace()
                    }
                }
            }

            override fun onError(error: String) {
                runOnUiThread {
                    Log.e("ChatActivity", "Error loading messages: $error")
                }
            }
        })
    }

    private fun sendMessage() {
        val text = messageInput.text.toString().trim()
        if (text.isEmpty()) return

        messageInput.setText("")
        sendTextMessage(text)
    }

    private fun sendTextMessage(text: String) {
        // Optimistically add message to UI immediately
        val tempMessage = ChatMessage(
            id = -1, // Temporary ID
            sender_id = currentUserId,
            receiver_id = otherUserId,
            message_text = text,
            media_type = "text",
            media_url = null,
            is_seen = false,
            is_vanish_mode = isVanishMode,
            created_at = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date()),
            updated_at = "",
            sender_username = sessionManager.getUsername() ?: "",
            sender_first_name = "",
            sender_last_name = "",
            sender_profile_photo_url = null
        )
        messages.add(tempMessage)
        adapter.addMessage(tempMessage)
        scrollToBottom()

        ApiService.sendMessage(
            currentUserId,
            otherUserId,
            text,
            "text",
            null,
            isVanishMode,
            object : ApiService.ApiCallback {
                override fun onSuccess(response: String) {
                    runOnUiThread {
                        try {
                            val jsonObject = JsonParser.parseString(response).asJsonObject
                            if (jsonObject.get("status")?.asString == "success") {
                                // Reload messages to get the actual message with correct ID from server
                                loadMessages()
                            } else {
                                // Remove the optimistic message if send failed
                                messages.remove(tempMessage)
                                adapter.updateMessages(messages)
                                Toast.makeText(this@ChatActivity, "Failed to send message", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Log.e("ChatActivity", "Error sending message: ${e.message}", e)
                            // Remove the optimistic message on error
                            messages.remove(tempMessage)
                            adapter.updateMessages(messages)
                        }
                    }
                }

                override fun onError(error: String) {
                    runOnUiThread {
                        // Remove the optimistic message on error
                        messages.remove(tempMessage)
                        adapter.updateMessages(messages)
                        Toast.makeText(this@ChatActivity, "Error: $error", Toast.LENGTH_SHORT).show()
                    }
                }
            })
    }

    private fun uploadMedia(uri: Uri) {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val file = File(cacheDir, "temp_media_${System.currentTimeMillis()}")
            val outputStream = FileOutputStream(file)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            val mediaType = when {
                file.name.contains(".jpg") || file.name.contains(".jpeg") ||
                        file.name.contains(".png") || file.name.contains(".gif") -> "image"
                file.name.contains(".mp4") || file.name.contains(".mov") ||
                        file.name.contains(".avi") -> "video"
                else -> "file"
            }

            ApiService.sendMessage(
                currentUserId,
                otherUserId,
                null,
                mediaType,
                file,
                isVanishMode,
                object : ApiService.ApiCallback {
                    override fun onSuccess(response: String) {
                        runOnUiThread {
                            file.delete()
                            loadMessages()
                        }
                    }

                    override fun onError(error: String) {
                        runOnUiThread {
                            file.delete()
                            Toast.makeText(this@ChatActivity, "Error uploading: $error", Toast.LENGTH_SHORT).show()
                        }
                    }
                })
        } catch (e: Exception) {
            Toast.makeText(this, "Error processing file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showMessageOptions(message: ChatMessage) {
        if (!message.canEditOrDelete) {
            Toast.makeText(this, "Message can only be edited/deleted within 5 minutes", Toast.LENGTH_SHORT).show()
            return
        }

        val options = arrayOf("Edit", "Delete")
        AlertDialog.Builder(this)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> editMessage(message)
                    1 -> deleteMessage(message)
                }
            }
            .show()
    }

    private fun editMessage(message: ChatMessage) {
        val input = EditText(this)

        // FIX: Use the Elvis operator (?:) to safely handle a potentially null message_text.
        // This ensures the setText function always receives a non-null CharSequence (in this case, String).
        input.setText(message.message_text ?: "")

        AlertDialog.Builder(this)
            .setTitle("Edit Message")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newText = input.text.toString().trim()
                if (newText.isNotEmpty()) {
                    ApiService.editMessage(message.id, currentUserId, newText, object : ApiService.ApiCallback {
                        override fun onSuccess(response: String) {
                            runOnUiThread {
                                try {
                                    val jsonObject = JsonParser.parseString(response).asJsonObject
                                    if (jsonObject.get("status")?.asString == "success") {
                                        loadMessages()
                                    } else {
                                        Toast.makeText(this@ChatActivity, "Failed to edit message", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Log.e("ChatActivity", "Error editing message: ${e.message}", e)
                                }
                            }
                        }

                        override fun onError(error: String) {
                            runOnUiThread {
                                Toast.makeText(this@ChatActivity, "Error: $error", Toast.LENGTH_SHORT).show()
                            }
                        }
                    })
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteMessage(message: ChatMessage) {
        AlertDialog.Builder(this)
            .setTitle("Delete Message")
            .setMessage("Are you sure you want to delete this message?")
            .setPositiveButton("Delete") { _, _ ->
                ApiService.deleteMessage(message.id, currentUserId, object : ApiService.ApiCallback {
                    override fun onSuccess(response: String) {
                        runOnUiThread {
                            try {
                                val jsonObject = JsonParser.parseString(response).asJsonObject
                                if (jsonObject.get("status")?.asString == "success") {
                                    dbHelper.deleteMessage(message.id)
                                    loadMessages()
                                } else {
                                    Toast.makeText(this@ChatActivity, "Failed to delete message", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Log.e("ChatActivity", "Error deleting message: ${e.message}", e)
                            }
                        }
                    }

                    override fun onError(error: String) {
                        runOnUiThread {
                            Toast.makeText(this@ChatActivity, "Error: $error", Toast.LENGTH_SHORT).show()
                        }
                    }
                })
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun markMessagesAsSeen() {
        ApiService.markMessagesSeen(currentUserId, otherUserId, object : ApiService.ApiCallback {
            override fun onSuccess(response: String) {
                dbHelper.markMessagesAsSeen(otherUserId, currentUserId)
            }

            override fun onError(error: String) {
                Log.e("ChatActivity", "Error marking messages as seen: $error")
            }
        })
    }

    private fun updateVanishModeUI() {
        // Update UI to show vanish mode is active
        vanishModeToggle.alpha = if (isVanishMode) 1.0f else 0.5f
    }

    private fun scrollToBottom() {
        recyclerView.post {
            if (messages.isNotEmpty()) {
                recyclerView.smoothScrollToPosition(messages.size - 1)
            }
        }
    }

    private fun startAutoRefresh() {
        refreshRunnable = object : Runnable {
            override fun run() {
                if (!isFinishing) {
                    loadMessages()
                    handler.postDelayed(this, 2000) // Refresh every 2 seconds for better real-time feel
                }
            }
        }
        handler.post(refreshRunnable!!)
    }

    override fun onResume() {
        super.onResume()
        loadMessages()
        markMessagesAsSeen()
    }

    override fun onPause() {
        super.onPause()
        refreshRunnable?.let { handler.removeCallbacks(it) }
        markMessagesAsSeen()
    }

    override fun onDestroy() {
        super.onDestroy()
        refreshRunnable?.let { handler.removeCallbacks(it) }

        // Delete vanish mode messages if chat is closed
        if (isVanishMode) {
            ApiService.deleteVanishMessages(currentUserId, otherUserId, object : ApiService.ApiCallback {
                override fun onSuccess(response: String) {
                    dbHelper.deleteVanishMessages(currentUserId, otherUserId)
                }
                override fun onError(error: String) {
                    Log.e("ChatActivity", "Error deleting vanish messages: $error")
                }
            })
        }
    }
}
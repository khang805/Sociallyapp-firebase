package com.example.assignment1

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView

class MainActivity8 : AppCompatActivity() {

    private val CAMERA_REQUEST_CODE = 101
    private val GALLERY_REQUEST_CODE = 102

    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var chatList: MutableList<Message>

    private var selectedUserId: String? = null
    private var currentUserId: String? = null
    private var chatId: String = ""
    private var imageUri: Uri? = null

    private var screenshotObserver: ContentObserver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main8)

        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUserId = auth.currentUser?.uid

        val usernameView = findViewById<TextView>(R.id.title_text)
        val profilePic = findViewById<CircleImageView>(R.id.profile_pic)
        val backArrow = findViewById<ImageView>(R.id.back_arrow)
        val cameraIcon = findViewById<ImageView>(R.id.camera_icon)
        val galleryIcon = findViewById<ImageView>(R.id.gallery_icon)
        val sendIcon = findViewById<ImageView>(R.id.send_icon)
        val videoCallIcon = findViewById<ImageView>(R.id.video_call)
        val messageBox = findViewById<EditText>(R.id.message_box)
        val chatRecyclerView = findViewById<RecyclerView>(R.id.chatRecyclerView)

        // Intent data
        val userName = intent.getStringExtra("username")
        selectedUserId = intent.getStringExtra("uid")
        usernameView.text = userName

        // Load receiver profile pic
        if (!selectedUserId.isNullOrEmpty()) {
            database.reference.child("users").child(selectedUserId!!)
                .get().addOnSuccessListener { snapshot ->
                    val imageUrl = snapshot.child("profileImage").getValue(String::class.java)
                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this).load(imageUrl).into(profilePic)
                    } else profilePic.setImageResource(R.drawable.ic_profile)
                }
        }

        // Unique chat ID for both users
        if (currentUserId != null && selectedUserId != null) {
            chatId = if (currentUserId!! < selectedUserId!!) {
                "${currentUserId}_${selectedUserId}"
            } else {
                "${selectedUserId}_${currentUserId}"
            }
        }

        chatList = mutableListOf()
        chatAdapter = ChatAdapter(this, chatList, currentUserId!!) { message -> onMessageLongClick(message) }
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = chatAdapter

        listenForMessages(chatRecyclerView)

        listenForScreenshotAlerts()

        sendIcon.setOnClickListener {
            val message = messageBox.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message)
                messageBox.text.clear()
            }
        }

        // Camera
        cameraIcon.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, CAMERA_REQUEST_CODE)
            }
        }

        // Gallery
        galleryIcon.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, GALLERY_REQUEST_CODE)
        }

        videoCallIcon.setOnClickListener {
            val options = arrayOf("Video Call", "Audio Call")
            AlertDialog.Builder(this)
                .setTitle("Choose Call Type")
                .setItems(options) { _, which ->
                    val callIntent = Intent(this, MainActivity10::class.java)
                    callIntent.putExtra("uid", selectedUserId)
                    callIntent.putExtra("username", userName)
                    callIntent.putExtra("isVideo", which == 0)
                    startActivity(callIntent)
                }.show()
        }

        // Back
        backArrow.setOnClickListener { finish() }


        setupScreenshotDetection()
    }

    private fun sendMessage(message: String, imageUrl: String? = null) {
        val currentUser = auth.currentUser
        if (currentUser == null || selectedUserId == null) return

        val messageId = database.reference.push().key ?: return

        val msgObj = Message(
            messageId = messageId,
            senderId = currentUser.uid,
            receiverId = selectedUserId!!,
            message = message,
            imageUrl = imageUrl,
            timestamp = System.currentTimeMillis()
        )

        // Save message
        database.reference.child("messages").child(chatId).child(messageId).setValue(msgObj)
            .addOnSuccessListener {
                // 🔔 Notify the other person
                val preview = if (message.isNotEmpty()) message else "📷 Image sent"
                database.reference.child("notifications")
                    .child(selectedUserId!!)
                    .push()
                    .setValue(mapOf("title" to "New Message", "message" to preview))
            }
    }

    private fun listenForMessages(chatRecyclerView: RecyclerView) {
        database.reference.child("messages").child(chatId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    chatList.clear()
                    for (data in snapshot.children) {
                        val msg = data.getValue(Message::class.java)
                        if (msg != null) chatList.add(msg)
                    }
                    chatAdapter.notifyDataSetChanged()
                    chatRecyclerView.scrollToPosition(chatList.size - 1)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun setupScreenshotDetection() {
        val screenshotDirs = listOf("screenshots", "screenshot", "screen_shot", "screencapture")
        var lastScreenshotTime = 0L

        screenshotObserver = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)
                if (uri == null || selectedUserId == null || currentUserId == null) return

                val projection = arrayOf(
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.DATE_ADDED
                )

                val cursor = contentResolver.query(
                    uri,
                    projection,
                    null,
                    null,
                    MediaStore.Images.Media.DATE_ADDED + " DESC"
                )

                cursor?.use {
                    if (it.moveToFirst()) {
                        val pathIndex = it.getColumnIndex(MediaStore.Images.Media.DATA)
                        if (pathIndex != -1) {
                            val filePath = it.getString(pathIndex)?.lowercase() ?: return
                            val isScreenshot = screenshotDirs.any { dir -> filePath.contains(dir) }

                            val now = System.currentTimeMillis()
                            if (isScreenshot && now - lastScreenshotTime > 3000) {
                                lastScreenshotTime = now

                                val alertRef = database.reference
                                    .child("screenshotAlerts")
                                    .child(selectedUserId!!)
                                    .child(currentUserId!!)

                                alertRef.get().addOnSuccessListener { snapshot ->
                                    val alreadyShown = snapshot.child("status").getValue(String::class.java) == "shown"
                                    if (!alreadyShown) {
                                        val userRef = database.reference
                                            .child("users")
                                            .child(currentUserId!!)
                                            .child("username")

                                        userRef.get().addOnSuccessListener { userSnap ->
                                            val currentUsername = userSnap.getValue(String::class.java) ?: "Someone"

                                            alertRef.setValue(
                                                mapOf(
                                                    "from" to currentUserId!!,
                                                    "to" to selectedUserId!!,
                                                    "username" to currentUsername,
                                                    "message" to "$currentUsername took a screenshot in chat",
                                                    "sent" to true,
                                                    "received" to false,
                                                    "status" to "shown"
                                                )
                                            )

                                            database.reference.child("notifications")
                                                .child(selectedUserId!!)
                                                .push()
                                                .setValue(
                                                    mapOf(
                                                        "title" to "Screenshot Alert",
                                                        "message" to "$currentUsername took a screenshot in chat"
                                                    )
                                                )

                                            PushNotifications.showNotification(
                                                this@MainActivity8,
                                                "Screenshot Alert",
                                                "$currentUsername took a screenshot in chat"
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            screenshotObserver!!
        )
    }



    private fun listenForScreenshotAlerts() {
        if (currentUserId == null) return

        database.reference.child("screenshotAlerts").child(currentUserId!!)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val msg = snapshot.child("message").getValue(String::class.java) ?: "Screenshot taken!"
                    PushNotifications.showNotification(
                        this@MainActivity8,
                        "Privacy Alert",
                        msg
                    )
                }
                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}
            })

        // Listen for message notifications (incoming messages)
        database.reference.child("notifications").child(currentUserId!!)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val title = snapshot.child("title").getValue(String::class.java) ?: "New Message"
                    val message = snapshot.child("message").getValue(String::class.java) ?: ""
                    PushNotifications.showNotification(this@MainActivity8, title, message)
                }
                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun uploadImage(uri: Uri) {
        val storageRef = storage.reference.child("chat_images/${System.currentTimeMillis()}.jpg")
        storageRef.putFile(uri).addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { url ->
                sendMessage("", url.toString())
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY_REQUEST_CODE) {
                imageUri = data?.data
                imageUri?.let { uploadImage(it) }
            }
        }
    }

    private fun onMessageLongClick(message: Message) {
        if (message.senderId != currentUserId) return
        val currentTime = System.currentTimeMillis()
        if (currentTime - message.timestamp > 5 * 60 * 1000) {
            AlertDialog.Builder(this)
                .setTitle("Action not allowed")
                .setMessage("You can only edit or delete messages within 5 minutes.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        val options = arrayOf("Edit", "Delete")
        AlertDialog.Builder(this)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> editMessage(message)
                    1 -> deleteMessage(message)
                }
            }.show()
    }

    private fun editMessage(message: Message) {
        val input = EditText(this)
        input.setText(message.message)
        AlertDialog.Builder(this)
            .setTitle("Edit Message")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newText = input.text.toString()
                database.reference.child("messages").child(chatId)
                    .child(message.messageId)
                    .child("message").setValue(newText)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteMessage(message: Message) {
        database.reference.child("messages").child(chatId)
            .child(message.messageId)
            .removeValue()
    }

    override fun onDestroy() {
        super.onDestroy()
        screenshotObserver?.let { contentResolver.unregisterContentObserver(it) }
    }
}

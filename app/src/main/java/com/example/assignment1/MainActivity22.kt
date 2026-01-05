package com.example.assignment1

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity22 : AppCompatActivity() {

    private lateinit var ivProfilePic: ImageView
    private lateinit var tvDisplayName: TextView
    private lateinit var tvUsername: TextView
    private lateinit var tvBio: TextView
    private lateinit var tvFollowers: TextView
    private lateinit var tvFollowing: TextView
    private lateinit var btnFollow: TextView

    private val db = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()
    private lateinit var currentUid: String
    private lateinit var targetUid: String
    private lateinit var currentUsername: String
    private lateinit var targetUsername: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main22)

        ivProfilePic = findViewById(R.id.iv_profile_picture)
        tvDisplayName = findViewById(R.id.tv_display_name)
        tvUsername = findViewById(R.id.tv_username_small)
        tvBio = findViewById(R.id.tv_bio_text)
        tvFollowers = findViewById(R.id.tv_followers_count)
        tvFollowing = findViewById(R.id.tv_following_count)
        btnFollow = findViewById(R.id.following)

        currentUid = auth.currentUser?.uid ?: return
        targetUid = intent.getStringExtra("uid") ?: return

        findViewById<ImageView>(R.id.iv_back_arrow).setOnClickListener { finish() }

        getUsernames {
            loadProfileData()
            loadFollowCounts()
            checkFollowStatus()
            setupFollowButton()
            setupFollowListClicks()
        }
    }

    private fun getUsernames(onComplete: () -> Unit) {
        val currentUserRef = db.child("users").child(currentUid).child("username")
        val targetUserRef = db.child("users").child(targetUid).child("username")

        currentUserRef.get().addOnSuccessListener { curSnap ->
            currentUsername = curSnap.getValue(String::class.java) ?: return@addOnSuccessListener
            targetUserRef.get().addOnSuccessListener { tarSnap ->
                targetUsername = tarSnap.getValue(String::class.java) ?: return@addOnSuccessListener
                onComplete()
            }
        }
    }

    private fun loadProfileData() {
        db.child("users").child(targetUid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tvDisplayName.text = snapshot.child("full name").getValue(String::class.java) ?: ""
                tvUsername.text = "@${snapshot.child("username").getValue(String::class.java) ?: ""}"
                tvBio.text = snapshot.child("bio").getValue(String::class.java) ?: ""

                val imageData = snapshot.child("profileImage").getValue(String::class.java)
                if (!imageData.isNullOrEmpty()) {
                    try {
                        val bytes = Base64.decode(imageData, Base64.DEFAULT)
                        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        ivProfilePic.setImageBitmap(bmp)
                    } catch (_: Exception) {}
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun loadFollowCounts() {
        db.child("followers").child(targetUsername)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    tvFollowers.text = snapshot.childrenCount.toString()
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        db.child("following").child(targetUsername)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    tvFollowing.text = snapshot.childrenCount.toString()
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun checkFollowStatus() {
        db.child("followers").child(targetUsername).child(currentUsername)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    btnFollow.text = if (snapshot.exists()) "Following" else "Follow"
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun setupFollowButton() {
        btnFollow.setOnClickListener {
            when (btnFollow.text) {
                "Follow" -> sendFollowRequest()
                "Following" -> unfollowUser()
            }
        }
    }

    private fun sendFollowRequest() {
        val requestData = mapOf(
            "senderUsername" to currentUsername,
            "status" to "pending"
        )

        db.child("followRequests").child(targetUid).child(currentUid)
            .setValue(requestData)
            .addOnSuccessListener {
                Toast.makeText(this, "Follow request sent", Toast.LENGTH_SHORT).show()
                btnFollow.text = "Requested"

                PushNotifications.showNotification(
                    this,
                    "New Follow Request",
                    "$currentUsername sent you a follow request"
                )
            }
    }

    private fun unfollowUser() {
        db.child("followers").child(targetUsername).child(currentUsername).removeValue()
        db.child("following").child(currentUid).child(targetUsername).removeValue()
        Toast.makeText(this, "Unfollowed", Toast.LENGTH_SHORT).show()
        btnFollow.text = "Follow"
    }

    private fun setupFollowListClicks() {
        tvFollowers.setOnClickListener {
            val intent = Intent(this, FollowListActivity::class.java)
            intent.putExtra("uid", targetUid)
            intent.putExtra("type", "followers")
            startActivity(intent)
        }
        tvFollowing.setOnClickListener {
            val intent = Intent(this, FollowListActivity::class.java)
            intent.putExtra("uid", targetUid)
            intent.putExtra("type", "following")
            startActivity(intent)
        }
    }


}

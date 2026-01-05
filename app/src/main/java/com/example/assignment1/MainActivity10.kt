package com.example.assignment1

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig

class MainActivity10 : AppCompatActivity() {

    private var rtcEngine: RtcEngine? = null
    private val appId = "81ab927354674031adc4434e0146190a"
    private val channelName = "testChannel"
    private val uid = 0
    private lateinit var targetUid: String
    private lateinit var username: String
    private lateinit var endCallButton: Button
    private lateinit var callStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main10)
        targetUid = intent.getStringExtra("targetUid") ?: return
        username = intent.getStringExtra("username") ?: "User"
        endCallButton = findViewById(R.id.end_call_button)
        callStatus = findViewById(R.id.call_status)
        callStatus.text = "Calling $username..."
        endCallButton.setOnClickListener {
            rtcEngine?.leaveChannel()
            RtcEngine.destroy()
            rtcEngine = null
            val intent = Intent(this, MainActivity8::class.java)
            startActivity(intent)
            finish()
        }
        checkPermissionAndInit()
    }

    private fun checkPermissionAndInit() {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
        )
        val denied = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (denied.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, denied.toTypedArray(), 100)
        } else {
            initializeAgoraEngine()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            initializeAgoraEngine()
        }
    }

    private fun initializeAgoraEngine() {
        val config = RtcEngineConfig().apply {
            mContext = this@MainActivity10
            mAppId = appId
            mEventHandler = object : IRtcEngineEventHandler() {
                override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                    runOnUiThread { callStatus.text = "Connected to $username" }
                }

                override fun onUserJoined(uid: Int, elapsed: Int) {
                    runOnUiThread { callStatus.text = "$username joined the call" }
                }

                override fun onUserOffline(uid: Int, reason: Int) {
                    runOnUiThread {
                        callStatus.text = "$username left the call"
                        endCallButton.performClick()
                    }
                }
            }
        }
        rtcEngine = RtcEngine.create(config)
        rtcEngine?.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)
        rtcEngine?.enableAudio()
        rtcEngine?.joinChannel(null, channelName, "", uid)
        sendCallNotification()
    }

    private fun sendCallNotification() {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseDatabase.getInstance().getReference("users").child(currentUid)
        db.get().addOnSuccessListener { snapshot ->
            val callerName = snapshot.child("username").getValue(String::class.java) ?: "Someone"
            val ref = FirebaseDatabase.getInstance().getReference("notifications").child(targetUid)
            ref.push().setValue(mapOf("title" to "Incoming Call", "message" to "$callerName is calling you"))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        rtcEngine?.leaveChannel()
        RtcEngine.destroy()
        rtcEngine = null
    }
}

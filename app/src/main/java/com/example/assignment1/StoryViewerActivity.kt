package com.example.assignment1

import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import com.bumptech.glide.Glide

class StoryViewerActivity : AppCompatActivity() {

    private lateinit var storyImageView: ImageView
    private lateinit var usernameTextView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var closeButton: ImageView
    private lateinit var gestureDetector: GestureDetectorCompat

    private var imageUrls: Array<String> = emptyArray()
    private var currentIndex = 0
    private var username = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story_viewer)

        username = intent.getStringExtra("username") ?: "User"
        imageUrls = intent.getStringArrayExtra("imageUrls") ?: emptyArray()

        storyImageView = findViewById(R.id.storyImageView)
        usernameTextView = findViewById(R.id.storyUsername)
        progressBar = findViewById(R.id.storyProgress)
        closeButton = findViewById(R.id.closeStory)

        usernameTextView.text = username

        gestureDetector = GestureDetectorCompat(this, StoryGestureListener())

        closeButton.setOnClickListener {
            finish()
        }

        if (imageUrls.isNotEmpty()) {
            showStory(0)
        }
    }

    private fun showStory(index: Int) {
        if (index < 0 || index >= imageUrls.size) {
            finish() // Close when no more stories
            return
        }

        currentIndex = index

        progressBar.progress = ((currentIndex + 1) * 100) / imageUrls.size

        try {
            val imageUrl = imageUrls[currentIndex]
            if (imageUrl.isNotEmpty()) {
                Glide.with(this)
                    .load(imageUrl)
                    .into(storyImageView)
                Log.d("StoryViewer", "Showing story ${currentIndex + 1}/${imageUrls.size}")
            } else {
                Log.e("StoryViewer", "Empty image URL")
            }
        } catch (e: Exception) {
            Log.e("StoryViewer", "Error loading story: ${e.message}", e)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (gestureDetector.onTouchEvent(event)) {
            true
        } else {
            super.onTouchEvent(event)
        }
    }

    inner class StoryGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            val screenWidth = storyImageView.width
            val tapX = e.x

            if (tapX < screenWidth / 2) {
                showStory(currentIndex - 1)
            } else {
                if (currentIndex + 1 >= imageUrls.size) {
                    finish() // Close if no more stories
                } else {
                    showStory(currentIndex + 1)
                }
            }
            return true
        }

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }
    }
}
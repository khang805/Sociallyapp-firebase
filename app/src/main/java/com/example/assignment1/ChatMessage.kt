package com.example.assignment1

import java.util.concurrent.TimeUnit

data class ChatMessage(
    val id: Int,
    val sender_id: Int,
    val receiver_id: Int,
    val message_text: String?, // Nullable for media messages
    val media_type: String, // "text", "image", "video", "file"
    val media_url: String?, // URL for media messages
    val is_seen: Boolean,
    val is_vanish_mode: Boolean,
    val created_at: String,
    val updated_at: String,
    val sender_username: String,
    val sender_first_name: String,
    val sender_last_name: String,
    val sender_profile_photo_url: String?
) {
    // Helper property to check if the message can be edited/deleted (e.g., within 5 minutes)
    val canEditOrDelete: Boolean
        get() {
            try {
                // Assuming created_at is in format "yyyy-MM-dd HH:mm:ss" (as formatted in ChatActivity)
                val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                val messageDate = format.parse(created_at) ?: return false
                val currentTime = java.util.Date()
                val diff = currentTime.time - messageDate.time
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
                return minutes < 5 && sender_id != 0 // Only sender can edit/delete
            } catch (e: Exception) {
                return false
            }
        }
}
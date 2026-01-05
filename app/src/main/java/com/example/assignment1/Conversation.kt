package com.example.assignment1

data class Conversation(
    val message_id: Int = 0,
    val other_user_id: Int = 0,
    val other_username: String = "",
    val other_first_name: String = "",
    val other_last_name: String = "",
    val other_profile_photo_url: String? = null,
    val last_message_text: String? = null,
    val last_message_media_type: String = "text",
    val last_message_media_url: String? = null,
    val last_message_time: String = "",
    val unread_count: Int = 0
) {
    val other_user_display_name: String
        get() = "$other_first_name $other_last_name".trim().ifEmpty { other_username }
    
    val last_message_preview: String
        get() = when (last_message_media_type) {
            "image" -> "📷 Photo"
            "video" -> "🎥 Video"
            "file" -> "📎 File"
            else -> last_message_text ?: ""
        }
}


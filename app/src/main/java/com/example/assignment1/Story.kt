package com.example.assignment1

data class Story(
    val id: Int = 0,
    val user_id: Int = 0,
    val media_url: String = "",
    val media_type: String = "image", // "image" or "video"
    val created_at: String = "",
    val expires_at: String = ""
) {
    // Compatibility properties for existing code
    val userId: String get() = user_id.toString()
    val imageUrl: String get() = media_url
    val timestamp: Long get() = try {
        java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            .parse(created_at)?.time ?: 0L
    } catch (e: Exception) {
        0L
    }
}

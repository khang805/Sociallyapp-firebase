package com.example.assignment1

data class Post(
    val id: Int = 0,
    val user_id: Int = 0,
    val image_url: String = "",
    val caption: String = "",
    val username: String = "",
    val first_name: String = "",
    val last_name: String = "",
    val profile_photo_url: String? = null,
    val like_count: Int = 0,
    val comment_count: Int = 0,
    val is_liked: Boolean = false
) {
    // Legacy fields for backward compatibility
    val postId: String get() = id.toString()
    val postImage: String get() = image_url
    val description: String get() = caption
    val publisher: String get() = user_id.toString()
    val timestamp: Long get() = 0L
    val likesCount: Int get() = like_count
    val commentsCount: Int get() = comment_count
}
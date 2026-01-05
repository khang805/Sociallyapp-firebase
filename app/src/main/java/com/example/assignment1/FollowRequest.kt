package com.example.assignment1

data class FollowRequest(
    val id: Int = 0,
    val sender_id: Int = 0,
    val username: String = "",
    val first_name: String = "",
    val last_name: String = "",
    val profile_photo_url: String? = null,
    val created_at: String = "",
    var status: String = "pending"
) {
    val displayName: String get() = "$first_name $last_name".trim().ifEmpty { username }
    val senderUsername: String get() = username
    val profileImage: String? get() = profile_photo_url
    val senderUid: String get() = sender_id.toString()
}

package com.example.assignment1

data class SearchUser(
    val id: Int = 0,
    val username: String = "",
    val first_name: String = "",
    val last_name: String = "",
    val profile_photo_url: String? = null
) {
    val uid: String get() = id.toString()
    val name: String get() = "$first_name $last_name".trim()
    val profileImageUrl: String get() = profile_photo_url ?: ""
}

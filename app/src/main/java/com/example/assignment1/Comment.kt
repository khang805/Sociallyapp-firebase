package com.example.assignment1

data class Comment(
    val id: Int = 0,
    val user_id: Int = 0,
    val comment: String = "",
    val username: String = "",
    val first_name: String = "",
    val last_name: String = ""
) {
    // Legacy field for backward compatibility
    val publisher: String get() = user_id.toString()
}
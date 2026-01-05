package com.example.assignment1

data class Message(
    val messageId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    var message: String = "",
    val imageUrl: String? = null,
    val timestamp: Long = 0L
)

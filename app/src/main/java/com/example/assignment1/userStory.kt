package com.example.assignment1

data class userStory(
    val userId: String = "",
    val username: String = "",
    val stories: List<Story> = emptyList(),
    val latestImageUrl: String = "", // The most recent story image to display
    val profilePhotoUrl: String? = null
)

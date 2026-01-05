package com.example.assignment1

import android.util.Log
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

object ApiService {
    // Ensure the BASE_URL is correct for your local server access
    private const val BASE_URL = "http://192.168.100.139/assignment-03/"
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    private const val TAG = "ApiService" // Tag for logging

    interface ApiCallback {
        fun onSuccess(response: String)
        fun onError(error: String)
    }

    // --- Utility Function for Logging and Response Handling ---
    private fun handleResponse(
        call: Call,
        response: Response,
        callback: ApiCallback,
        endpoint: String
    ) {
        val responseBody = response.body?.string() ?: ""
        if (response.isSuccessful) {
            Log.d(TAG, "$endpoint Success: $responseBody")
            callback.onSuccess(responseBody)
        } else {
            // Log the HTTP code and the response body for debugging 404/500 errors
            Log.e(TAG, "$endpoint Server Error ${response.code}: $responseBody")
            callback.onError("Server error ${response.code}: $responseBody")
        }
    }

    private fun handleFailure(call: Call, e: IOException, callback: ApiCallback, endpoint: String) {
        Log.e(TAG, "$endpoint Network Error: ${e.message}", e)
        callback.onError("Network error: ${e.message}")
    }
    // ------------------------------------

    // Login API
    fun login(email: String, password: String, callback: ApiCallback) {
        val endpoint = "login.php"
        val formBody = FormBody.Builder()
            .add("email", email)
            .add("password", password)
            .build()

        val request = Request.Builder()
            .url("${BASE_URL}$endpoint")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) =
                handleFailure(call, e, callback, endpoint)

            override fun onResponse(call: Call, response: Response) =
                handleResponse(call, response, callback, endpoint)
        })
    }

    // Signup API
    fun signup(
        username: String,
        first_name: String,
        last_name: String,
        dob: String,
        email: String,
        password: String,
        callback: ApiCallback
    ) {
        val endpoint = "signup.php"
        val formBody = FormBody.Builder()
            .add("username", username)
            .add("first_name", first_name)
            .add("last_name", last_name)
            .add("dob", dob)
            .add("email", email)
            .add("password", password)
            .build()

        val request = Request.Builder()
            .url("${BASE_URL}$endpoint")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) =
                handleFailure(call, e, callback, endpoint)

            override fun onResponse(call: Call, response: Response) =
                handleResponse(call, response, callback, endpoint)
        })
    }

    // Get Posts API
    fun getPosts(userId: Int, callback: ApiCallback) {
        val endpoint = "get_posts.php"
        val url = "${BASE_URL}$endpoint?user_id=$userId"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) =
                handleFailure(call, e, callback, endpoint)

            override fun onResponse(call: Call, response: Response) =
                handleResponse(call, response, callback, endpoint)
        })
    }

    // Upload Post API
    fun uploadPost(
        userId: Int,
        imageFile: File,
        caption: String,
        callback: ApiCallback
    ) {
        val endpoint = "upload_post.php"
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("user_id", userId.toString())
            .addFormDataPart("caption", caption)
            .addFormDataPart(
                "image",
                imageFile.name,
                imageFile.asRequestBody("image/*".toMediaType())
            )
            .build()

        val request = Request.Builder()
            .url("${BASE_URL}$endpoint")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) =
                handleFailure(call, e, callback, endpoint)

            override fun onResponse(call: Call, response: Response) =
                handleResponse(call, response, callback, endpoint)
        })
    }

    // Like/Unlike Post API
    fun likePost(userId: Int, postId: Int, callback: ApiCallback) {
        val endpoint = "like_post.php"
        val formBody = FormBody.Builder()
            .add("user_id", userId.toString())
            .add("post_id", postId.toString())
            .build()

        val request = Request.Builder()
            .url("${BASE_URL}$endpoint")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) =
                handleFailure(call, e, callback, endpoint)

            override fun onResponse(call: Call, response: Response) =
                handleResponse(call, response, callback, endpoint)
        })
    }

    // Get Comments API
    fun getComments(postId: Int, callback: ApiCallback) {
        val endpoint = "get_comments.php"
        val url = "${BASE_URL}$endpoint?post_id=$postId"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) =
                handleFailure(call, e, callback, endpoint)

            override fun onResponse(call: Call, response: Response) =
                handleResponse(call, response, callback, endpoint)
        })
    }

    // Add Comment API
    fun addComment(userId: Int, postId: Int, comment: String, callback: ApiCallback) {
        val endpoint = "add_comment.php"
        val formBody = FormBody.Builder()
            .add("user_id", userId.toString())
            .add("post_id", postId.toString())
            .add("comment", comment)
            .build()

        val request = Request.Builder()
            .url("${BASE_URL}$endpoint")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) =
                handleFailure(call, e, callback, endpoint)

            override fun onResponse(call: Call, response: Response) =
                handleResponse(call, response, callback, endpoint)
        })
    }

    // Get User Profile API
    fun getUserProfile(userId: Int, callback: ApiCallback) {
        // --- PATH CORRECTED: Removed 'apis/' ---
        val endpoint = "get_user_profile.php"
        val url = "${BASE_URL}$endpoint?user_id=$userId"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        Log.d(TAG, "GET $endpoint: $url") // Keep logging for debugging

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) =
                handleFailure(call, e, callback, endpoint)

            override fun onResponse(call: Call, response: Response) =
                handleResponse(call, response, callback, endpoint)
        })
    }

    // Upload Profile Photo API
    fun uploadProfilePhoto(userId: Int, imageFile: File, callback: ApiCallback) {
        // --- PATH CORRECTED: Removed 'apis/' ---
        val endpoint = "upload_profile_photo.php"
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("user_id", userId.toString())
            .addFormDataPart(
                "profile_photo",
                imageFile.name,
                imageFile.asRequestBody("image/*".toMediaType())
            )
            .build()

        val request = Request.Builder()
            .url("${BASE_URL}$endpoint")
            .post(requestBody)
            .build()

        Log.d(TAG, "POST $endpoint: Uploading photo for user $userId")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) =
                handleFailure(call, e, callback, endpoint)

            override fun onResponse(call: Call, response: Response) =
                handleResponse(call, response, callback, endpoint)
        })
    }

    // Update Profile API
    fun updateProfile(
        userId: Int,
        firstName: String? = null,
        lastName: String? = null,
        username: String? = null,
        bio: String? = null,
        website: String? = null,
        phone: String? = null,
        gender: String? = null,
        callback: ApiCallback
    ) {
        // --- PATH CORRECTED: Removed 'apis/' ---
        val endpoint = "update_profile.php"
        val formBuilder = FormBody.Builder()
            .add("user_id", userId.toString())

        firstName?.let { formBuilder.add("first_name", it) }
        lastName?.let { formBuilder.add("last_name", it) }
        username?.let { formBuilder.add("username", it) }
        bio?.let { formBuilder.add("bio", it) }
        website?.let { formBuilder.add("website", it) }
        phone?.let { formBuilder.add("phone", it) }
        gender?.let { formBuilder.add("gender", it) }

        val request = Request.Builder()
            .url("${BASE_URL}$endpoint")
            .post(formBuilder.build())
            .build()

        Log.d(TAG, "POST $endpoint: Updating profile for user $userId")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) =
                handleFailure(call, e, callback, endpoint)

            override fun onResponse(call: Call, response: Response) =
                handleResponse(call, response, callback, endpoint)
        })
    }

    // Search Users API
    fun searchUsers(query: String, currentUserId: Int, callback: ApiCallback) {
        // --- PATH CORRECTED: Removed 'apis/' ---
        val endpoint = "search_users.php"
        val url = "${BASE_URL}$endpoint?query=${
            java.net.URLEncoder.encode(
                query,
                "UTF-8"
            )
        }&current_user_id=$currentUserId"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) =
                handleFailure(call, e, callback, endpoint)

            override fun onResponse(call: Call, response: Response) =
                handleResponse(call, response, callback, endpoint)
        })
    }

    // Send Follow Request API
    fun sendFollowRequest(senderId: Int, receiverId: Int, callback: ApiCallback) {
        // --- PATH CORRECTED: Removed 'apis/' ---
        val endpoint = "send_follow_request.php"
        val formBody = FormBody.Builder()
            .add("sender_id", senderId.toString())
            .add("receiver_id", receiverId.toString())
            .build()

        val request = Request.Builder()
            .url("${BASE_URL}$endpoint")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) =
                handleFailure(call, e, callback, endpoint)

            override fun onResponse(call: Call, response: Response) =
                handleResponse(call, response, callback, endpoint)
        })
    }

    // Accept Follow Request API
    fun acceptFollowRequest(receiverId: Int, senderId: Int, callback: ApiCallback) {
        // --- PATH CORRECTED: Removed 'apis/' ---
        val endpoint = "accept_follow_request.php"
        val formBody = FormBody.Builder()
            .add("receiver_id", receiverId.toString())
            .add("sender_id", senderId.toString())
            .build()

        val request = Request.Builder()
            .url("${BASE_URL}$endpoint")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) =
                handleFailure(call, e, callback, endpoint)

            override fun onResponse(call: Call, response: Response) =
                handleResponse(call, response, callback, endpoint)
        })
    }

    // Reject Follow Request API
    fun rejectFollowRequest(receiverId: Int, senderId: Int, callback: ApiCallback) {
        // --- PATH CORRECTED: Removed 'apis/' ---
        val endpoint = "reject_follow_request.php"
        val formBody = FormBody.Builder()
            .add("receiver_id", receiverId.toString())
            .add("sender_id", senderId.toString())
            .build()

        val request = Request.Builder()
            .url("${BASE_URL}$endpoint")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) =
                handleFailure(call, e, callback, endpoint)

            override fun onResponse(call: Call, response: Response) =
                handleResponse(call, response, callback, endpoint)
        })
    }

    // Unfollow User API
    fun unfollowUser(followerId: Int, followingId: Int, callback: ApiCallback) {
        // --- PATH CORRECTED: Removed 'apis/' ---
        val endpoint = "unfollow_user.php"
        val formBody = FormBody.Builder()
            .add("follower_id", followerId.toString())
            .add("following_id", followingId.toString())
            .build()

        val request = Request.Builder()
            .url("${BASE_URL}$endpoint")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) =
                handleFailure(call, e, callback, endpoint)

            override fun onResponse(call: Call, response: Response) =
                handleResponse(call, response, callback, endpoint)
        })
    }

    // Get Follow Status API
    fun getFollowStatus(followerId: Int, followingId: Int, callback: ApiCallback) {
        // --- PATH CORRECTED: Removed 'apis/' ---
        val endpoint = "get_follow_status.php"
        val url = "${BASE_URL}$endpoint?follower_id=$followerId&following_id=$followingId"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) =
                handleFailure(call, e, callback, endpoint)

            override fun onResponse(call: Call, response: Response) =
                handleResponse(call, response, callback, endpoint)
        })
    }

    // Get Followers API
    fun getFollowers(userId: Int, callback: ApiCallback) {
        // --- PATH CORRECTED: Removed 'apis/' ---
        val endpoint = "get_followers.php"
        val url = "${BASE_URL}$endpoint?user_id=$userId"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) =
                handleFailure(call, e, callback, endpoint)

            override fun onResponse(call: Call, response: Response) =
                handleResponse(call, response, callback, endpoint)
        })
    }

    // Get Following API
    fun getFollowing(userId: Int, callback: ApiCallback) {
        // --- PATH CORRECTED: Removed 'apis/' ---
        val endpoint = "get_following.php"
        val url = "${BASE_URL}$endpoint?user_id=$userId"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) =
                handleFailure(call, e, callback, endpoint)

            override fun onResponse(call: Call, response: Response) =
                handleResponse(call, response, callback, endpoint)
        })
    }

    // Get Follow Requests API
    fun getFollowRequests(userId: Int, callback: ApiCallback) {
        // --- PATH CORRECTED: Removed 'apis/' ---
        val endpoint = "get_follow_requests.php"
        val url = "${BASE_URL}$endpoint?user_id=$userId"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) =
                handleFailure(call, e, callback, endpoint)

            override fun onResponse(call: Call, response: Response) =
                handleResponse(call, response, callback, endpoint)
        })
    }

    // Get Follow Counts API
    fun getFollowCounts(userId: Int, callback: ApiCallback) {
        // --- PATH CORRECTED: Removed 'apis/' ---
        val endpoint = "get_follow_counts.php"
        val url = "${BASE_URL}$endpoint?user_id=$userId"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) =
                handleFailure(call, e, callback, endpoint)

            override fun onResponse(call: Call, response: Response) =
                handleResponse(call, response, callback, endpoint)
        })
    }

    // Upload Story API
    fun uploadStory(userId: Int, mediaFile: File, callback: ApiCallback) {
        val endpoint = "upload_story.php"
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("user_id", userId.toString())
            .addFormDataPart(
                "story_media",
                mediaFile.name,
                mediaFile.asRequestBody("image/*".toMediaType())
            )
            .build()

        val request = Request.Builder()
            .url("${BASE_URL}$endpoint")
            .post(requestBody)
            .build()

        Log.d(TAG, "POST $endpoint: Uploading story for user $userId")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) =
                handleFailure(call, e, callback, endpoint)

            override fun onResponse(call: Call, response: Response) =
                handleResponse(call, response, callback, endpoint)
        })
    }

    // Get Stories API
    fun getStories(currentUserId: Int, callback: ApiCallback) {
        val endpoint = "get_stories.php"
        val url = "${BASE_URL}$endpoint?current_user_id=$currentUserId"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        Log.d(TAG, "GET $endpoint: $url")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) =
                handleFailure(call, e, callback, endpoint)

            override fun onResponse(call: Call, response: Response) =
                handleResponse(call, response, callback, endpoint)
        })
    }

    // ========== MESSAGING APIs ==========

    // Send Message API
    fun sendMessage(
        senderId: Int,
        receiverId: Int,
        messageText: String?,
        mediaType: String,
        mediaFile: File?,
        isVanishMode: Boolean,
        callback: ApiCallback
    ) {
        val endpoint = "send_message.php"
        val requestBodyBuilder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("sender_id", senderId.toString())
            .addFormDataPart("receiver_id", receiverId.toString())
            .addFormDataPart("media_type", mediaType)
            .addFormDataPart("is_vanish_mode", isVanishMode.toString())

        if (!messageText.isNullOrEmpty()) {
            requestBodyBuilder.addFormDataPart("message_text", messageText)
        }

        if (mediaFile != null) {
            val mediaTypeOkHttp = when (mediaType) {
                "image" -> "image/*".toMediaType()
                "video" -> "video/*".toMediaType()
                else -> "application/octet-stream".toMediaType()
            }
            requestBodyBuilder.addFormDataPart(
                "media_file",
                mediaFile.name,
                mediaFile.asRequestBody(mediaTypeOkHttp)
            )
        }

        val request = Request.Builder()
            .url("${BASE_URL}$endpoint")
            .post(requestBodyBuilder.build())
            .build()

        Log.d(TAG, "POST $endpoint: Sending message from $senderId to $receiverId")
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) =
                handleFailure(call, e, callback, endpoint)

            override fun onResponse(call: Call, response: Response) =
                handleResponse(call, response, callback, endpoint)
        })
    }

    // Get Messages API
    fun getMessages(user1Id: Int, user2Id: Int, limit: Int = 50, offset: Int = 0, callback: ApiCallback) {
        val endpoint = "get_messages.php"
        val url = "${BASE_URL}$endpoint?user1_id=$user1Id&user2_id=$user2Id&limit=$limit&offset=$offset"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        Log.d(TAG, "GET $endpoint: $url")
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) =
                handleFailure(call, e, callback, endpoint)

            override fun onResponse(call: Call, response: Response) =
                handleResponse(call, response, callback, endpoint)
        })
    }

    // Get Conversations API
    fun getConversations(userId: Int, callback: ApiCallback) {
        val endpoint = "get_conversations.php"
        val url = "${BASE_URL}$endpoint?user_id=$userId"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        Log.d(TAG, "GET $endpoint: $url")
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) =
                handleFailure(call, e, callback, endpoint)

            override fun onResponse(call: Call, response: Response) =
                handleResponse(call, response, callback, endpoint)
        })
    }

    // Mark Messages as Seen API
    fun markMessagesSeen(userId: Int, otherUserId: Int, callback: ApiCallback) {
        val endpoint = "mark_messages_seen.php"
        val formBody = FormBody.Builder()
            .add("user_id", userId.toString())
            .add("other_user_id", otherUserId.toString())
            .build()

        val request = Request.Builder()
            .url("${BASE_URL}$endpoint")
            .post(formBody)
            .build()

        Log.d(TAG, "POST $endpoint: Marking messages as seen")
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) =
                handleFailure(call, e, callback, endpoint)

            override fun onResponse(call: Call, response: Response) =
                handleResponse(call, response, callback, endpoint)
        })
    }

    // Delete Vanish Mode Messages API
    fun deleteVanishMessages(userId: Int, otherUserId: Int, callback: ApiCallback) {
        val endpoint = "delete_vanish_messages.php"
        val formBody = FormBody.Builder()
            .add("user_id", userId.toString())
            .add("other_user_id", otherUserId.toString())
            .build()

        val request = Request.Builder()
            .url("${BASE_URL}$endpoint")
            .post(formBody)
            .build()

        Log.d(TAG, "POST $endpoint: Deleting vanish mode messages")
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) =
                handleFailure(call, e, callback, endpoint)

            override fun onResponse(call: Call, response: Response) =
                handleResponse(call, response, callback, endpoint)
        })
    }

    // Edit Message API
    fun editMessage(messageId: Int, userId: Int, newText: String, callback: ApiCallback) {
        val endpoint = "edit_message.php"
        val formBody = FormBody.Builder()
            .add("message_id", messageId.toString())
            .add("user_id", userId.toString())
            .add("message_text", newText)
            .build()

        val request = Request.Builder()
            .url("${BASE_URL}$endpoint")
            .post(formBody)
            .build()

        Log.d(TAG, "POST $endpoint: Editing message $messageId")
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) =
                handleFailure(call, e, callback, endpoint)

            override fun onResponse(call: Call, response: Response) =
                handleResponse(call, response, callback, endpoint)
        })
    }

    // Delete Message API
    fun deleteMessage(messageId: Int, userId: Int, callback: ApiCallback) {
        val endpoint = "delete_message.php"
        val formBody = FormBody.Builder()
            .add("message_id", messageId.toString())
            .add("user_id", userId.toString())
            .build()

        val request = Request.Builder()
            .url("${BASE_URL}$endpoint")
            .post(formBody)
            .build()

        Log.d(TAG, "POST $endpoint: Deleting message $messageId")
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) =
                handleFailure(call, e, callback, endpoint)

            override fun onResponse(call: Call, response: Response) =
                handleResponse(call, response, callback, endpoint)
        })
    }
}


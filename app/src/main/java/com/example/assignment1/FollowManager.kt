package com.example.assignment1

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue

object FollowManager {
    private val db: DatabaseReference = FirebaseDatabase.getInstance().reference

    fun sendFollowRequest(fromUid: String, fromUsername: String, toUid: String, toUsername: String, callback: (Boolean) -> Unit) {
        val requestData = hashMapOf(
            "senderUid" to fromUid,
            "senderUsername" to fromUsername,
            "receiverUid" to toUid,
            "receiverUsername" to toUsername,
            "timestamp" to ServerValue.TIMESTAMP
        )

        db.child("followRequests").child(toUid).child(fromUid)
            .setValue(requestData)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun acceptFollowRequest(currentUid: String, currentUsername: String, fromUid: String, fromUsername: String, callback: (Boolean) -> Unit) {
        val followerData = hashMapOf(
            "uid" to fromUid,
            "username" to fromUsername,
            "timestamp" to ServerValue.TIMESTAMP
        )

        val followingData = hashMapOf(
            "uid" to currentUid,
            "username" to currentUsername,
            "timestamp" to ServerValue.TIMESTAMP
        )

        val updates = hashMapOf<String, Any?>(
            "followers/$currentUid/$fromUid" to followerData,
            "following/$fromUid/$currentUid" to followingData,
            "followRequests/$currentUid/$fromUid" to null
        )

        db.updateChildren(updates)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun rejectFollowRequest(currentUid: String, fromUid: String, callback: (Boolean) -> Unit) {
        db.child("followRequests").child(currentUid).child(fromUid).removeValue()
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun unfollowUser(fromUid: String, toUid: String, callback: (Boolean) -> Unit) {
        val updates = hashMapOf<String, Any?>(
            "following/$fromUid/$toUid" to null,
            "followers/$toUid/$fromUid" to null
        )
        db.updateChildren(updates)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun isFollowing(fromUid: String, toUid: String, callback: (Boolean) -> Unit) {
        db.child("following").child(fromUid).child(toUid)
            .get()
            .addOnSuccessListener { snapshot: DataSnapshot -> callback(snapshot.exists()) }
            .addOnFailureListener { callback(false) }
    }

    fun getFollowerCount(uid: String, callback: (Int) -> Unit) {
        db.child("followers").child(uid)
            .get()
            .addOnSuccessListener { snapshot: DataSnapshot ->
                callback(snapshot.childrenCount.toInt())
            }
            .addOnFailureListener { callback(0) }
    }

    fun getFollowingCount(uid: String, callback: (Int) -> Unit) {
        db.child("following").child(uid)
            .get()
            .addOnSuccessListener { snapshot: DataSnapshot ->
                callback(snapshot.childrenCount.toInt())
            }
            .addOnFailureListener { callback(0) }
    }
}

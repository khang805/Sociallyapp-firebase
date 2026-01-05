package com.example.assignment1

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.JsonParser

class FollowListAdapter(
    private val userList: MutableList<User>,
    private val currentUserId: Int
) : RecyclerView.Adapter<FollowListAdapter.FollowingViewHolder>() {

    class FollowingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: ImageView = view.findViewById(R.id.followingProfileImage)
        val nameText: TextView = view.findViewById(R.id.followingName)
        val usernameText: TextView = view.findViewById(R.id.followingUsername)
        val unfollowButton: Button = view.findViewById(R.id.unfollowButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_following_user, parent, false)
        return FollowingViewHolder(view)
    }

    override fun onBindViewHolder(holder: FollowingViewHolder, position: Int) {
        val user = userList[position]
        val fullName = user.fullName
        holder.nameText.text = if (!fullName.isNullOrEmpty()) fullName else user.username
        holder.usernameText.text = "@${user.username}"

        // Load profile image using Glide from URL
        val profileImageUrl = user.profileImage
        if (!profileImageUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(profileImageUrl)
                .placeholder(R.drawable.ic_profile_blue)
                .error(R.drawable.ic_profile_blue)
                .circleCrop()
                .into(holder.profileImage)
        } else {
            holder.profileImage.setImageResource(R.drawable.ic_profile_blue)
        }

        // Navigate to profile when clicking on user
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val userId = user.uid.toIntOrNull() ?: 0
            if (userId > 0) {
                val intent = Intent(context, OtherProfileActivity::class.java)
                intent.putExtra("user_id", userId)
                context.startActivity(intent)
            }
        }

        // Handle unfollow button
        holder.unfollowButton.setOnClickListener {
            val userId = user.uid.toIntOrNull() ?: 0
            if (userId > 0 && currentUserId > 0) {
                unfollowUser(holder, position, currentUserId, userId)
            }
        }
    }

    override fun getItemCount() = userList.size

    private fun unfollowUser(holder: FollowingViewHolder, position: Int, followerId: Int, followingId: Int) {
        ApiService.unfollowUser(followerId, followingId, object : ApiService.ApiCallback {
            override fun onSuccess(response: String) {
                (holder.itemView.context as? AppCompatActivity)?.runOnUiThread {
                    try {
                        val jsonObject = JsonParser.parseString(response).asJsonObject
                        if (jsonObject.get("status")?.asString == "success") {
                            userList.removeAt(position)
                            notifyItemRemoved(position)
                            notifyItemRangeChanged(position, userList.size)
                            Toast.makeText(holder.itemView.context, "Unfollowed", Toast.LENGTH_SHORT).show()
                        } else {
                            val message = jsonObject.get("message")?.asString ?: "Failed to unfollow"
                            Toast.makeText(holder.itemView.context, message, Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(holder.itemView.context, "Error unfollowing user", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onError(error: String) {
                (holder.itemView.context as? AppCompatActivity)?.runOnUiThread {
                    Toast.makeText(holder.itemView.context, error, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}
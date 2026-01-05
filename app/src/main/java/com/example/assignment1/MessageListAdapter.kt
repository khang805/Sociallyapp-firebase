package com.example.assignment1

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MessageListAdapter(private val userList: MutableList<User>) :
    RecyclerView.Adapter<MessageListAdapter.UserViewHolder>() {

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: ImageView = view.findViewById(R.id.followingProfileImage)
        val nameText: TextView = view.findViewById(R.id.followingName)
        val usernameText: TextView = view.findViewById(R.id.followingUsername)
        val messageButton: Button = view.findViewById(R.id.messageButton)
        val statusText: TextView = view.findViewById(R.id.statusText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        // Assuming item_chat_message.xml is the correct layout for the list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        // Assuming User class has fullName and username properties
        holder.nameText.text = user.fullName
        holder.usernameText.text = "@${user.username}"

        // Assuming user.profileImage holds the URL
        if (!user.profileImage.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(user.profileImage)
                .placeholder(R.drawable.ic_profile)
                .into(holder.profileImage)
        } else {
            holder.profileImage.setImageResource(R.drawable.ic_profile)
        }

        if (user.status == "online") {
            holder.statusText.text = "Online"
            holder.statusText.setTextColor(holder.itemView.context.getColor(android.R.color.holo_green_dark))
        } else {
            holder.statusText.text = "Offline"
            holder.statusText.setTextColor(holder.itemView.context.getColor(android.R.color.darker_gray))
        }

        val context = holder.itemView.context

        // FIX: Update intent to pass required data to ChatActivity
        val openChat = {
            // NOTE: I am assuming ChatActivity is the correct class and not MainActivity8
            val intent = Intent(context, ChatActivity::class.java)

            // Assuming your User data class has these properties:
            intent.putExtra("other_user_id", user.uid) // Assuming 'id' is the Int ID required
            intent.putExtra("other_username", user.username)
            intent.putExtra("other_profile_photo_url", user.profileImage)

            context.startActivity(intent)
        }

        holder.itemView.setOnClickListener { openChat() }
        holder.messageButton.setOnClickListener { openChat() }
    }

    override fun getItemCount() = userList.size

    // NOTE: You are referencing a 'User' data class that was not provided.
    // I am assuming it has 'id: Int', 'username: String', 'fullName: String',
    // 'firstName: String', 'lastName: String', and 'profileImage: String?'
}
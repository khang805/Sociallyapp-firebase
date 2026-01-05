package com.example.assignment1

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class UserSearchAdapter(private val context: Context, private var userList: List<SearchUser>) :
    RecyclerView.Adapter<UserSearchAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.search_item, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.tvUsername.text = user.username
        holder.tvDisplayName.text = user.name

        // Load image using Glide from the remote URL
        if (user.profileImageUrl.isNotEmpty()) {
            Glide.with(context)
                .load(user.profileImageUrl)
                .placeholder(R.drawable.ic_profile_blue)
                .error(R.drawable.ic_profile_blue)
                .circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.ivProfile)
        } else {
            holder.ivProfile.setImageResource(R.drawable.ic_profile_blue)
        }

        // Set click listener on the entire item view to open profile
        holder.itemView.setOnClickListener {
            // Validate user ID before opening profile
            if (user.id > 0) {
                val intent = Intent(context, OtherProfileActivity::class.java)
                intent.putExtra("user_id", user.id)
                context.startActivity(intent)
            }
        }
        
        // Make sure the item view is clickable
        holder.itemView.isClickable = true
        holder.itemView.isFocusable = true
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    fun updateUsers(users: List<SearchUser>) {
        this.userList = users
        notifyDataSetChanged()
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProfile: ImageView = itemView.findViewById(R.id.ivProfile)
        val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        val tvDisplayName: TextView = itemView.findViewById(R.id.tvDisplayName)
    }
}
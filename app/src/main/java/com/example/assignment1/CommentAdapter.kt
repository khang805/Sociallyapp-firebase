package com.example.assignment1

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView

class CommentAdapter(private val mContext: Context, private val mComment: MutableList<Comment>) :
    RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.comment_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = mComment[position]
        holder.comment.text = comment.comment
        getUserInfo(holder.imageProfile, holder.username, comment)
    }

    override fun getItemCount(): Int {
        return mComment.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageProfile: CircleImageView = itemView.findViewById(R.id.image_profile)
        var username: TextView = itemView.findViewById(R.id.username)
        var comment: TextView = itemView.findViewById(R.id.comment)
    }

    private fun getUserInfo(imageProfile: CircleImageView, username: TextView, comment: Comment) {
        // Set username from comment data
        username.text = comment.username.ifEmpty { 
            "${comment.first_name} ${comment.last_name}".trim().ifEmpty { "User" }
        }
        
        // Set profile image placeholder (can be enhanced later with actual profile images)
        imageProfile.setImageResource(R.drawable.ic_profile_vector)
    }
}
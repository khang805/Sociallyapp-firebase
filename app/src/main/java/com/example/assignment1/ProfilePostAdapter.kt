package com.example.assignment1

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ProfilePostAdapter(private val context: Context, private val postList: List<Post>) :
    RecyclerView.Adapter<ProfilePostAdapter.PostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.profile_post_item, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]

        if (post.postImage.isNotEmpty()) {
            val imageBytes = Base64.decode(post.postImage, Base64.DEFAULT)
            val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            holder.postImage.setImageBitmap(decodedImage)
        } else {
            // Optionally, set a placeholder if there is no image
            holder.postImage.setImageResource(R.drawable.ic_placeholder)
        }
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postImage: ImageView = itemView.findViewById(R.id.post_image)
    }
}

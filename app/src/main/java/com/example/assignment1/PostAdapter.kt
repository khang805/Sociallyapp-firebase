package com.example.assignment1

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.assignment1.databinding.PostItemBinding

class PostAdapter(private val context: Context, private val postList: List<Post>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = PostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        holder.bind(post)
    }

    override fun getItemCount(): Int = postList.size

    inner class PostViewHolder(private val binding: PostItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post) {
            // Load post image using Glide
            if (post.image_url.isNotEmpty()) {
                Glide.with(context)
                    .load(post.image_url)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .into(binding.postImage)
            } else {
                binding.postImage.setImageResource(R.drawable.ic_placeholder)
            }

            binding.postDescription.text = post.caption
            binding.postUsername.text = post.username

            // Load profile photo
            if (!post.profile_photo_url.isNullOrEmpty()) {
                Glide.with(context)
                    .load(post.profile_photo_url)
                    .placeholder(R.drawable.ic_profile_vector)
                    .error(R.drawable.ic_profile_vector)
                    .circleCrop()
                    .into(binding.postProfileImage)
            } else {
                binding.postProfileImage.setImageResource(R.drawable.ic_profile_vector)
            }

            // Set like button state
            if (post.is_liked) {
                binding.postLikeButton.setImageResource(R.drawable.heart_filled)
                binding.postLikeButton.tag = "liked"
            } else {
                binding.postLikeButton.setImageResource(R.drawable.ic_heart)
                binding.postLikeButton.tag = "like"
            }

            // Set like and comment counts
            binding.postLikes.text = "${post.like_count} likes"
            binding.postComments.text = "View All ${post.comment_count} Comments"

            binding.postLikeButton.setOnClickListener {
                val sessionManager = SessionManager(context)
                val userId = sessionManager.getUserId()
                
                if (userId == -1) {
                    return@setOnClickListener
                }

                ApiService.likePost(userId, post.id, object : ApiService.ApiCallback {
                    override fun onSuccess(response: String) {
                        (context as? androidx.appcompat.app.AppCompatActivity)?.runOnUiThread {
                            try {
                                val jsonObject = com.google.gson.JsonParser.parseString(response).asJsonObject
                                val status = jsonObject.get("status")?.asString
                                
                                if (status == "success") {
                                    val action = jsonObject.get("action")?.asString
                                    val likeCount = jsonObject.get("like_count")?.asInt ?: post.like_count
                                    
                                    // Update UI
                                    binding.postLikes.text = "$likeCount likes"
                                    
                                    if (action == "liked") {
                                        binding.postLikeButton.setImageResource(R.drawable.heart_filled)
                                        binding.postLikeButton.tag = "liked"
                                    } else {
                                        binding.postLikeButton.setImageResource(R.drawable.ic_heart)
                                        binding.postLikeButton.tag = "like"
                                    }
                                    
                                    // Note: Posts will be refreshed when HomeActivity calls readPosts() again
                                    // For now, we just update the UI locally
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    override fun onError(error: String) {
                        // Handle error silently
                    }
                })
            }

            binding.postCommentButton.setOnClickListener {
                val intent = Intent(context, CommentsActivity::class.java)
                intent.putExtra("postId", post.id)
                intent.putExtra("publisherId", post.user_id)
                context.startActivity(intent)
            }

            binding.postShareButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_TEXT, post.image_url)
                context.startActivity(Intent.createChooser(intent, "Share Post"))
            }
        }
    }

}
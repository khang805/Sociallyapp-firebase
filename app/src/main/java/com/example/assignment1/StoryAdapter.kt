package com.example.assignment1

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView

class StoryAdapter(
    private val context: Context,
    private val userStories: List<userStory>,
    private val onStoryClick: (userStory) -> Unit
) : RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.story_item, parent, false)
        return StoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        holder.bind(userStories[position])
    }

    override fun getItemCount(): Int = userStories.size

    inner class StoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val storyImage: CircleImageView = itemView.findViewById(R.id.storyImage)
        private val usernameText: TextView = itemView.findViewById(R.id.storyName)

        fun bind(userStory: userStory) {
            // Set username with story count if multiple stories
            usernameText.text = if (userStory.stories.size > 1) {
                "${userStory.username} (${userStory.stories.size})"
            } else {
                userStory.username
            }

            // ALWAYS use the story image (actual story media), NOT profile photo
            // Use the latest story image URL (already calculated in HomeActivity)
            val storyImageUrl = userStory.latestImageUrl.ifEmpty { 
                userStory.stories.firstOrNull()?.imageUrl ?: "" 
            }

            if (storyImageUrl.isNotEmpty()) {
                try {
                    // Use Glide to load the actual story image from URL
                    com.bumptech.glide.Glide.with(context)
                        .load(storyImageUrl)
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .circleCrop()
                        .into(storyImage)
                } catch (e: Exception) {
                    Log.e("StoryAdapter", "Error loading story image: ${e.message}", e)
                    storyImage.setImageResource(R.drawable.ic_profile)
                }
            } else {
                // Fallback to profile photo only if no story image exists (shouldn't happen)
                val fallbackUrl = userStory.profilePhotoUrl ?: ""
                if (fallbackUrl.isNotEmpty()) {
                    try {
                        com.bumptech.glide.Glide.with(context)
                            .load(fallbackUrl)
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .circleCrop()
                            .into(storyImage)
                    } catch (e: Exception) {
                        storyImage.setImageResource(R.drawable.ic_profile)
                    }
                } else {
                    storyImage.setImageResource(R.drawable.ic_profile)
                }
            }

            itemView.setOnClickListener {
                Log.d("StoryAdapter", "Clicked ${userStory.username}'s story (${userStory.stories.size} total)")
                onStoryClick(userStory)
            }
        }
    }
}
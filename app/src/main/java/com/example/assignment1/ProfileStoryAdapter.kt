package com.example.assignment1

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView

class ProfileStoryAdapter(
    private val context: Context,
    private val stories: List<Story>,
    private val onStoryClick: (Story) -> Unit
) : RecyclerView.Adapter<ProfileStoryAdapter.StoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.story_item, parent, false)
        return StoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        holder.bind(stories[position])
    }

    override fun getItemCount(): Int = stories.size

    inner class StoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val storyImage: CircleImageView = itemView.findViewById(R.id.storyImage)
        private val usernameText: android.widget.TextView = itemView.findViewById(R.id.storyName)

        fun bind(story: Story) {
            // Hide username text for profile stories (or show story number)
            usernameText.text = ""
            usernameText.visibility = View.GONE

            // Load story image
            if (story.imageUrl.isNotEmpty()) {
                try {
                    com.bumptech.glide.Glide.with(context)
                        .load(story.imageUrl)
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .circleCrop()
                        .into(storyImage)
                } catch (e: Exception) {
                    Log.e("ProfileStoryAdapter", "Error loading image: ${e.message}", e)
                    storyImage.setImageResource(R.drawable.ic_profile)
                }
            } else {
                storyImage.setImageResource(R.drawable.ic_profile)
            }

            itemView.setOnClickListener {
                Log.d("ProfileStoryAdapter", "Clicked story ${story.id}")
                onStoryClick(story)
            }
        }
    }
}


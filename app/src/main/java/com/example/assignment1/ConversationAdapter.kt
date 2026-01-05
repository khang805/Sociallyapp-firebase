package com.example.assignment1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*

class ConversationAdapter(
    private val conversations: MutableList<Conversation>,
    private val onConversationClick: (Conversation) -> Unit
) : RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_conversation, parent, false)
        return ConversationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        holder.bind(conversations[position])
    }

    override fun getItemCount(): Int = conversations.size

    fun updateConversations(newConversations: List<Conversation>) {
        conversations.clear()
        conversations.addAll(newConversations)
        notifyDataSetChanged()
    }

    inner class ConversationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImage: CircleImageView = itemView.findViewById(R.id.profileImage)
        private val nameText: TextView = itemView.findViewById(R.id.nameText)
        private val lastMessageText: TextView = itemView.findViewById(R.id.lastMessageText)
        private val timeText: TextView = itemView.findViewById(R.id.timeText)
        private val unreadBadge: TextView = itemView.findViewById(R.id.unreadBadge)

        fun bind(conversation: Conversation) {
            nameText.text = conversation.other_user_display_name
            lastMessageText.text = conversation.last_message_preview
            timeText.text = formatTime(conversation.last_message_time)

            if (conversation.other_profile_photo_url != null) {
                Glide.with(itemView.context)
                    .load(conversation.other_profile_photo_url)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .circleCrop()
                    .into(profileImage)
            } else {
                profileImage.setImageResource(R.drawable.ic_profile)
            }

            if (conversation.unread_count > 0) {
                unreadBadge.text = conversation.unread_count.toString()
                unreadBadge.visibility = View.VISIBLE
            } else {
                unreadBadge.visibility = View.GONE
            }

            itemView.setOnClickListener {
                onConversationClick(conversation)
            }
        }

        private fun formatTime(timeString: String): String {
            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val date = sdf.parse(timeString)
                val now = Date()
                val diff = now.time - (date?.time ?: 0)

                when {
                    diff < 60000 -> "Just now"
                    diff < 3600000 -> "${diff / 60000}m ago"
                    diff < 86400000 -> "${diff / 3600000}h ago"
                    else -> {
                        val timeFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                        timeFormat.format(date)
                    }
                }
            } catch (e: Exception) {
                timeString
            }
        }
    }
}


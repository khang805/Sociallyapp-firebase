package com.example.assignment1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ChatMessageAdapter(
    private val messages: MutableList<ChatMessage>,
    private val currentUserId: Int,
    private val onMessageClick: (ChatMessage) -> Unit
) : RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Sent views
        val sentLayout: LinearLayout = view.findViewById(R.id.sentMessageLayout)
        val sentText: TextView = view.findViewById(R.id.sentMessageText)
        val sentImage: ImageView = view.findViewById(R.id.sentMessageImage)
        val sentTime: TextView = view.findViewById(R.id.sentMessageTime)

        // Received views
        val receivedLayout: LinearLayout = view.findViewById(R.id.receivedMessageLayout)
        val receivedText: TextView = view.findViewById(R.id.receivedMessageText)
        val receivedImage: ImageView = view.findViewById(R.id.receivedMessageImage)
        val receivedTime: TextView = view.findViewById(R.id.receivedMessageTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun getItemCount() = messages.size

    fun updateMessages(newMessages: List<ChatMessage>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        val isSent = message.sender_id == currentUserId

        if (isSent) {
            holder.sentLayout.visibility = View.VISIBLE
            holder.receivedLayout.visibility = View.GONE

            holder.sentTime.text = message.created_at.substring(11, 16) // HH:mm format

            // LOGIC TO SHOW CONTENT
            when (message.media_type) {
                "text" -> {
                    holder.sentText.visibility = View.VISIBLE
                    holder.sentImage.visibility = View.GONE
                    holder.sentText.text = message.message_text ?: ""
                }
                "image" -> {
                    holder.sentText.visibility = View.GONE
                    holder.sentImage.visibility = View.VISIBLE
                    message.media_url?.let { url ->
                        Glide.with(holder.itemView.context).load(url).into(holder.sentImage)
                    }
                }
                else -> {
                    // Handle other media types like video/file with text fallback
                    holder.sentText.visibility = View.VISIBLE
                    holder.sentImage.visibility = View.GONE
                    holder.sentText.text = if (message.message_text.isNullOrEmpty())
                        "Sent ${message.media_type.capitalize()} (View Media)"
                    else
                        message.message_text
                }
            }

            holder.sentLayout.setOnClickListener { onMessageClick(message) }

        } else {
            holder.sentLayout.visibility = View.GONE
            holder.receivedLayout.visibility = View.VISIBLE

            holder.receivedTime.text = message.created_at.substring(11, 16) // HH:mm format

            // LOGIC TO SHOW CONTENT
            when (message.media_type) {
                "text" -> {
                    holder.receivedText.visibility = View.VISIBLE
                    holder.receivedImage.visibility = View.GONE
                    holder.receivedText.text = message.message_text ?: ""
                }
                "image" -> {
                    holder.receivedText.visibility = View.GONE
                    holder.receivedImage.visibility = View.VISIBLE
                    message.media_url?.let { url ->
                        Glide.with(holder.itemView.context).load(url).into(holder.receivedImage)
                    }
                }
                else -> {
                    // Handle other media types like video/file with text fallback
                    holder.receivedText.visibility = View.VISIBLE
                    holder.receivedImage.visibility = View.GONE
                    holder.receivedText.text = if (message.message_text.isNullOrEmpty())
                        "Received ${message.media_type.capitalize()} (View Media)"
                    else
                        message.message_text
                }
            }

            holder.receivedLayout.setOnClickListener { onMessageClick(message) }
        }
    }
}
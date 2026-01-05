package com.example.assignment1

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ChatAdapter(
    private val context: Context,
    private val chatList: List<Message>,
    private val currentUserId: String,
    private val onMessageLongClick: (Message) -> Unit
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.dm, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = chatList[position]

        holder.sentLayout.visibility = View.GONE
        holder.receivedLayout.visibility = View.GONE

        if (message.senderId == currentUserId) {
            holder.sentLayout.visibility = View.VISIBLE

            if (!message.imageUrl.isNullOrEmpty()) {
                holder.sentText.visibility = View.GONE
                holder.sentImage.visibility = View.VISIBLE
                Glide.with(context).load(message.imageUrl).into(holder.sentImage)
            } else {
                holder.sentImage.visibility = View.GONE
                holder.sentText.visibility = View.VISIBLE
                holder.sentText.text = message.message
            }

        } else {
            holder.receivedLayout.visibility = View.VISIBLE

            if (!message.imageUrl.isNullOrEmpty()) {
                holder.receivedText.visibility = View.GONE
                holder.receivedImage.visibility = View.VISIBLE
                Glide.with(context).load(message.imageUrl).into(holder.receivedImage)
            } else {
                holder.receivedImage.visibility = View.GONE
                holder.receivedText.visibility = View.VISIBLE
                holder.receivedText.text = message.message
            }
        }

        holder.itemView.setOnLongClickListener {
            onMessageLongClick(message)
            true
        }
    }

    override fun getItemCount(): Int = chatList.size

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sentLayout: LinearLayout = itemView.findViewById(R.id.sent_message_layout)
        val receivedLayout: LinearLayout = itemView.findViewById(R.id.received_message_layout)

        val sentText: TextView = itemView.findViewById(R.id.text_message)
        val sentImage: ImageView = itemView.findViewById(R.id.image_message)

        val receivedText: TextView = itemView.findViewById(R.id.text_message_received)
        val receivedImage: ImageView = itemView.findViewById(R.id.image_message_received)
    }
}

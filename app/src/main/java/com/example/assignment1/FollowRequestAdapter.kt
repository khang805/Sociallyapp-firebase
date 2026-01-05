package com.example.assignment1

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.JsonParser

class FollowRequestAdapter(
    private val requests: MutableList<FollowRequest>,
    private val currentUserId: Int
) : RecyclerView.Adapter<FollowRequestAdapter.RequestViewHolder>() {

    inner class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.requestName)
        val acceptBtn: Button = itemView.findViewById(R.id.acceptBtn)
        val rejectBtn: Button = itemView.findViewById(R.id.rejectBtn)
        val profileImage: ImageView = itemView.findViewById(R.id.requestImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.follow_request_item, parent, false)
        return RequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val req = requests[position]

        holder.name.text = req.displayName.ifEmpty { req.username }

        // Load profile image using Glide from URL
        if (!req.profile_photo_url.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(req.profile_photo_url)
                .placeholder(R.drawable.ic_profile_blue)
                .error(R.drawable.ic_profile_blue)
                .circleCrop()
                .into(holder.profileImage)
        } else {
            holder.profileImage.setImageResource(R.drawable.ic_profile_blue)
        }

        holder.acceptBtn.setOnClickListener {
            acceptRequest(holder, req, position)
        }

        holder.rejectBtn.setOnClickListener {
            rejectRequest(holder, req, position)
        }
    }

    override fun getItemCount() = requests.size

    private fun acceptRequest(holder: RequestViewHolder, req: FollowRequest, position: Int) {
        ApiService.acceptFollowRequest(currentUserId, req.sender_id, object : ApiService.ApiCallback {
            override fun onSuccess(response: String) {
                (holder.itemView.context as? AppCompatActivity)?.runOnUiThread {
                    try {
                        val jsonObject = JsonParser.parseString(response).asJsonObject
                        if (jsonObject.get("status")?.asString == "success") {
        requests.removeAt(position)
        notifyItemRemoved(position)
                            Toast.makeText(holder.itemView.context, "Follow request accepted", Toast.LENGTH_SHORT).show()
                        } else {
                            val message = jsonObject.get("message")?.asString ?: "Failed to accept request"
                            Toast.makeText(holder.itemView.context, message, Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(holder.itemView.context, "Error accepting request", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onError(error: String) {
                (holder.itemView.context as? AppCompatActivity)?.runOnUiThread {
                    Toast.makeText(holder.itemView.context, error, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun rejectRequest(holder: RequestViewHolder, req: FollowRequest, position: Int) {
        ApiService.rejectFollowRequest(currentUserId, req.sender_id, object : ApiService.ApiCallback {
            override fun onSuccess(response: String) {
                (holder.itemView.context as? AppCompatActivity)?.runOnUiThread {
                    try {
                        val jsonObject = JsonParser.parseString(response).asJsonObject
                        if (jsonObject.get("status")?.asString == "success") {
        requests.removeAt(position)
        notifyItemRemoved(position)
                            notifyItemRangeChanged(position, requests.size)
                            Toast.makeText(holder.itemView.context, "Follow request rejected", Toast.LENGTH_SHORT).show()
                        } else {
                            val message = jsonObject.get("message")?.asString ?: "Failed to reject request"
                            Toast.makeText(holder.itemView.context, message, Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(holder.itemView.context, "Error rejecting request", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onError(error: String) {
                (holder.itemView.context as? AppCompatActivity)?.runOnUiThread {
                    Toast.makeText(holder.itemView.context, error, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}

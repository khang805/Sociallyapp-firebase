package com.example.assignment1

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView

data class User(
    val uid: String = "",
    val username: String = "",
    val fullName: String? = "",
    val profileImage: String? = null,
    val status: String = "offline"
)

class UserAdapter(
    private val users: List<User>,
    private val onAcceptClick: (User) -> Unit,
    private val onRejectClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivProfile: CircleImageView = view.findViewById(R.id.requestImage)
        val tvUsername: TextView = view.findViewById(R.id.tvUsername)
        val tvDisplayName: TextView = view.findViewById(R.id.tvDisplayName)
        val btnAccept: Button = view.findViewById(R.id.acceptBtn)
        val btnReject: Button = view.findViewById(R.id.rejectBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.follow_request_item, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.tvUsername.text = user.username
        holder.tvDisplayName.text = user.fullName

        if (!user.profileImage.isNullOrEmpty() && user.profileImage != "null") {
            val bytes = Base64.decode(user.profileImage, Base64.DEFAULT)
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            holder.ivProfile.setImageBitmap(bmp)
        } else {
            holder.ivProfile.setImageResource(R.drawable.ic_profile)
        }

        holder.btnAccept.setOnClickListener { onAcceptClick(user) }
        holder.btnReject.setOnClickListener { onRejectClick(user) }
    }

    override fun getItemCount(): Int = users.size
}

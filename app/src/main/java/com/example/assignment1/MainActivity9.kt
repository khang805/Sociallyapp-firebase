package com.example.assignment1

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity9 : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MessageListAdapter
    private lateinit var searchBar: EditText

    private val followersList = mutableListOf<User>()
    private val filteredList = mutableListOf<User>()

    private val db = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main9)

        recyclerView = findViewById(R.id.followersRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MessageListAdapter(filteredList)
        recyclerView.adapter = adapter

        searchBar = findViewById(R.id.et_search)
        val currentUser = auth.currentUser ?: return

        // Load followers of current user
        db.child("users").child(currentUser.uid).child("username").get()
            .addOnSuccessListener { snap ->
                val username = snap.getValue(String::class.java) ?: return@addOnSuccessListener
                loadFollowers(username)
            }

        // Search filter
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase()
                filteredList.clear()
                filteredList.addAll(
                    followersList.filter {
                        (it.fullName?.lowercase()?.contains(query) == true) ||
                                (it.username?.lowercase()?.contains(query) == true)
                    }
                )

                adapter.notifyDataSetChanged()
            }
        })
    }

    private fun loadFollowers(username: String) {
        db.child("followers").child(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    followersList.clear()

                    val followerUsernames = snapshot.children.mapNotNull { it.key }
                    if (followerUsernames.isEmpty()) {
                        filteredList.clear()
                        adapter.notifyDataSetChanged()
                        return
                    }

                    val limitedFollowers = followerUsernames.take(7)
                    var fetchedCount = 0

                    for (followerUsername in limitedFollowers) {
                        db.child("users")
                            .orderByChild("username")
                            .equalTo(followerUsername)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(userSnap: DataSnapshot) {
                                    for (user in userSnap.children) {
                                        val uid = user.key ?: ""
                                        val fullName = user.child("Full name").getValue(String::class.java) ?: ""
                                        val username = user.child("username").getValue(String::class.java) ?: ""
                                        val profileImage = user.child("profileImage").getValue(String::class.java) ?: ""

                                        val follower = User(uid, fullName, username, profileImage)
                                        followersList.add(follower)
                                    }

                                    fetchedCount++
                                    if (fetchedCount == limitedFollowers.size) {
                                        filteredList.clear()
                                        filteredList.addAll(followersList)
                                        adapter.notifyDataSetChanged()
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {}
                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}

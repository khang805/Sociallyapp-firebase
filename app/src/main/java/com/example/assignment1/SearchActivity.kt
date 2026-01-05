package com.example.assignment1

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonParser

class SearchActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var searchEditText: EditText
    private lateinit var searchResultsRecyclerView: RecyclerView
    private lateinit var userSearchAdapter: UserSearchAdapter
    private val userList = mutableListOf<SearchUser>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        sessionManager = SessionManager(this)

        searchEditText = findViewById(R.id.searchEditText)
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView)

        setupRecyclerView()

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                if (query.isNotEmpty() && query.length >= 2) {
                    searchUsers(query)
                } else {
                    userList.clear()
                    userSearchAdapter.updateUsers(userList)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupRecyclerView() {
        userSearchAdapter = UserSearchAdapter(this, userList)
        searchResultsRecyclerView.layoutManager = LinearLayoutManager(this)
        searchResultsRecyclerView.adapter = userSearchAdapter
    }

    private fun searchUsers(query: String) {
        val currentUserId = sessionManager.getUserId()
        if (currentUserId == -1) {
            // User not logged in, clear results
            userList.clear()
            userSearchAdapter.updateUsers(userList)
            return
        }
        
        ApiService.searchUsers(query, currentUserId, object : ApiService.ApiCallback {
            override fun onSuccess(response: String) {
                runOnUiThread {
                    try {
                        val jsonObject = JsonParser.parseString(response).asJsonObject
                        if (jsonObject.get("status")?.asString == "success") {
                            val usersArray = jsonObject.getAsJsonArray("users")
                            userList.clear()

                            usersArray?.forEach { element ->
                                val userJson = element.asJsonObject
                                val userId = userJson.get("id")?.asInt ?: 0
                                // Only add users with valid IDs
                                if (userId > 0) {
                                    val user = SearchUser(
                                        id = userId,
                                        username = userJson.get("username")?.asString ?: "",
                                        first_name = userJson.get("first_name")?.asString ?: "",
                                        last_name = userJson.get("last_name")?.asString ?: "",
                                        profile_photo_url = userJson.get("profile_photo_url")?.asString
                                    )
                                    userList.add(user)
                                }
                            }

                            userSearchAdapter.updateUsers(userList)
                        } else {
                            // If status is not success, clear the list
                            userList.clear()
                            userSearchAdapter.updateUsers(userList)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // Clear list on error
                        userList.clear()
                        userSearchAdapter.updateUsers(userList)
                    }
                }
            }

            override fun onError(error: String) {
                runOnUiThread {
                    // Clear list on error
                    userList.clear()
                    userSearchAdapter.updateUsers(userList)
                }
            }
        })
    }
}

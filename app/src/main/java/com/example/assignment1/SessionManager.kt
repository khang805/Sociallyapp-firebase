package com.example.assignment1

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = prefs.edit()

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"
        private const val KEY_FIRST_NAME = "first_name"
        private const val KEY_LAST_NAME = "last_name"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    fun saveUserSession(
        userId: Int,
        username: String,
        email: String,
        firstName: String? = null,
        lastName: String? = null
    ) {
        editor.putInt(KEY_USER_ID, userId)
        editor.putString(KEY_USERNAME, username)
        editor.putString(KEY_EMAIL, email)
        editor.putString(KEY_FIRST_NAME, firstName)
        editor.putString(KEY_LAST_NAME, lastName)
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.apply()
    }

    fun getUserId(): Int {
        return prefs.getInt(KEY_USER_ID, -1)
    }

    fun getUsername(): String? {
        return prefs.getString(KEY_USERNAME, null)
    }

    fun getEmail(): String? {
        return prefs.getString(KEY_EMAIL, null)
    }

    fun getFirstName(): String? {
        return prefs.getString(KEY_FIRST_NAME, null)
    }

    fun getLastName(): String? {
        return prefs.getString(KEY_LAST_NAME, null)
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) && getUserId() != -1
    }

    fun logout() {
        editor.clear()
        editor.apply()
    }
}


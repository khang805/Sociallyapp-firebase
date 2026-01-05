package com.example.assignment1

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.gson.JsonParser
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class EditProfileActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var btnCancel: TextView
    private lateinit var btnDone: TextView
    private lateinit var profilePic: ImageView
    private lateinit var changePhoto: TextView
    private lateinit var etName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etUsername: EditText
    private lateinit var etWebsite: EditText
    private lateinit var etBio: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var tvGender: TextView

    private var imageUri: Uri? = null
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            profilePic.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        sessionManager = SessionManager(this)

        btnCancel = findViewById(R.id.btnCancel)
        btnDone = findViewById(R.id.btnDone)
        profilePic = findViewById(R.id.profilePic)
        changePhoto = findViewById(R.id.changePhoto)
        etName = findViewById(R.id.etName)
        etUsername = findViewById(R.id.etUsername)
        etWebsite = findViewById(R.id.etWebsite)
        etBio = findViewById(R.id.etBio)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        tvGender = findViewById(R.id.tvGender)

        val userId = sessionManager.getUserId()
        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadUserInfo(userId)

        changePhoto.setOnClickListener { pickImage() }
        btnCancel.setOnClickListener { finish() }
        btnDone.setOnClickListener { saveChanges(userId) }
    }

    private fun loadUserInfo(userId: Int) {
        ApiService.getUserProfile(userId, object : ApiService.ApiCallback {
            override fun onSuccess(response: String) {
                runOnUiThread {
                    try {
                        val jsonObject = JsonParser.parseString(response).asJsonObject
                        if (jsonObject.get("status")?.asString == "success") {
                            val user = jsonObject.getAsJsonObject("user")
                            etName.setText(user.get("first_name")?.asString ?: "")
                            // Note: You may need to add etLastName to your layout
                            etUsername.setText(user.get("username")?.asString ?: "")
                            etWebsite.setText(user.get("website")?.asString ?: "")
                            etBio.setText(user.get("bio")?.asString ?: "")
                            etEmail.setText(user.get("email")?.asString ?: "")
                            etPhone.setText(user.get("phone")?.asString ?: "")
                            tvGender.text = user.get("gender")?.asString ?: ""

                            val profilePhotoUrl = user.get("profile_photo_url")?.asString
                            if (!profilePhotoUrl.isNullOrEmpty()) {
                                Glide.with(this@EditProfileActivity)
                                    .load(profilePhotoUrl)
                                    .placeholder(R.drawable.ic_profile_vector)
                                    .error(R.drawable.ic_profile_vector)
                                    .circleCrop()
                                    .into(profilePic)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onError(error: String) {
                runOnUiThread {
                    Toast.makeText(this@EditProfileActivity, "Failed to load profile: $error", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun pickImage() {
        pickImageLauncher.launch("image/*")
    }

    private fun saveChanges(userId: Int) {
        val firstName = etName.text.toString().trim()
        val username = etUsername.text.toString().trim()
        val website = etWebsite.text.toString().trim()
        val bio = etBio.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val gender = tvGender.text.toString().trim()

        // First upload profile photo if changed
        if (imageUri != null) {
            uploadProfilePhoto(userId) { success ->
                if (success) {
                    updateProfileInfo(userId, firstName, username, website, bio, phone, gender)
                } else {
                    // Still try to update other info even if photo upload fails
                    updateProfileInfo(userId, firstName, username, website, bio, phone, gender)
                }
            }
        } else {
            updateProfileInfo(userId, firstName, username, website, bio, phone, gender)
        }
    }

    private fun uploadProfilePhoto(userId: Int, callback: (Boolean) -> Unit) {
        val inputStream: InputStream? = contentResolver.openInputStream(imageUri!!)
        val file = File(cacheDir, "temp_profile_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)

        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }

        ApiService.uploadProfilePhoto(userId, file, object : ApiService.ApiCallback {
            override fun onSuccess(response: String) {
                file.delete()
                callback(true)
            }

            override fun onError(error: String) {
                file.delete()
                callback(false)
            }
        })
    }

    private fun updateProfileInfo(userId: Int, firstName: String, username: String, website: String, bio: String, phone: String, gender: String) {
        ApiService.updateProfile(
            userId = userId,
            firstName = firstName,
            username = username,
            website = website,
            bio = bio,
            phone = phone,
            gender = gender,
            callback = object : ApiService.ApiCallback {
                override fun onSuccess(response: String) {
                    runOnUiThread {
                        try {
                            val jsonObject = JsonParser.parseString(response).asJsonObject
                            if (jsonObject.get("status")?.asString == "success") {
                                Toast.makeText(this@EditProfileActivity, "Profile updated!", Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                val message = jsonObject.get("message")?.asString ?: "Failed to update profile"
                                Toast.makeText(this@EditProfileActivity, message, Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this@EditProfileActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onError(error: String) {
                    runOnUiThread {
                        Toast.makeText(this@EditProfileActivity, error, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }
}

package com.example.assignment1

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.assignment1.databinding.ActivityAddPostBinding
import com.google.gson.JsonParser
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class AddPostActivity : AppCompatActivity() {

    private val binding: ActivityAddPostBinding by lazy {
        ActivityAddPostBinding.inflate(layoutInflater)
    }
    private lateinit var sessionManager: SessionManager
    private var imageUri: Uri? = null

    // Activity Result API launcher for picking an image from gallery
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
            binding.imagePost.setImageURI(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        binding.closeButton.setOnClickListener {
            finish()
        }

        binding.postButton.setOnClickListener {
            uploadPost()
        }

        binding.imagePost.setOnClickListener {
            openGallery()
        }
    }

    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun uploadPost() {
        val caption = binding.descriptionPost.text.toString().trim()
        if (imageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = sessionManager.getUserId()
        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // Convert URI to File
            val inputStream: InputStream? = contentResolver.openInputStream(imageUri!!)
            val file = File(cacheDir, "temp_post_image_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            ApiService.uploadPost(userId, file, caption, object : ApiService.ApiCallback {
                override fun onSuccess(response: String) {
                    runOnUiThread {
                        try {
                            val jsonObject = JsonParser.parseString(response).asJsonObject
                            val status = jsonObject.get("status")?.asString

                            if (status == "success") {
                                Toast.makeText(this@AddPostActivity, "Post uploaded successfully", Toast.LENGTH_SHORT).show()
                                // Clean up temp file
                                file.delete()
                                finish()
                            } else {
                                val message = jsonObject.get("message")?.asString ?: "Failed to upload post"
                                Toast.makeText(this@AddPostActivity, message, Toast.LENGTH_SHORT).show()
                                file.delete()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(this@AddPostActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            file.delete()
                        }
                    }
                }

                override fun onError(error: String) {
                    runOnUiThread {
                        Toast.makeText(this@AddPostActivity, error, Toast.LENGTH_SHORT).show()
                        file.delete()
                    }
                }
            })

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to process image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

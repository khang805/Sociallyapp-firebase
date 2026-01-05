package com.example.assignment1

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.assignment1.databinding.ActivitySignupBinding
import com.google.gson.JsonParser
import java.text.SimpleDateFormat
import java.util.*

class SignUpActivity : AppCompatActivity() {

    private val binding by lazy { ActivitySignupBinding.inflate(layoutInflater) }
    private var imageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            binding.dp.setImageURI(it)
            imageUri = it
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnSignup.setOnClickListener {
            handleSignUp()
        }

        binding.arrowIcon.setOnClickListener {
            finish()
        }

        binding.dp.setOnClickListener {
            openGallery()
        }
    }

    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun handleSignUp() {
        val username = binding.etUserName.text.toString().trim()
        val name = binding.etFullName.text.toString().trim()
        val lname = binding.etLastName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (username.isEmpty() || name.isEmpty() || lname.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Note: Profile picture upload can be handled separately later
        // For now, we'll proceed with signup without requiring profile picture
        // Using default DOB if not provided (2000-01-01)
        val dob = "2000-01-01" // Default DOB, can be updated later

        ApiService.signup(username, name, lname, dob, email, password, object : ApiService.ApiCallback {
            override fun onSuccess(response: String) {
                runOnUiThread {
                    try {
                        val jsonObject = JsonParser.parseString(response).asJsonObject
                        val status = jsonObject.get("status")?.asString

                        if (status == "success") {
                            Toast.makeText(this@SignUpActivity, "Signup successful! Please login.", Toast.LENGTH_LONG).show()
                            val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        } else {
                            val message = jsonObject.get("message")?.asString ?: "Signup failed"
                            Toast.makeText(this@SignUpActivity, message, Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this@SignUpActivity, "Error parsing response: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onError(error: String) {
                runOnUiThread {
                    Toast.makeText(this@SignUpActivity, error, Toast.LENGTH_LONG).show()
                }
            }
        })
    }
}

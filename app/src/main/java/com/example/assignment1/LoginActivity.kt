package com.example.assignment1

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.assignment1.databinding.ActivityLoginBinding
import com.google.gson.JsonObject
import com.google.gson.JsonParser

class LoginActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        binding.btnLogin.setOnClickListener {
            handleLogin()
        }

        binding.signupLink.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    private fun handleLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
            return
        }

        ApiService.login(email, password, object : ApiService.ApiCallback {
            override fun onSuccess(response: String) {
                runOnUiThread {
                    try {
                        val jsonObject = JsonParser.parseString(response).asJsonObject
                        val status = jsonObject.get("status")?.asString

                        if (status == "success") {
                            val userId = jsonObject.get("user_id")?.asInt ?: -1
                            if (userId != -1) {
                                // Save session
                                sessionManager.saveUserSession(
                                    userId = userId,
                                    username = "", // Will be fetched later if needed
                                    email = email
                                )

                                Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this@LoginActivity, "Login failed: Invalid response", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            val message = jsonObject.get("message")?.asString ?: "Login failed"
                            Toast.makeText(this@LoginActivity, message, Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this@LoginActivity, "Error parsing response: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onError(error: String) {
                runOnUiThread {
                    Toast.makeText(this@LoginActivity, error, Toast.LENGTH_LONG).show()
                }
            }
        })
    }
}


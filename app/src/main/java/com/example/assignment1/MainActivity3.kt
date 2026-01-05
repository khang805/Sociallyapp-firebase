package com.example.assignment1

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity3 : AppCompatActivity() {

    private lateinit var loginButton: TextView
    private lateinit var signupButton: TextView
    private lateinit var switchAccountsButton: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main3)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loginButton = findViewById(R.id.login)
        signupButton = findViewById(R.id.noAccount)
        switchAccountsButton = findViewById(R.id.signup)

        loginButton.setOnClickListener {
            val intent = Intent(this@MainActivity3, HomeActivity::class.java)
            startActivity(intent)
        }

        signupButton.setOnClickListener {
            val intent = Intent(this@MainActivity3, SignUpActivity::class.java)
            startActivity(intent)
        }

        switchAccountsButton.setOnClickListener {
            val intent = Intent(this@MainActivity3, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}
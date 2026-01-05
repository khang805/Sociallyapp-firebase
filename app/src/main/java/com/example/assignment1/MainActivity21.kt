package com.example.assignment1

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity21 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main21)

        val followBtn = findViewById<TextView>(R.id.following)
        val backArrow = findViewById<ImageView>(R.id.iv_back_arrow)

        followBtn.setOnClickListener {
            startActivity(Intent(this, MainActivity22::class.java))
        }

        backArrow.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        val navHome = findViewById<ImageView>(R.id.nav_home)
        val navSearch = findViewById<ImageView>(R.id.nav_search)
        val navAdd = findViewById<ImageView>(R.id.nav_add)
        val navFavorite = findViewById<ImageView>(R.id.nav_favorite)
        val navProfile = findViewById<ImageView>(R.id.nav_profile)

        navHome.setOnClickListener {
            startActivity(Intent(this, SplashActivity::class.java))
        }

        navSearch.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        navAdd.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        navFavorite.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        navProfile.setOnClickListener {
            startActivity(Intent(this, MainActivity21::class.java))
        }
    }
}

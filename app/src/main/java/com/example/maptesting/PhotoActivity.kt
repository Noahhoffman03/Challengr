package com.example.maptesting

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class PhotoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.photo_viewer)

        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            val intent = Intent(this, ChallengeActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}
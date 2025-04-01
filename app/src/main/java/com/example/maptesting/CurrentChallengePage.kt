package com.example.maptesting

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


//There is definetly a better way of doing this page for the loading stuff
//but this is what I came up with and don't wanna bother optimizing it rn

class CurrentChallengePage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.currentchallenge_page)

        // get the challenge data from previous page
        val title = intent.getStringExtra("CHALLENGE_TITLE") ?: "No Title"
        val description = intent.getStringExtra("CHALLENGE_DESC") ?: "No Description"
       //????
        val photo = intent.getStringExtra("CHALLENGE_PHOTO") ?: "No Photo"

        // Set the title and description
        findViewById<TextView>(R.id.title_display).text = title
        findViewById<TextView>(R.id.desc_display).text = description


        // Photo stuff idk
        if (photo != "No Photo") {
            // WE NEED TO DO ------------
        }


        // Back button
        findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            finish()
        }
    }
}

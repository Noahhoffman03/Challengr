package com.example.maptesting

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.net.URI

class ChallengeActivity : AppCompatActivity() {
    val firestoreClient = FirestoreClient()
    lateinit var imageView: ImageView
    private lateinit var challenge: Challenge
    private lateinit var user: User


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_challenge)
        val submitButton = findViewById<Button>(R.id.submit_button)
        val title_text = findViewById<TextInputEditText>(R.id.title_input)
        val desc_text = findViewById<TextInputEditText>(R.id.textInputEditText)


        // Get latitude & longitude from intent thingy
        val latitude = intent.getDoubleExtra("LATITUDE", 0.0)
        val longitude = intent.getDoubleExtra("LONGITUDE", 0.0)

        //// ------- Extra stuff if we wanna display the coordinates
        // val locationTextView: TextView = findViewById(R.id.location_info)
        // locationTextView.text = "Challenge Location:\nLatitude: $latitude \nLongitude: $longitude"


        // Back button
        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }


        val photoView = findViewById<Button>(R.id.picture_view)
        photoView.setOnClickListener {
            val intent = Intent(this, PhotoActivity::class.java)
            startActivity(intent)
            finish()
        }

        imageView = findViewById(R.id.pic_display)
        val pick_photo = findViewById<Button>(R.id.btn_take_picture)
        var uriSave: Uri? = null
        val pickMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                // Callback is invoked after the user selects a media item or closes the
                // photo picker.
                if (uri != null) {
                    Log.d("PhotoPicker", "Selected URI: $uri")
                    imageView.setImageURI(uri)
                    uriSave = uri

                } else {
                    Log.d("PhotoPicker", "No media selected")
                }


            }
        pick_photo.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        submitButton.setOnClickListener {
            challenge = Challenge(
                title = title_text.text.toString(),
                desc = desc_text.text.toString(),
                photo = null,
                lat = latitude,
                lng = longitude
            )


            lifecycleScope.launch {
                firestoreClient.insertChallenge(challenge).collect { id ->
                    challenge = challenge.copy(id = id ?: "")
                }
                firestoreClient.updateChallenge(challenge).collect { result ->
                    println(result)
                }


            }

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
             // goes back to the previous page



            //Code that gets something from the database
            /*
            user = firestoreClient.getUser(user.username).collect{ result ->
                if (result!= null){
                    printLn("user got")
                    //id = user.id
                    //username = user.username
                    //etc
                }
                else{
                    println("no user")
                }
            }
            */
        }

    }
}

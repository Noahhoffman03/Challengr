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
import com.google.android.material.textfield.TextInputEditText
import java.net.URI

// Challenge data class to hold challenge information
data class Challenge(
    val title: String,
    val image: Uri?,
    val description: String,

    val latitude: Double,  //new stuff for coordinates
    val longitude: Double
)

// List to store challenges
var challenges = mutableListOf(Challenge("example", null, "example", 0.0, 0.0))

class ChallengeActivity : AppCompatActivity() {

    lateinit var imageView: ImageView

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

        // Open photo activity
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
            val newChal = Challenge(
                title_text.text.toString(),
                uriSave,
                desc_text.text.toString(),
                latitude,
                longitude
            )
            challenges.add(newChal)
        }
    }
    /*
fun setTitle(context: Context, title: String) {
    val prefs = context.getSharedPreferences("myAppPackage", 0)
    val editor = prefs.edit();
    editor.putString("Title", title);
    editor.apply();
}
fun getTitle(context: Context): String? {
    val prefs = context.getSharedPreferences("myAppPackage", 0)
    return prefs.getString("username", "")
}*/


}

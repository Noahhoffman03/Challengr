package com.example.maptesting

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.location.Location
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.net.URI
public final data class Challenge(
    val title: String,
    val image: Uri?,
    val description: String
)
var challenges = mutableListOf(Challenge("example", null, "example"))

class ChallengeActivity : AppCompatActivity() {
    val firestoreClient = FirestoreClient()
    lateinit var imageView: ImageView
    private var user = User(
        username = "Test",
        bio = "test Bio",
        mainLocation = "St. Peter, MN"
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_challenge)
        val submitButton = findViewById<Button>(R.id.submit_button)
        val title_text = findViewById<TextInputEditText>(R.id.title_input)
        val desc_text = findViewById<TextInputEditText>(R.id.textInputEditText)

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
        submitButton.setOnClickListener{
            lifecycleScope.launch{
                firestoreClient.insertUser(user).collect{ id ->
                    user = user.copy(id = id?: "")


                }
                firestoreClient.updateUser(user).collect{ result ->
                    println(result)
                }

                //get code
                /*
                firestoreClient.getUser(user.username).collect{ result ->
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

        //var newChal: Challenge
            //newChal = Challenge(title_text.text.toString(), uriSave, desc_text.text.toString())

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


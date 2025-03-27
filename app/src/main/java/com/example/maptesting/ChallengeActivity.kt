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
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import java.net.URI
import kotlin.math.abs

public final data class Challenge(
    val title: String,
    val image: Uri?,
    val description: String
)
var challenges = mutableListOf(Challenge("example", null, "example"))

class ChallengeActivity : AppCompatActivity(), GestureDetector.OnGestureListener {

    lateinit var imageView: ImageView
    lateinit var gestureDetector: GestureDetector
    companion object {
        const val MIN_DISTANCE = 50
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_challenge)
        this.gestureDetector = GestureDetector(this, this )
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
            var newChal: Challenge
            newChal = Challenge(title_text.text.toString(), uriSave, desc_text.text.toString())

        }




    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
        }
        return super.onTouchEvent(event)
    }
    override fun onDown(e: MotionEvent): Boolean {
        TODO("Not yet implemented")
        return false
    }

    override fun onShowPress(e: MotionEvent) {
        TODO("Not yet implemented")
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        TODO("Not yet implemented")
        return false
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        TODO("Not yet implemented")
    }
    override fun onLongPress(e: MotionEvent) {
        TODO("Not yet implemented")
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        if (e1 == null) return false
        val diffX = e2.x - e1.x
        val diffY = e2.y - e1.y

        if (abs(diffX) > abs(diffY) && abs(diffX) > MIN_DISTANCE && abs(velocityX) > 100) {
            if (diffX < 0) {
                Log.d("Gesture", "Left Swipe Detected, launching ChallengeActivity")
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Log.d("Gesture", "Right Swipe Detected")
            }
            return true
        }
        return false
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


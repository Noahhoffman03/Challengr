package com.example.maptesting

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.abs
import android.view.GestureDetector
import android.widget.ImageView
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.ImageRequest


//There is definetly a better way of doing this page for the loading stuff
//but this is what I came up with and don't wanna bother optimizing it rn

class CurrentChallengeActivity : AppCompatActivity(), GestureDetector.OnGestureListener {
    lateinit var gestureDetector: GestureDetector
    lateinit var bitmap: Bitmap
    var MIN_DISTANCE = 150
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_current_challenge)
        gestureDetector = GestureDetector(this, this)
        // get the challenge data from previous page
        val title = intent.getStringExtra("CHALLENGE_TITLE") ?: "No Title"
        val description = intent.getStringExtra("CHALLENGE_DESC") ?: "No Description"
        val photo = intent.getStringExtra("URL") ?: "https://firebasestorage.googleapis.com/v0/b/challengr-1be1f.firebasestorage.app/o/image%2F20250408_104015-userImage.png?alt=media&token=bf5965ab-0cc8-48ca-99f7-9a5a27f470d9"
       //????
        //val photo = intent.getStringExtra("CHALLENGE_PHOTO") ?: "No Photo"

        // Set the title and description
        findViewById<TextView>(R.id.title_display).text = title
        findViewById<TextView>(R.id.desc_display).text = description

        val loader = ImageLoader(this)
        val req = ImageRequest.Builder(this)
            .data(photo)
            .target { result ->
                bitmap = (result as BitmapDrawable).bitmap
            }
            .build()

        val disposable = loader.enqueue(req)
        findViewById<ImageView>(R.id.pic_display).setImageBitmap(bitmap)

        // Photo stuff idk
       // if (photo != "No Photo") {
            // WE NEED TO DO ------------
        //}


        // Back button
        findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            finish()
        }

    }
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return if (event != null) {
            gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
        } else {
            super.onTouchEvent(event)
        }
    }

    override fun onDown(e: MotionEvent): Boolean {
        return false
    }

    override fun onShowPress(e: MotionEvent) {
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return false
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        return false
    }
    override fun onLongPress(e: MotionEvent) {
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        if (e1 == null) return false
        val diffX = e2.x - e1.x
        val diffY = e2.y - e1.y
        if (abs(diffX) > abs(diffY)) {
            if (diffX < -MIN_DISTANCE) {
                Log.d("Gesture", "Swipe Left → ChallList")
                val intent = Intent(this, ChallengeListActivity::class.java)
                startActivity(intent)
                return true
            } else if (diffX > MIN_DISTANCE) {
                Log.d("Gesture", "Swipe Right → MainActivity")
                val intent = Intent(this, MapActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
                return true
            }
        }
        return false

    }


}

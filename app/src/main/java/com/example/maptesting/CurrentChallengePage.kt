package com.example.maptesting

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.abs
import android.view.GestureDetector



//There is definetly a better way of doing this page for the loading stuff
//but this is what I came up with and don't wanna bother optimizing it rn

class CurrentChallengePage : AppCompatActivity(), GestureDetector.OnGestureListener {
    lateinit var gestureDetector: GestureDetector
    var MIN_DISTANCE = 150
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.currentchallenge_page)
        gestureDetector = GestureDetector(this, this)
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
                val intent = Intent(this, ChallList::class.java)
                startActivity(intent)
                finish()
            } else {
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
                Log.d("Gesture", "Right Swipe Detected")
            }
            return true
        }
        return false

    }

}

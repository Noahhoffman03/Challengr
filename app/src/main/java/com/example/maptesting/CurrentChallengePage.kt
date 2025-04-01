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
                val intent = Intent(this, ChallList::class.java)
                startActivity(intent)
                return true
            } else if (diffX > MIN_DISTANCE) {
                Log.d("Gesture", "Swipe Right → MainActivity")
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
                return true
            }
        }
        return false

    }

}

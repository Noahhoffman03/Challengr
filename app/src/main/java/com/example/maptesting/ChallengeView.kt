package com.example.maptesting

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.abs
import android.view.GestureDetector

class ChallengeView : AppCompatActivity(), GestureDetector.OnGestureListener{
    lateinit var gestureDetector: GestureDetector
    var MIN_DISTANCE = 150
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.challenge_view)
        gestureDetector = GestureDetector(this, this)
        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            val intent = Intent(this, ChallList::class.java)
            startActivity(intent)
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

        if (abs(diffX) > abs(diffY) && abs(diffX) > MIN_DISTANCE && abs(velocityX) > 100) {
            if (diffX < 0) {
                Log.d("Gesture", "Left Swipe Detected, launching ChallengeActivity")
                val intent = Intent(this, ChallList::class.java)
                startActivity(intent)
            } else {
                Log.d("Gesture", "Right Swipe Detected Do Nothing")
            }
            return true
        }
        return false

    }
}
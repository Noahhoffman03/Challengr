package com.example.maptesting

import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
private const val TAG = "BANANA"
class GestureHanding : AppCompatActivity() {
    override fun onTouchEvent(event: MotionEvent): Boolean{

        return when (event.action){
            MotionEvent.ACTION_DOWN -> {
                Log.d(TAG, "You are moving up")
                true
            }
            else -> super.onTouchEvent(event)
        }
    }
}
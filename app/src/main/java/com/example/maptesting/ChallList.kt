package com.example.maptesting

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.GoogleMap
import android.view.GestureDetector
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.abs

class ChallList : AppCompatActivity(), GestureDetector.OnGestureListener {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val firestore = FirebaseFirestore.getInstance() // Firestore thingy
    private lateinit var gestureDetector: GestureDetector
    private val MIN_DISTANCE = 150
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.challlist)
        gestureDetector = GestureDetector(this, this)
        val toChallView = findViewById<ImageButton>(R.id.back_button)
        toChallView.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        val backButton = findViewById<ImageButton>(R.id.imageButton)
        backButton.setOnClickListener {
            val intent = Intent(this, ChallengeView::class.java)
            startActivity(intent)
            finish()
        }

        /*val toChall = findViewById<ImageButton>(R.id.toChall)
        toChall.setOnClickListener {
            val intent = Intent(this, CurrentChallengePage::class.java)
            startActivity(intent)
            finish()
        }*/

        // getting the recyclerview by its id
        val recyclerview: RecyclerView = findViewById(R.id.recyclerview)

        // this creates a vertical layout Manager
        recyclerview.layoutManager = LinearLayoutManager(this)

        // ArrayList of class ItemsViewModel
        val data = ArrayList<Item>()

        // This loop will create Views containing
        // the image with the title of the challenge
        var i = 0
        firestore.collection("Challenges") //for the collection
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val title = document.getString("title") ?: "No Title"
                    val desc = document.getString("desc") ?: "No Description"
                    data.add(Item(R.drawable.tiger, title, desc))
                    i += 1
                }

                // This will pass the ArrayList to our Adapter
                //val adapter = Adapter(data)

                // Setting the Adapter with the recyclerview
                val adapter = Adapter(this, data) { challenge ->
                    startCurrentChallengeActivity(challenge)
                }
                recyclerview.adapter = adapter
            }

    }
    private fun startCurrentChallengeActivity(challenge: Item) {
        val intent = Intent(this, CurrentChallengePage::class.java)
        intent.putExtra("CHALLENGE_TITLE", challenge.text)
        intent.putExtra("CHALLENGE_DESC", challenge.desc)
        startActivity(intent)
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
                Log.d("Gesture", "Swipe Left → CurrentChallengePage")
                val intent = Intent(this,  CurrentChallengePage::class.java)
                startActivity(intent)
                return true
            } else if (diffX > MIN_DISTANCE) {
                Log.d("Gesture", "Swipe Right → MainActivity")
                val intent = Intent(this,  MainActivity::class.java)
                startActivity(intent)
                return true
            }
        }
        return false

    }
}
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
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlin.math.abs


class ChallengeListActivity : AppCompatActivity(), GestureDetector.OnGestureListener {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val firestore = FirebaseFirestore.getInstance() // Firestore thingy
    private lateinit var gestureDetector: GestureDetector
    private val MIN_DISTANCE = 200
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_challenge_list)
        gestureDetector = GestureDetector(this, this)



        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
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
        recyclerview.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
        }


        // ArrayList of class ItemsViewModel
        val data = ArrayList<Item>()

        // This loop will create Views containing
        // the image with the title of the challenge

        //I updated this to work with the new ChallengeRepository file to load all files
        lifecycleScope.launch {
            try {
                val challenges = ChallengeRepository.getAllChallenges()
                val data = challenges.map {
                    Item(it.photo, it.title, it.desc, it.creatorId, it.id)
                }

                val adapter = Adapter(this@ChallengeListActivity, ArrayList(data)) { challenge ->
                    startCurrentChallengeActivity(challenge)
                }
                recyclerview.adapter = adapter

            } catch (e: Exception) {
                Log.e("ChallengeListActivity", "Error loading challenges", e)
            }
        }
    }

    private fun startCurrentChallengeActivity(challenge: Item) {
        val intent = Intent(this, CurrentChallengeActivity::class.java)
        intent.putExtra("CHALLENGE_TITLE", challenge.text)
        intent.putExtra("CHALLENGE_DESC", challenge.desc)
        intent.putExtra("CREATOR_ID", challenge.creatorId)
        intent.putExtra("CHALLENGE_ID", challenge.id)
        intent.putExtra("CHALLENGE_PHOTO", challenge.photo)
        startActivity(intent)
    }



    //Swipe tech stuff _________________________________________
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
                val intent = Intent(this,  CurrentChallengeActivity::class.java)
                startActivity(intent)
                return true
            } else if (diffX > MIN_DISTANCE) {
                Log.d("Gesture", "Swipe Right → MainActivity")
                val intent = Intent(this,  MapActivity::class.java)
                startActivity(intent)
                return true
            }
        }
        return false

    }
}
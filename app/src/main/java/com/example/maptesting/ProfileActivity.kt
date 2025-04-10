package com.example.maptesting

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.abs
import android.view.GestureDetector

class ProfileActivity : AppCompatActivity(), GestureDetector.OnGestureListener {

    private lateinit var usernameText: TextView
    private lateinit var locationText: TextView
    private lateinit var bioText: TextView
    private lateinit var challengesText: TextView

    private lateinit var userIdText: TextView
    lateinit var gestureDetector: GestureDetector
    var MIN_DISTANCE = 150
    private lateinit var profileTitle: TextView
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        gestureDetector = GestureDetector(this, this)
        //usernameText = findViewById(R.id.username_text)
        supportActionBar?.title = "Profile"
        profileTitle = findViewById(R.id.profile_title)
        usernameText = findViewById(R.id.profile_title)
        locationText = findViewById(R.id.location_text)
        bioText = findViewById(R.id.bio_text)
        challengesText = findViewById(R.id.challenges_text)

        loadUserData()

        // log out feature
        val logoutButton: Button = findViewById(R.id.logout_button)
        logoutButton.setOnClickListener {
            logoutUser()
        }

        val map_button: Button = findViewById(R.id.map_button)
        map_button.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid
        db.collection("Users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val username = document.getString("username") ?: "N/A"
                    val bio = document.getString("bio") ?: "No bio available"
                    val mainLocation = document.getString("mainLocation") ?: "Unknown"
                    val completedChallenges = document.get("completedChallenge") as? List<*> ?: emptyList<Any>()

                    usernameText.text = username
                    supportActionBar?.title = username
                    profileTitle.text = username
                    locationText.text = mainLocation
                    bioText.text = bio
                    challengesText.text = completedChallenges.size.toString() // Display number of challenges
                } else {
                    Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show()
                Log.e("ProfileActivity", "Error fetching user data", e)
            }
    }

    //From online
    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut() // Logout the user
        // Redirect to LoginActivity
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
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
                Log.d("Gesture", "Swipe Left → MainActivity")
                val intent = Intent(this,  MapActivity::class.java)
                startActivity(intent)
                return true
            } else if (diffX > MIN_DISTANCE) {
                Log.d("Gesture", "Swipe Right → Null")
                // Swipe right goes nowhere
                return true
            }
        }
        return false

    }

}

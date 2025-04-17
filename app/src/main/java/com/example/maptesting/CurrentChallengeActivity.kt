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
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
//import coil3.ImageLoader
//import coil3.request.ImageRequest
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore


//There is definetly a better way of doing this page for the loading stuff
//but this is what I came up with and don't wanna bother optimizing it rn

class CurrentChallengeActivity : AppCompatActivity(), GestureDetector.OnGestureListener {
    lateinit var gestureDetector: GestureDetector
    var MIN_DISTANCE = 150
    private lateinit var bitmap: Bitmap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_current_challenge)
        gestureDetector = GestureDetector(this, this)
        // get the challenge data from previous page
        val title = intent.getStringExtra("CHALLENGE_TITLE") ?: "No Title"
        val description = intent.getStringExtra("CHALLENGE_DESC") ?: "No Description"
        val challengeId = intent.getStringExtra("CHALLENGE_ID") //Need to pass for the completing the challenge
        val creatorId = intent.getStringExtra("CREATOR_ID") ?: "Unknown" //Incase we wanna display
        //pointless rn ---------------------------
        val photo = intent.getStringExtra("CHALLENGE_PHOTO") ?: "No Photo"

        // Set the title and description
        findViewById<TextView>(R.id.title_display).text = title
        findViewById<TextView>(R.id.desc_display).text = description
        findViewById<TextView>(R.id.creator).text = creatorId


        // Photo stuff idk -----------------------
//        if (photo != "No Photo") {
//            val loader = ImageLoader(this)
//            val req = ImageRequest.Builder(this)
//                .data(photo) // demo link
//                .target { result ->
//                    bitmap = (result as BitmapDrawable).bitmap
//                }
//                .build()
//
//            val disposable = loader.enqueue(req)
//
//        }
        val picDisplay = findViewById<ImageView>(R.id.pic_display)
        Glide.with(this)
            .load(photo)
            .into(picDisplay);


        // Back button
        findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            finish()
        }


        //Completion button (pretty much same as the new challenge submission code)
        val completeButton = findViewById<Button>(R.id.complete_button)
        completeButton.setOnClickListener {
            //load email for user
            val currentUser = FirebaseAuth.getInstance().currentUser
            val email = currentUser?.email

            //Loads the current users data
            val db = FirebaseFirestore.getInstance()
            db.collection("Users")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val userDoc = documents.documents[0]
                        val userDocId = userDoc.id //gets the users id

                        //Add the challenge id to the users account
                        db.collection("Users").document(userDocId)
                            .update("completedChallenge", FieldValue.arrayUnion(challengeId))
                            .addOnSuccessListener {
                                Toast.makeText(this, "Challenge Completed!", Toast.LENGTH_SHORT).show()
                            }
                        val intent = Intent(this, MapActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "User not found.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error fetching user info.", Toast.LENGTH_SHORT).show()
                }
        }

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

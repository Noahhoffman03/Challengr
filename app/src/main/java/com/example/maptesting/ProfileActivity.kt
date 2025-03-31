package com.example.maptesting

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var usernameText: TextView
    private lateinit var locationText: TextView
    private lateinit var bioText: TextView
    private lateinit var challengesText: TextView
    private lateinit var userIdText: TextView

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_page)

        usernameText = findViewById(R.id.username_text)
        locationText = findViewById(R.id.location_text)
        bioText = findViewById(R.id.bio_text)
        challengesText = findViewById(R.id.challenges_text)
        userIdText = findViewById(R.id.userid_text)

        loadUserData()

        //Testing log out feature
        val logoutButton: Button = findViewById(R.id.testing_button)
        logoutButton.setOnClickListener {
            logoutUser()
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
                    locationText.text = mainLocation
                    bioText.text = bio
                    challengesText.text = completedChallenges.size.toString() // Display number of challenges
                    userIdText.text = userId
                } else {
                    Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show()
                Log.e("ProfileActivity", "Error fetching user data", e)
            }
    }

    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut() // Logs out the user

        // Redirect to LoginActivity and clear backstack
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

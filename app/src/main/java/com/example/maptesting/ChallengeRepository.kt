package com.example.maptesting

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// This loads ALL challenges in one place
// instead of doing it in both the map and list pages
object ChallengeRepository {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getAllChallenges(): List<Challenge> {
        val result = mutableListOf<Challenge>()

        val documents = firestore.collection("Challenges").get().await()
        for (document in documents) {
            val id = document.id
            val creatorId = document.getString("creatorId") ?: "Unknown"
            val title = document.getString("title") ?: "No Title"
            val desc = document.getString("desc") ?: "No Description"
            val lat = document.getDouble("lat") ?: 0.0
            val lng = document.getDouble("lng") ?: 0.0
            val photoPath = document.getString("photo")?: ""

            result.add(Challenge(id, creatorId, title, desc, photoPath, lat, lng))
        }

        return result
    }
}

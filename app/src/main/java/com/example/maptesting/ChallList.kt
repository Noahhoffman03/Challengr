package com.example.maptesting

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.GoogleMap
import com.google.firebase.firestore.FirebaseFirestore

class ChallList : AppCompatActivity() {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val firestore = FirebaseFirestore.getInstance() // Firestore thingy

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.challlist)

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
                    data.add(Item(R.drawable.tiger, title))
                    i += 1
                }

                // This will pass the ArrayList to our Adapter
                //val adapter = Adapter(data)
                val itemList = listOf(
                    Item(R.drawable.tiger, "Challenge 1")
                )

                // Setting the Adapter with the recyclerview
                val adapter = Adapter(this, itemList)
                recyclerview.adapter = adapter
            }
    }
}
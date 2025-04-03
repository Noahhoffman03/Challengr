package com.example.maptesting

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import com.example.maptesting.MainActivity
import com.example.maptesting.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val firestore = FirebaseFirestore.getInstance() // Firestore thingy
    private val defaultLocation = LatLng(44.3238, -93.9758) // Default location (GAC)
    private var newchalPinMarker: Marker? = null // Tracks the new challenge pin (red)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Fragment for map
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)




        val createChallengeButton: ImageButton = findViewById(R.id.toChallengrz)
        createChallengeButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        //"
        // val createChallengeButton: Button = findViewById(R.id.create_challenge)
        //   createChallengeButton.setOnClickListener {
        //      val intent = Intent(this, ChallengeActivity::class.java)
        //            startActivity(intent)
        //  }

        val toChallList: ImageButton = findViewById(R.id.toChallList)
        toChallList.setOnClickListener {
            val intent = Intent(this, ChallList::class.java)
            startActivity(intent)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            getLastKnownLocation()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }

        // Load challenges
        loadExistingChallenges()

        // Handle user clicks to place a new challenge pin
        mMap.setOnMapClickListener { latLng ->
            placeSingleRedPin(latLng)
        }

        // Click listener for challenge pins
        mMap.setOnMarkerClickListener { marker ->
            when {
                marker == newchalPinMarker -> {
                    // Clicking the red pin starts ChallengeActivity
                    startChallengeActivity(marker.position)
                    true
                }
                marker.tag is Challenge -> {
                    // Log challenge data
                    val challenge = marker.tag as Challenge
                    Log.d("MarkerClick", "Challenge clicked: $challenge")
                    startCurrentChallengeActivity(challenge)
                    true
                }
                else -> false
            }
        }
    }



    // Loads challenges from database as blue pins
    //Probably an easier way of doing this (Iteration 3)?

    // This loads all the current challenges
    // TO UPDATE
    //  - load completed colors in different color

    private fun loadExistingChallenges() {

        firestore.collection("Challenges") //for the collection
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) { //Loop to get the attributes
                    val id = document.id
                    val creatorId = document.getString("creatorId") ?: "Unknown"
                    val title = document.getString("title") ?: "No Title"
                    val desc = document.getString("desc") ?: "No Description"
                    val lat = document.getDouble("lat") ?: 0.0
                    val lng = document.getDouble("lng") ?: 0.0
                    val photoPath = document.getString("photo")

                    // Convert Firestore photo path to a File (assuming local storage)
                    val photoFile = photoPath?.let { File(it) }

                    // Create Challenge object
                    val challenge = Challenge(id, creatorId, title, desc, photoFile?.toUri().toString(), lat, lng)


                    // Places challenge pin on the map
                    val location = LatLng(lat, lng)
                    val marker = mMap.addMarker(
                        MarkerOptions()
                            .position(location)
                            .title(title) // Not sure how to get this to perma show
                            .snippet(desc) // ^ This either
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    )
                    marker?.tag = challenge // Sets tag for when its clicked on
                }
            }
            .addOnFailureListener { //This was from online probably dont need
                Toast.makeText(this, "Failed to load challenges from Firestore.", Toast.LENGTH_SHORT).show()
            }

    }

    //Placing pin for new challenge
    //Only places one at a time and in red
    private fun placeSingleRedPin(latLng: LatLng) {
        newchalPinMarker?.remove()
        newchalPinMarker = mMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("New Challenge Location") // Doesn't show up ever, but bugs out on my computer if missing
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )
    }

    //opens the new challenge page
    private fun startChallengeActivity(location: LatLng) {
        val intent = Intent(this, ChallengeActivity::class.java)
        intent.putExtra("LATITUDE", location.latitude) //brings stuff for coords
        intent.putExtra("LONGITUDE", location.longitude)
        startActivity(intent)
    }

    //opens current challenge based on pin
    private fun startCurrentChallengeActivity(challenge: Challenge) {
        val intent = Intent(this, CurrentChallengePage::class.java)
        intent.putExtra("CHALLENGE_TITLE", challenge.title)
        intent.putExtra("CHALLENGE_DESC", challenge.desc)

        // NEED TO FIGURE OUT PICTURES ---------
        intent.putExtra("CHALLENGE_PHOTO", challenge.photo)//.path)
        startActivity(intent)
    }

    //Tracks the users location when they open the map
    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val userLatLng = LatLng(location.latitude, location.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 16f))
            } else {
                moveToDefaultLocation()
            }
        }.addOnFailureListener {
            moveToDefaultLocation()
        }
    }

    //Asks for user permision when first launched
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.isMyLocationEnabled = true
                getLastKnownLocation()
            }
        } else {
            moveToDefaultLocation()
        }
    }


    //Incase the user denies
    private fun moveToDefaultLocation() {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))
        mMap.addMarker(MarkerOptions().position(defaultLocation).title("Default Location"))
    }

}
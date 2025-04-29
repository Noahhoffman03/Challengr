package com.example.maptesting

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.io.File

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val firestore = FirebaseFirestore.getInstance() // Firestore thingy
    private val defaultLocation = LatLng(44.3238, -93.9758) // Default location (GAC)
    private var newchalPinMarker: Marker? = null // Tracks the new challenge pin (red)

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Fragment for map
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        val toChallengrz: Button = findViewById(R.id.toChallengrz2)
        toChallengrz.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        //  // Testing button
        // val createChallengeButton: Button = findViewById(R.id.create_challenge)
        //   createChallengeButton.setOnClickListener {
        //      val intent = Intent(this, ChallengeActivity::class.java)
        //            startActivity(intent)
        //  }

        val toChallList: Button = findViewById(R.id.toChallList)
        toChallList.setOnClickListener {
            val intent = Intent(this, ChallengeListActivity::class.java)
            startActivity(intent)
        }
        val createChallengeButton = findViewById<Button>(R.id.testing_button)
        createChallengeButton.setOnClickListener {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val userLatLng = LatLng(location.latitude, location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 16f))
                    startChallengeActivity(userLatLng)
                } else {
                    startChallengeActivity(defaultLocation)
                }
            }.addOnFailureListener {
                startChallengeActivity(defaultLocation)
            }
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
            placeSingleNewPin(latLng)
        }

        // Click listener for challenge pins
        mMap.setOnMarkerClickListener { marker ->
            when {
                marker == newchalPinMarker -> {
                    // Clicking the new pin starts ChallengeActivity
                    startChallengeActivity(marker.position)
                    true
                }
                marker.tag is Challenge -> {
                    // Log challenge data
                    val challenge = marker.tag as Challenge
                    startCurrentChallengeActivity(challenge)
                    true
                }
                else -> false
            }
        }
    }



    // Loads challenges from database as red pins


    // This loads all the current challenges
    private fun loadExistingChallenges() {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        //Get the current users completed challenge list
        FirebaseFirestore.getInstance().collection("Users").document(currentUser.uid).get()
            .addOnSuccessListener { document ->
                //get all completed challenges as a lsts
                val completedList = document.get("completedChallenge") as? List<String> ?: emptyList<String>()

                //Get all challenges from the repository
                lifecycleScope.launch {
                        val challengelist = ChallengeRepository.getAllChallenges()
                    //runs through all challenges to load them
                        for (challenge in challengelist) {
                            //their coordiantes for plotting
                            val location = LatLng(challenge.lat, challenge.lng)

                            // checks if the challenge id is in the users completed challenge
                            val isCompleted = completedList.contains(challenge.id)
                            val color = if (isCompleted) {
                                BitmapDescriptorFactory.HUE_GREEN
                            } else {
                                BitmapDescriptorFactory.HUE_ROSE
                            }

                            //place the actual marker
                            val marker = mMap.addMarker(
                                MarkerOptions()
                                    .position(location)
                                    .icon(BitmapDescriptorFactory.defaultMarker(color))
                            )
                            marker?.tag = challenge //This was previsouly in here and breaks if not in

                    }
                }
            }


    }



    //Placing pin for new challenge
    //Only places one at a time and in blue
    private fun placeSingleNewPin(latLng: LatLng) {
        newchalPinMarker?.remove()
        newchalPinMarker = mMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        )
    }

    //opens the new challenge page
    private fun startChallengeActivity(location: LatLng) {
        val intent = Intent(this, ChallengeCreateActivity::class.java)
        intent.putExtra("LATITUDE", location.latitude) //brings stuff for coords
        intent.putExtra("LONGITUDE", location.longitude)
        startActivity(intent)
    }

    //opens current challenge based on pin
    private fun startCurrentChallengeActivity(challenge: Challenge) {
        val intent = Intent(this, CurrentChallengeActivity::class.java)

        //Def a better way but this works
        //Bring all the important things for the current page from the pin
        intent.putExtra("CHALLENGE_TITLE", challenge.title)
        intent.putExtra("CHALLENGE_DESC", challenge.desc)
        intent.putExtra("CREATOR_ID", challenge.creatorId)
        intent.putExtra("CHALLENGE_ID", challenge.id)

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
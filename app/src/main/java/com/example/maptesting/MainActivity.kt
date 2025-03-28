package com.example.maptesting

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
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

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val firestore = FirebaseFirestore.getInstance() // Firestore instance
    private val defaultLocation = LatLng(44.3238, -93.9758) // Default location (GAC)
    private var redPinMarker: Marker? = null // Tracks the new challenge pin (red one)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Fragment for map
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            getLastKnownLocation()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }

        // Load challenges from Firestore
        loadExistingChallenges()

        // Handle user clicks to place a pin
        mMap.setOnMapClickListener { latLng ->
            placeSingleRedPin(latLng)
        }

        // Click the pin opens new challenge page
        mMap.setOnMarkerClickListener { marker ->
            val challenge = marker.tag as? Challenge // Assuming you've set the challenge as the marker's tag
            if (challenge != null) {
                val intent = Intent(this, CurrentChallengePage::class.java)
                intent.putExtra("CHALLENGE_TITLE", challenge.title)
                intent.putExtra("CHALLENGE_DESC", challenge.desc)
                intent.putExtra("CHALLENGE_PHOTO", challenge.photo?.path) // or URL if stored in Firebase storage
                startActivity(intent)
                true
            } else {
                false
            }
        }
    }


    //Loads challenges from database onto map in blue
    private fun loadExistingChallenges() {
        firestore.collection("Challenges") // Assuming the collection name is "Challenges"
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val id = document.id
                    val creatorId = document.getString("creatorId") ?: "Unknown"
                    val title = document.getString("title") ?: "No Title"
                    val desc = document.getString("desc") ?: "No Description"
                    val lat = document.getDouble("lat") ?: 0.0
                    val lng = document.getDouble("lng") ?: 0.0
                    val photoPath = document.getString("photo") // Stored as a path or URL in Firestore

                    // Convert Firestore photo path to a File (assuming local storage)
                    val photoFile = photoPath?.let { File(it) }

                    // Create Challenge object (optional)
                    val challenge = Challenge(id, creatorId, title, desc, photoFile, lat, lng)

                    // Place challenge pin on the map
                    val location = LatLng(lat, lng)
                    val marker = mMap.addMarker(
                        MarkerOptions()
                            .position(location)
                            .title(title)
                            .snippet(desc)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    )
                    marker?.tag = challenge // Set challenge data as the marker's tag
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load challenges from Firestore.", Toast.LENGTH_SHORT).show()
            }
    }

    // Function to place pin for new challenge (red pin)
    private fun placeSingleRedPin(latLng: LatLng) {
        redPinMarker?.remove()
        redPinMarker = mMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("New Challenge Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )
    }

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

    private fun moveToDefaultLocation() {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))
        mMap.addMarker(MarkerOptions().position(defaultLocation).title("Default Location"))
    }
}

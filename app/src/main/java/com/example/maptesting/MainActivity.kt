package com.example.maptesting

//Ignore this is just links for resources for all the parts
// Stuff about pins (extra at bottom) - https://developers.google.com/maps/documentation/android-sdk/marker
// measure lat and lonitude from dropped pin -  https://stackoverflow.com/questions/14208952/drag-and-drop-pin-on-google-map-manually-and-get-longitude-latitude-accordingl



import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
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
import com.example.maptesting.MainActivity
import com.example.maptesting.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val defaultLocation = LatLng(44.3238, -93.9758) // Default location (GAC)
    private var redPinMarker: Marker? = null // Tracks the new challenge pin (red one)


    // Hardcoded challenges for testing map loading
    // REMOVE LATER --------------------------------------------
    var challenges = mutableListOf(
        Challenge(
            title ="Challenge 1",
            photo = null,
            desc = "First test challenge",
            lat =44.32295,
            lng = -93.97234),
        Challenge(
            title = "Challenge 2",
            photo =  null,
            desc = "Second test challenge",
            lat = 44.32407,
            lng = -93.97552)
    )
    //---------------------------



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Fragment for map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)




        val createChallengeButton: Button = findViewById(R.id.testing_button)
        createChallengeButton.setOnClickListener {
           val intent = Intent(this, ProfileActivity::class.java)
               startActivity(intent)
     }
    }


    //Originally from stackOverflow tutorial
    //All the stuff that the map works with
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Checks if permission to access location is already given
        // If not, asks for location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            getLastKnownLocation()
        } else {
            // Ask for location permission
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }

        // Load existing challenges as blue pins
        loadExistingChallenges()

        // Handle user clicks to place a  pin
        mMap.setOnMapClickListener { latLng ->
            placeSingleRedPin(latLng)
        }

        // Click the pin opens new challenge page
        mMap.setOnMarkerClickListener { marker ->
            if (marker == redPinMarker) {
                startChallengeActivity(marker.position)
                true
            } else {
                false
            }
        }
    }



    // This loads all the current challenges
    // TO UPDATE
    //  - load completed colors in different color
    private fun loadExistingChallenges() {
        for (challenge in challenges) {
            val location = LatLng(challenge.lat, challenge.lng)
            mMap.addMarker(
                MarkerOptions() // This is the stuff that provides info on pin
                    .position(location)
                    .title(challenge.title)
                    .snippet(challenge.desc)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)) // Makes the pin blue
            )
        }
    }



    // Function to place pin for new challenge (in red)
    // Updated to only place one at a time
    private fun placeSingleRedPin(latLng: LatLng) {
        // Remove the current  pin if placed
        redPinMarker?.remove()

        // Add new red pin
        redPinMarker = mMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("New Challenge Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)) //Literally just make the pin red
        )
    }


    // Start the ChallengeActivity for a new challenge
    // with the red pin's location brought over for the coords
    private fun startChallengeActivity(location: LatLng) {
        val intent = Intent(this, ChallengeActivity::class.java)
        intent.putExtra("LATITUDE", location.latitude)
        intent.putExtra("LONGITUDE", location.longitude)
        startActivity(intent)
    }


    // Get last known location and move camera when the app is opened
    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val userLatLng = LatLng(location.latitude, location.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 16f))
            } else {
                moveToDefaultLocation()
            }
            //If location is denied go to default
        }.addOnFailureListener {
            moveToDefaultLocation()
        }
    }


    // Handles permission for location when first opening ap
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
                mMap.isMyLocationEnabled = true
                getLastKnownLocation()
            }
        } else {
            // If denied, move to default location (GAC)
            moveToDefaultLocation()
        }
    }


    // Move the camera to the default location
    private fun moveToDefaultLocation() {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))
        mMap.addMarker(MarkerOptions().position(defaultLocation).title("Default Location"))
    }

}

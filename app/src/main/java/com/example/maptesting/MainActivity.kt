package com.example.maptesting

//Ignore this is just links for resources for all the parts
// measure lat and lonitude from dropped pin -  https://stackoverflow.com/questions/14208952/drag-and-drop-pin-on-google-map-manually-and-get-longitude-latitude-accordingl

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.Intent
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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    //The default location (gac) if user denies location access for some reason
    private val defaultLocation = LatLng(44.3238, -93.9758)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //Fragment for map
        // (From the source page, not super sure of how this works)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val createChallengeButton: Button = findViewById(R.id.create_challenge)
        createChallengeButton.setOnClickListener {
            val intent = Intent(this, ChallengeActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Checks if permission to access location is already given
        // If not, asks for location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            getLastKnownLocation()
        } else {
            //This is the ask for location permission
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }

        // Add markers on clicking on the map
        mMap.setOnMapClickListener { latLng ->
            val marker = mMap.addMarker(MarkerOptions().position(latLng).title("Pinned Location"))

            mMap.setOnMarkerClickListener { clickedMarker ->
                if (clickedMarker == marker) {
                    // Pass latitude and longitude to ChallengeActivity
                    val intent = Intent(this, ChallengeActivity::class.java).apply {
                        putExtra("LATITUDE", latLng.latitude)
                        putExtra("LONGITUDE", latLng.longitude)
                    }
                    startActivity(intent)
                }
                true
            }
        }
    }

    // Get last known location and move camera when the app is open
    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
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

    // Handles permission result, not super sure of this stuff either
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
                mMap.isMyLocationEnabled = true
                getLastKnownLocation()
            }
        } else {
            // If denied moves to default location (gac)
            moveToDefaultLocation()
        }
    }

    // Move the camera to the default spot
    private fun moveToDefaultLocation() {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))
        mMap.addMarker(MarkerOptions().position(defaultLocation).title("Default Location"))
    }
}

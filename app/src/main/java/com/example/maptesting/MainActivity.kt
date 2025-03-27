package com.example.maptesting

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val firestoreClient = FirestoreClient()
    private val defaultLocation = LatLng(44.3238, -93.9758) // Default location (GAC)
    private var redPinMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

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

        loadExistingChallenges() // Fetch challenges from Firestore

        mMap.setOnMapClickListener { latLng -> placeSingleRedPin(latLng) }

        mMap.setOnMarkerClickListener { marker ->
            if (marker == redPinMarker) {
                startChallengeActivity(marker.position)
                true
            } else {
                false
            }
        }
    }

    // Fetch challenges from Firestore and display them
    private fun loadExistingChallenges() {
        lifecycleScope.launch {
            firestoreClient.getChallenges().collect { challenges ->
                for (challenge in challenges) {
                    val location = LatLng(challenge.latitude, challenge.longitude)
                    mMap.addMarker(
                        MarkerOptions()
                            .position(location)
                            .title(challenge.title)
                            .snippet(challenge.description)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    )
                }
            }
        }
    }

    private fun placeSingleRedPin(latLng: LatLng) {
        redPinMarker?.remove()
        redPinMarker = mMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("New Challenge Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )
    }

    private fun startChallengeActivity(location: LatLng) {
        val intent = Intent(this, ChallengeActivity::class.java)
        intent.putExtra("LATITUDE", location.latitude)
        intent.putExtra("LONGITUDE", location.longitude)
        startActivity(intent)
    }

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

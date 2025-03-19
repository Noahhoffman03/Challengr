package com.example.maptesting

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    //Creates the map, finds the starting location, and enables the pins to be placed
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Set initial location (Gustavus)
        // --- Should change to open on users location instead, Iteration 3 (?) ---
        val initialLocation = LatLng(44.323173, -93.973017)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 16f))

        // Add a pin on click (update for providing info/changing screens when clicked on
        mMap.setOnMapClickListener { latLng ->
            val marker = mMap.addMarker(MarkerOptions().position(latLng).title("Pinned Location"))
            mMap.setOnMarkerClickListener { clickedMarker ->
                if (clickedMarker == marker) {
                    val intent = Intent(this, ChallengeActivity::class.java)
                    startActivity(intent)
                }
                true

            }
        }
    }
}

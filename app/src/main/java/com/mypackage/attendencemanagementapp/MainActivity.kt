package com.mypackage.attendencemanagementapp

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.mypackage.attendencemanagementapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Set up the map fragment
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        // Check and request location permission if not granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            showCurrentLocation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun showCurrentLocation() {
        // Check if location permission is granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Get the last known location
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        // Add a marker at the current location
                        val currentLocation = LatLng(it.latitude, it.longitude)
                        Log.d(ContentValues.TAG, "Current Location: $currentLocation")
                        googleMap.clear()  // Clear previous markers
                        googleMap.addMarker(
                            MarkerOptions()
                                .position(currentLocation)
                                .title("You are here")
                        )
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, DEFAULT_ZOOM))
                    } ?: run {
                        Log.d(ContentValues.TAG, "No location detected")
                    }
                }
                .addOnFailureListener { e ->
                    Log.d(ContentValues.TAG, "Failed to get location: ${e.message}")
                }
        } else {
            Log.d(ContentValues.TAG, "Location permission not granted")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, show the current location
                showCurrentLocation()
            } else {
                // Permission denied
                Log.d(ContentValues.TAG, "Location permission denied")
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val DEFAULT_ZOOM = 15f
    }
}

package com.example.e_rikshawbookingautomation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.compose.runtime.saveable.autoSaver
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.e_rikshawbookingautomation.databinding.ActivityMapsBinding
import com.example.e_rikshawbookingautomation.sendnotification.FirebaseService
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private val LOCATION_PERMISSION_REQUEST=1
   // private lateinit var loc:Location

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        binding.doneButtonOnGoogleMap.setOnClickListener {
        }

        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

    }


    private fun getLocationAccess() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.isMyLocationEnabled = true // enable the data location layer.
            getLocationUpdates()
            startLocationUpdates()
        }
        else
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST)
            // this will open the dialog box to give option to the user to choose the options for location allowance.
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                getLocationAccess()
            }
            else {
                Toast.makeText(this, "User has not granted location access permission", Toast.LENGTH_LONG).show()
               finish()
            }
        }

    }
    private fun getLocationUpdates() {
        locationRequest = LocationRequest.create()
        locationRequest.interval = 30000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult.locations.isNotEmpty()) {
                    val location = locationResult.lastLocation

                    val firebaseAuth=FirebaseAuth.getInstance()
                    var email= firebaseAuth.currentUser?.email.toString()
                    email=email.replace('.',',')
                    val name=firebaseAuth.currentUser?.displayName.toString()

                    FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                        if(task.isSuccessful)
                        {
                            FirebaseService.token=task.result
                        }
                        else
                        {
                            Toast.makeText(this@MapsActivity,"Failed to Generate Token",Toast.LENGTH_SHORT).show()
                            return@OnCompleteListener
                        }
                    })


                    val database=FirebaseDatabase.getInstance()
                    val databaseRef=database.reference
                    val objOfLocationCordinates= FirebaseService.token?.let {
                        LocationCordinates(name,
                            it,location.latitude,location.longitude)
                    }
                    databaseRef.child("USERS").child(email).setValue(objOfLocationCordinates)
                    //databaseRef.child("UserLocation").setValue(objOfLocationCordinates)
                    if (location != null) {
                        val latLng = LatLng(location.latitude, location.longitude)
                        val markerOptions = MarkerOptions().position(latLng)
                        map.addMarker(markerOptions)
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                        binding.doneButtonOnGoogleMap.visibility=View.VISIBLE
                        binding.doneButtonOnGoogleMap.setOnClickListener {
                            val intent=Intent(this@MapsActivity,SendRequest::class.java)
                            intent.putExtra("Latitude",latLng.latitude)
                            intent.putExtra("Longitude",latLng.longitude)
                            startActivity(intent)
                        }

                    }
                }
            }
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
                Toast.makeText(this,"Location is not received",Toast.LENGTH_SHORT).show()
            return
        }
        else
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        getLocationAccess()
        getLocationUpdates()
        startLocationUpdates()
         val zoomLevel=15f // For street Level view we use 15
        // Add a marker in Sydney and move the camera
//        val sydney = LatLng(-34.0, 151.0)
//        map.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        map.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,zoomLevel))
    }



}
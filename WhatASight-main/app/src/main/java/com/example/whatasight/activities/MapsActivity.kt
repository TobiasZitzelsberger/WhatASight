package com.example.whatasight.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.whatasight.R
import com.example.whatasight.databinding.ActivityMainMenuBinding
import com.example.whatasight.databinding.ActivityMapsBinding
import com.example.whatasight.main.MainApp

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.whatasight.models.Location
import com.example.whatasight.models.POIModel
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.Marker
import com.squareup.picasso.Picasso
import timber.log.Timber

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, LocationListener {

    private lateinit var map: GoogleMap
    lateinit var app: MainApp
    private lateinit var binding: ActivityMapsBinding
    private lateinit var refreshIntentLauncher : ActivityResultLauncher<Intent>

    private var LOCATION_PERMISSION_REQUEST = 1
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var lastMarker: Long = -1

    //handle the provided user location
    private val callback= object: LocationCallback(){
        override fun onLocationResult(result: LocationResult) {
            val lastLocation= result?.lastLocation
            val loc = LatLng(lastLocation.latitude, lastLocation.longitude)
            activateLocationButton()
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15.0f))
            super.onLocationResult(result)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this)

        onGPS()
        registerRefreshCallback()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        app = application as MainApp
        //set markers for all POIs
        for(poi in app.pois.findAll()){
            val loc = LatLng(poi.lat, poi.lng)
            val options = MarkerOptions()
                .title(poi.title)
                .snippet("GPS : $loc")
                .draggable(false)
                .position(loc)
            map.addMarker(options)
            lastMarker = poi.id
        }
        map.setOnMarkerClickListener(this)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_add -> {
                val launcherIntent = Intent(this, POIActivity::class.java)
                refreshIntentLauncher.launch(launcherIntent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        return false
    }

    private fun activateLocationButton() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.isMyLocationEnabled = true
        }
        else
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST)
    }

    override fun onBackPressed() {
        val resultIntent = Intent()
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
        super.onBackPressed()
    }

    override fun onLocationChanged(location: android.location.Location) {
        TODO("Not yet implemented")
    }
    
    //check if permission on location is granted and start request on user location
    fun onGPS() {
        if (!isLocationEnabled()) {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        } else {
            fetchLocation()
        }
    }

    private fun fetchLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    200
                )
                return
            }else{
                requestLocation()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocation() {
        val requestLocation= LocationRequest()
        requestLocation.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        requestLocation.interval = 0
        requestLocation.fastestInterval = 0
        requestLocation.numUpdates = 1
        fusedLocationProviderClient.requestLocationUpdates(
            requestLocation,callback, Looper.myLooper()!!
        )

    }

    fun isLocationEnabled(): Boolean {
        val locationManager =
            applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun registerRefreshCallback() {
        refreshIntentLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            {   result ->
                when(result.resultCode){
                    RESULT_OK -> {
                        lastMarker++
                        var poi: POIModel? = app.pois.find(lastMarker)
                        if(poi != null){
                            val loc = LatLng(poi.lat, poi.lng)
                            val options = MarkerOptions()
                                .title(poi.title)
                                .snippet("GPS : $loc")
                                .draggable(false)
                                .position(loc)
                            map.addMarker(options)
                        }
                    }
                    RESULT_CANCELED -> { } else -> { }
                }
            }
    }

}

package com.example.whatasight.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.example.whatasight.R
import com.example.whatasight.databinding.ActivityPoiBinding
import com.example.whatasight.helpers.showImagePicker
import com.example.whatasight.main.MainApp
import com.example.whatasight.models.POIModel
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import timber.log.Timber

class POIActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPoiBinding
    lateinit var app: MainApp
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var imageIntentLauncher : ActivityResultLauncher<Intent>

    var poi = POIModel()
    var edit = false
    var callbackWorked = false

    private val callback= object: LocationCallback(){
        override fun onLocationResult(result: LocationResult) {
            val lastLocation= result?.lastLocation
            if(lastLocation != null) callbackWorked = true
            if((lastLocation.longitude == 0.0) and (lastLocation.latitude == 0.0)) callbackWorked = false
            val loc = LatLng(lastLocation.latitude, lastLocation.longitude)
            poi.lat = loc.latitude
            poi.lng = loc.longitude
            poi.zoom = 15.0f
            super.onLocationResult(result)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPoiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbarAdd.title = title
        setSupportActionBar(binding.toolbarAdd)

        app = application as MainApp

        if (intent.hasExtra("poi_edit")) {
            edit = true
            poi = intent.extras?.getParcelable("poi_edit")!!
            binding.poiTitle.setText(poi.title)
            binding.description.setText(poi.description)
            binding.rating.rating = poi.rating
            binding.btnAdd.setText(R.string.save_poi)
            Picasso.get()
                .load(poi.image)
                .into(binding.poiImage)
            if (poi.image != Uri.EMPTY) {
                binding.chooseImage.setText(R.string.change_poi_image)
            }
        }else{
            fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this)
            onGPS()
        }


        binding.btnAdd.setOnClickListener() {
            poi.title = binding.poiTitle.text.toString()
            poi.description = binding.description.text.toString()
            poi.rating = binding.rating.rating
            if (poi.title.isEmpty()) {
                Snackbar.make(it,R.string.enter_poi_title, Snackbar.LENGTH_LONG)
                    .show()
            } else {
                if (edit) {
                    app.pois.update(poi.copy())
                } else {
                    if(!callbackWorked){
                        Toast.makeText(this,"Unfortunately that didn't work... try again later", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_CANCELED)
                        finish()
                    }
                    app.pois.create(poi.copy())
                }
            }
            Timber.i("add Button Pressed: $poi")
            setResult(RESULT_OK)
            finish()
        }

        binding.chooseImage.setOnClickListener {
            showImagePicker(imageIntentLauncher)
        }
        registerImagePickerCallback()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        if(!edit){
            menuInflater.inflate(R.menu.poi_add_menu, menu)
        }else{
            menuInflater.inflate(R.menu.poi_menu, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_cancel -> {
                finish()
            }
        }
        if(edit){
            when (item.itemId) {
                R.id.item_delete -> {
                    app.pois.delete(poi)
                    finish()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

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

    private fun registerImagePickerCallback() {
        imageIntentLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { result ->
                when(result.resultCode){
                    RESULT_OK -> {
                        if (result.data != null) {
                            Timber.i("Got Result ${result.data!!.data}")
                            poi.image = result.data!!.data!!
                            Picasso.get()
                                .load(poi.image)
                                .into(binding.poiImage)
                            binding.chooseImage.setText(R.string.change_poi_image)
                        } // end of if
                    }
                    RESULT_CANCELED -> { } else -> { }
                }
            }
    }
}

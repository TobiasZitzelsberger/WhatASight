package com.example.whatasight.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whatasight.R
import com.example.whatasight.adapters.POIAdapter
import com.example.whatasight.adapters.POIListener
import com.example.whatasight.databinding.ActivityMainMenuBinding
import com.example.whatasight.main.MainApp
import com.example.whatasight.models.Location
import com.example.whatasight.models.POIModel
import timber.log.Timber

class MainMenuActivity : AppCompatActivity(), POIListener {

    lateinit var app: MainApp
    private lateinit var binding: ActivityMainMenuBinding
    private lateinit var refreshIntentLauncher : ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.title = title
        setSupportActionBar(binding.toolbar)

        app = application as MainApp

        val launcherIntent = Intent(this, LoginActivity::class.java)
        launcherIntent.setFlags(launcherIntent.getFlags() or Intent.FLAG_ACTIVITY_NO_HISTORY)
        startActivity(launcherIntent)

        val layoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = POIAdapter(app.pois.findAll(),this)

        binding.openMap.setOnClickListener() {
            val launcherIntent = Intent(this, MapsActivity::class.java)
            refreshIntentLauncher.launch(launcherIntent)
        }

        getLocationPermission()
        registerRefreshCallback()
    }

    override fun onPOIClick(poi: POIModel) {
        val launcherIntent = Intent(this, POIActivity::class.java)
        launcherIntent.putExtra("poi_edit", poi)
        refreshIntentLauncher.launch(launcherIntent)
    }


    private fun registerRefreshCallback() {
        refreshIntentLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { binding.recyclerView.adapter?.notifyDataSetChanged() }
    }
    
    //ask for permission on ACCESS_FINE_LOcATION
    fun getLocationPermission(): Boolean {
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
        }
        return true
    }
}

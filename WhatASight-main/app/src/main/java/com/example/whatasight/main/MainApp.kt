package com.example.whatasight.main

import android.app.Application
import com.example.whatasight.models.POIJSONStore
import com.example.whatasight.models.POIStore
import timber.log.Timber
import timber.log.Timber.i

class MainApp : Application() {

    lateinit var pois: POIStore

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        pois = POIJSONStore(applicationContext)
        i("App started")
    }
}
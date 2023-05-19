package com.example.vegas

import android.app.Application
import com.yandex.mapkit.MapKitFactory
//import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class VegasApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey("3fea70ae-a68a-49b5-acfe-25e652523bca")
    }
}

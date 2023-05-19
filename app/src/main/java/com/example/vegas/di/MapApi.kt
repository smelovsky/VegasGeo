package com.example.vegas.di

import android.content.Context
import android.view.View

interface MapApi {
    fun mapInit(context: Context, title: String, onTap: (latitude: Double, longitude: Double) -> Unit): View
    fun updateMarker(title: String)
    fun setMarker(title:String, latitude: Double, longitude: Double)
    fun updatePolyline()
    fun isZoomEmbedded(): Boolean
    fun zoomIn(): Float
    fun zoomOut(): Float
    fun saveMapSettings()
    fun getMapCenter()
    fun setMapCenter()
    fun onPause()
    fun onResume()
    fun onStart()
    fun onStop()
    fun invalidate()
}
package com.example.vegas.di

import android.content.Context
import android.graphics.Color
import android.preference.PreferenceManager
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import com.example.vegas.BuildConfig
import com.example.vegas.viewModel
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.ScaleBarOverlay

class OpenStreetMapApi: MapApi {

    private lateinit var mapView: org.osmdroid.views.MapView
    private var isMapInited = false
    private var isPlacemarkAdded = false
    private lateinit var placemark: Marker
    private var isPolylineAdded = false
    private var polyline = org.osmdroid.views.overlay.Polyline()

    internal class mapEventsReceiver(var onTap: (latitude: Double, longitude: Double) -> Unit): MapEventsReceiver {
        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
            return false
        }

        override fun longPressHelper(point: GeoPoint?): Boolean {
            if (point != null) {
                onTap (point.latitude, point.longitude)
            }
            return true
        }

    }

    override fun mapInit(context: Context, title: String, onTap: (latitude: Double, longitude: Double) -> Unit): org.osmdroid.views.MapView{

        Configuration.getInstance().load(
            context,
            PreferenceManager.getDefaultSharedPreferences(context)
        )
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID)

        mapView = org.osmdroid.views.MapView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER,
            )
        }

        mapView.setMultiTouchControls(true)
        mapView.setBuiltInZoomControls(true)
        val mapController = mapView.controller

        mapController.setZoom(viewModel.zoom.toDouble())

        val point = if (viewModel.showSavedMarker) {
            GeoPoint(
                viewModel.markerListDetails[viewModel.marker_index].point.latitude,
                viewModel.markerListDetails[viewModel.marker_index].point.longitude
            )
        }
        else {
            if (viewModel.adjustMapSettings) {
                GeoPoint(viewModel.centerLatitude, viewModel.centerLongitude)
            } else {
                GeoPoint(viewModel.locationLatitude, viewModel.locationLongitude)
            }

        }
        mapController.setCenter(point)

        mapView.overlays.add(MapEventsOverlay(mapEventsReceiver(onTap)))
        val scala = ScaleBarOverlay(mapView)
        mapView.overlays.add(scala)
        mapView.invalidate()

        isMapInited = true

        updateMarker(title)
        updatePolyline()

        return mapView
    }

    override fun setMarker(title:String, latitude: Double, longitude: Double) {
        val geoPoint = GeoPoint(latitude, longitude)
        if (isMapInited) {
            var placemark = Marker(mapView)
            placemark.setPosition(geoPoint)
            placemark.setTitle(title)
            mapView.overlays.add(placemark)
        }
    }

    override fun updateMarker(title: String) {

        if (isMapInited) {

            if (isPlacemarkAdded) {
                mapView.overlays.remove(placemark)
                isPlacemarkAdded = false
            }

            if (viewModel.showSavedMarker) {
                val geoPoint = GeoPoint(
                    viewModel.markerListDetails[viewModel.marker_index].point.latitude,
                    viewModel.markerListDetails[viewModel.marker_index].point.longitude
                )
                placemark = Marker(mapView)
                placemark.setPosition(geoPoint)
                placemark.setTitle(viewModel.markerListDetails[viewModel.marker_index].name)
                mapView.overlays.add(placemark)
                isPlacemarkAdded = true
            } else {
                val geoPoint = GeoPoint(
                    viewModel.locationLatitude.toDouble(),
                    viewModel.locationLongitude.toDouble()
                )
                placemark = Marker(mapView)
                placemark.setPosition(geoPoint)
                placemark.setTitle(title)
                mapView.overlays.add(placemark)
                isPlacemarkAdded = true
            }
        }
    }

    override fun updatePolyline() {
        if (isMapInited) {
            if (isPolylineAdded) {
                mapView.getOverlayManager().remove(polyline)
                isPolylineAdded = false
            }

            if (viewModel.showSavedTrack) {
                if (viewModel.trackListDetails[viewModel.track_index].track.size > 0) {
                    val geoPoints = arrayListOf<GeoPoint>()
                    viewModel.trackListDetails[viewModel.track_index].track.forEach { geoPoints.add(GeoPoint(it.latitude, it.longitude)) }
                    polyline.setPoints(geoPoints)
                    polyline.color = Color.BLUE
                    mapView.getOverlayManager().add(polyline)
                    isPolylineAdded = true
                }
            } else {
                if (viewModel.track.size > 1) {
                    val geoPoints = arrayListOf<GeoPoint>()
                    viewModel.track.forEach { geoPoints.add(GeoPoint(it.latitude, it.longitude)) }
                    polyline.setPoints(geoPoints)
                    polyline.color = Color.BLUE
                    mapView.getOverlayManager().add(polyline)
                    isPolylineAdded = true
                }
            }
        }
    }

    override fun isZoomEmbedded(): Boolean { return true }

    override fun zoomIn(): Float { return 0F }

    override fun zoomOut(): Float { return 0F }

    override fun saveMapSettings() {
        viewModel.zoom = mapView.getZoomLevel().toFloat()
        getMapCenter()
        viewModel.adjustMapSettings = true
    }

    override fun getMapCenter()
    {
        var pos = mapView.getMapCenter()
        viewModel.centerLatitude = pos.latitude
        viewModel.centerLongitude = pos.longitude
        Log.d("zzz", "getMapCenter: ${viewModel.centerLatitude}, ${viewModel.centerLongitude}")
    }

    override fun setMapCenter()
    {
        var pos = GeoPoint(viewModel.centerLatitude, viewModel.centerLongitude)
        placemark.setPosition(pos)
    }

    override fun onPause() {
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        mapView.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    override fun onResume() {
        if (isMapInited) {
            mapView.onResume(); //needed for compass, my location overlays, v6.0.0 and up
        }
    }

    override fun onStop() {

    }

    override fun onStart() {

    }

    override fun invalidate() {
        mapView.invalidate()
    }

}

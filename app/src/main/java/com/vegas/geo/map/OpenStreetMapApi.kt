package com.vegas.geo.map

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.preference.PreferenceManager
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.res.ResourcesCompat
import com.vegas.geo.MapMode
import com.vegas.geo.R
import com.vegas.geo.mainViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.ScaleBarOverlay

import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.views.overlay.Polyline

class OpenStreetMapApi: MapApi {

    private lateinit var mapView: org.osmdroid.views.MapView
    private var isMapInited = false

    var currentMarkerTitle = ""
    var scanerMarkerTitle = ""

    private lateinit var roadManager: RoadManager

    private lateinit var roadPolyline: Polyline
    private var isRoadPolylineInited = false

    private lateinit var trackingPolyline: Polyline
    private var isTrackingPolylineInited = false

    private lateinit var currentMarker: Marker
    private var currentMarkerInited = false

    private lateinit var scannerMarker: Marker


    internal class mapEventsReceiver(var onTap: (latitude: Double, longitude: Double) -> Unit):
        MapEventsReceiver {
        override fun singleTapConfirmedHelper(point: GeoPoint?): Boolean {
            if (point != null) {
                //onTap (point.latitude, point.longitude)
            }
            return true
        }

        override fun longPressHelper(point: GeoPoint?): Boolean {
            if (point != null) {
                onTap (point.latitude, point.longitude)
            }
            return true
        }
    }

    override fun mapInit(context: Context, onTap: (latitude: Double, longitude: Double) -> Unit): org.osmdroid.views.MapView{

        currentMarkerTitle = context.getResources().getString(R.string.your_location)

        val contactName = mainViewModel.getContactName()
        scanerMarkerTitle = if (contactName.isEmpty()) mainViewModel.getPhoneNumber() else contactName

        roadManager = OSRMRoadManager(context, "ms")

        Configuration.getInstance().load(
            context,
            PreferenceManager.getDefaultSharedPreferences(context) )
        Configuration.getInstance().setUserAgentValue(context.getPackageName())

        mapView = org.osmdroid.views.MapView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER,
            )
        }

        mapView.setMultiTouchControls(true)
        mapView.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        val mapController = mapView.controller

        mapController.setZoom(mainViewModel.zoom.toDouble())

        mapView.overlays.add(MapEventsOverlay(mapEventsReceiver(onTap)))

        val scala = ScaleBarOverlay(mapView)
        mapView.overlays.add(scala)

        // Set map center (latest or first saved point)
        when (mainViewModel.mapMode) {
            MapMode.LOCATION ->
                {
                    val point =
                        if (mainViewModel.trackingEanabled) {
                            mainViewModel.currentPoint
                        } else {
                            if (mainViewModel.mapPointInited) {
                                mainViewModel.mapPoint
                            } else {
                                mainViewModel.currentPoint
                            }
                        }
                    mapView.controller.setCenter(point)
                }
            MapMode.SAVED_TRACK ->
                {
                    if (mainViewModel.locationList.size > 0) {
                        val point =
                            GeoPoint(
                                mainViewModel.locationList.first().latitude,
                                mainViewModel.locationList.first().longitude)
                        mapView.controller.setCenter(point)
                    }
                }

            MapMode.SCANNER -> {
                val point = mainViewModel.scannerPoint
                mapView.controller.setCenter(point)
            }
        }


        // Set "Your location" marker
        setCurrentMarker()

        if (mainViewModel.mapMode == MapMode.SCANNER) {
            setScannerMarker(context)
        }

        // Set current or saved tracking
        if (mainViewModel.trackingEanabled || mainViewModel.mapMode == MapMode.SAVED_TRACK) {
            setTrackingPolyline()
        }

        if (mainViewModel.isRoadAdded) {
            setRoute()
        }

        mapView.invalidate()

        isMapInited = true

        return mapView
    }

    fun setCurrentMarker() {

        val geoPoint = mainViewModel.currentPoint

        if (currentMarkerInited) {
            mapView.overlays.remove(currentMarker)
        }

        currentMarker = Marker(mapView)
        currentMarker.setPosition(geoPoint)
        currentMarker.setTitle(currentMarkerTitle)


        mapView.overlays.add(currentMarker)
        currentMarkerInited = true

    }

    fun setScannerMarker(context: Context) {

        val geoPoint = mainViewModel.scannerPoint

        scannerMarker = Marker(mapView)
        scannerMarker.setPosition(geoPoint)
        scannerMarker.setTitle(scanerMarkerTitle)
        scannerMarker.icon = ResourcesCompat.getDrawable(context.getResources(), R.drawable.default_pin, null)

        mapView.overlays.add(scannerMarker)

    }

    fun setTrackingPolyline() {
        if (mainViewModel.locationList.size > 0) {

            if (isTrackingPolylineInited) {
                mapView.getOverlays().remove(trackingPolyline)
            }

            trackingPolyline = Polyline()
            val geoPoints = arrayListOf<GeoPoint>()
            mainViewModel.locationList.forEach { geoPoints.add(GeoPoint(it.latitude, it.longitude)) }

            trackingPolyline.setPoints(geoPoints)
            trackingPolyline.color = Color.BLUE

            mapView.getOverlayManager().add(trackingPolyline)
            isTrackingPolylineInited = true

        }
    }

    override fun updateTracking() {
        if (mainViewModel.mapMode == MapMode.LOCATION) {
            setCurrentMarker()
            setTrackingPolyline()
            mapView.invalidate()
        }
    }

    override fun addMarker(title: String, latitude: Double, longitude: Double) {
        val geoPoint = GeoPoint(latitude, longitude)
        var placemark = Marker(mapView)
        placemark.setPosition(geoPoint)
        placemark.setTitle(title)
        mapView.overlays.add(placemark)
    }

    override fun isZoomEmbedded(): Boolean { return false }

    override fun zoomIn(): Float {
        if (mapView.controller.zoomIn()) mainViewModel.zoom++
        return mainViewModel.zoom
    }

    override fun zoomOut(): Float {
        if (mapView.controller.zoomOut()) mainViewModel.zoom--
        return mainViewModel.zoom
    }

    override fun saveMapSettings() {
        mainViewModel.zoom = mapView.getZoomLevel().toFloat()

        var pos = mapView.getMapCenter()
        mainViewModel.mapPoint = GeoPoint(pos.latitude, pos.longitude)
        mainViewModel.mapPointInited = true
    }

    override fun onPause() {
        mapView.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    override fun onResume() {
        if (isMapInited) {
            mapView.onResume(); //needed for compass, my location overlays, v6.0.0 and up
        }
    }

    override fun onStop() {}

    override fun onStart() {}

    override fun invalidate() {
        mapView.invalidate()
    }

    override fun removeRoute() {
        mapView.getOverlays().remove(roadPolyline)
        isRoadPolylineInited = false
        mapView.invalidate()
    }

    override fun addRoute(startPoint: GeoPoint, endPoint: GeoPoint) {
        mainViewModel.startPoint = startPoint
        mainViewModel.endPoint = endPoint

        mainViewModel.setRoute(true)
        mainViewModel.isRoadAdded = true;

        setRoute()
    }

    fun setRoute() {
        GlobalScope.launch {
            val waypoints = ArrayList<GeoPoint>()
            waypoints.add(mainViewModel.startPoint)
            waypoints.add(mainViewModel.endPoint)
            val road = roadManager.getRoad(waypoints)

            if (road.mStatus == Road.STATUS_OK) {

                if (isRoadPolylineInited) {
                    mapView.getOverlays().remove(roadPolyline)
                }

                roadPolyline = RoadManager.buildRoadOverlay(road)
                roadPolyline.color = Color.RED
                roadPolyline.width = 7F


                mapView.getOverlays().add(roadPolyline)

                isRoadPolylineInited = true


                mapView.invalidate()
            }
        }
    }



}
package com.example.vegas.di

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.example.vegas.LayoutCreator
import com.example.vegas.viewModel
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.map.*

class MapKitApi: MapApi {

    private lateinit var mapView: com.yandex.mapkit.mapview.MapView

    private var isMapKitInited = false
    private var isMapKitPlacemarkAdded = false
    private lateinit var marker: PlacemarkMapObject
    private var isMapKitPolylineAdded = false
    private var polylineMapKit: MapObject? = null
    private val layoutCreator = LayoutCreator()

    internal class VegasInputListener(var onTap: (latitude: Double, longitude: Double) -> Unit): InputListener {
        override fun onMapTap(map: com.yandex.mapkit.map.Map, point: Point) {
        }
        override fun onMapLongTap(map: com.yandex.mapkit.map.Map, point: Point) {
            onTap (point.latitude, point.longitude)
        }
    }
    private lateinit var inputListener: VegasInputListener

    override fun mapInit(context: Context, title: String, onTap: (latitude: Double, longitude: Double) -> Unit): View {

        MapKitFactory.initialize(context)

        if (isMapKitInited) {
            if (mapView != null) {
                if (mapView!!.getParent() != null) {
                    (mapView!!.getParent() as ViewGroup).removeView(mapView)
                }
            }
        }
        else {
            mapView = layoutCreator.provideMapView(context) as com.yandex.mapkit.mapview.MapView
        }

        val point = if (viewModel.showSavedMarker) {
            Point(
                viewModel.markerListDetails[viewModel.marker_index].point.latitude,
                viewModel.markerListDetails[viewModel.marker_index].point.longitude
            )
        } else {
            if (viewModel.adjustMapSettings) {
                Point(viewModel.centerLatitude.toDouble(), viewModel.centerLongitude.toDouble())
            } else {
                Point(viewModel.locationLatitude.toDouble(), viewModel.locationLongitude.toDouble())
            }
        }

        mapView!!.getMap().move(
            CameraPosition(point, viewModel.zoom, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 0F),
            null
        )

        MapKitFactory.getInstance().onStart()
        mapView.onStart()

        inputListener = VegasInputListener(onTap)
        mapView!!.getMap().addInputListener(inputListener)

        isMapKitInited = true

        updateMarker(title)
        updatePolyline()

        return mapView
    }

    override fun setMarker(title:String, latitude: Double, longitude: Double) {
        if (isMapKitInited) {
            val point = Point(latitude.toDouble(), longitude.toDouble())
            var marker = mapView?.getMap()?.mapObjects?.addPlacemark(point)!!
            var textStyle: TextStyle = com.yandex.mapkit.map.TextStyle().apply {
                color = Color.BLUE
                setOffsetFromIcon(true)
                setPlacement(TextStyle.Placement.TOP)
            }
            marker.setText(title, textStyle)
        }
    }

    override fun updateMarker(title: String) {

        if (isMapKitInited) {

            val point = if (viewModel.showSavedMarker) {
                Point(
                    viewModel.markerListDetails[viewModel.marker_index].point.latitude,
                    viewModel.markerListDetails[viewModel.marker_index].point.longitude
                )
            }
            else {
                Point(viewModel.locationLatitude.toDouble(), viewModel.locationLongitude.toDouble())
            }

            if (isMapKitPlacemarkAdded) {
                marker.setGeometry(point)
            } else {
                marker = mapView?.getMap()?.mapObjects?.addPlacemark(point)!!
                isMapKitPlacemarkAdded = true
            }

            var textStyle: TextStyle = com.yandex.mapkit.map.TextStyle().apply {
                color = Color.BLUE
                setOffsetFromIcon(true)
                setPlacement(TextStyle.Placement.TOP)
            }

            if (viewModel.showSavedMarker) {
                marker.setText(viewModel.markerListDetails[viewModel.marker_index].name, textStyle)
            }
            else {
                marker.setText(title, textStyle)
            }
        }

    }

    override fun updatePolyline() {
        if (isMapKitInited) {
            if (isMapKitPolylineAdded) {
                mapView?.getMap()?.mapObjects?.remove(polylineMapKit!!)
                isMapKitPolylineAdded = false
            }

            if (viewModel.showSavedTrack) {
                if (viewModel.trackListDetails[viewModel.track_index].track.size > 0) {
                    Log.d("zzz", "Polyline: ${viewModel.trackListDetails[viewModel.track_index].track.size}")
                    var polilyneList: MutableList<Point> = mutableListOf()
                    viewModel.trackListDetails[viewModel.track_index].track.forEach {
                        polilyneList.add(Point(it.latitude, it.longitude))
                    }
                    var polilyne = Polyline(polilyneList)
                    polylineMapKit = mapView?.getMap()?.mapObjects?.addPolyline(polilyne)?.apply {
                        this.strokeWidth = 1.0F
                        this.setStrokeColor(Color.BLUE)
                    }
                    isMapKitPolylineAdded = true
                }
            } else {
                if (viewModel.track.size > 1) {
                    Log.d("zzz", "Polyline: ${viewModel.track.size}")
                    var polilyneList: MutableList<Point> = mutableListOf()
                    viewModel.track.forEach {
                        polilyneList.add(Point(it.latitude, it.longitude))
                    }
                    var polilyne = Polyline(polilyneList)
                    polylineMapKit = mapView?.getMap()?.mapObjects?.addPolyline(polilyne)?.apply {
                        this.strokeWidth = 1.0F
                        this.setStrokeColor(Color.BLUE)
                    }
                    isMapKitPolylineAdded = true
                }
            }
        }
    }

    override fun isZoomEmbedded(): Boolean { return false }

    override fun zoomIn(): Float {
        var zoom = mapView?.getMap()?.cameraPosition?.zoom
        var maxZoom = mapView?.getMap()?.maxZoom
        if (viewModel.zoom + 1 <= maxZoom!!) {
            if (zoom != null) {
                viewModel.zoom = zoom + 1
            }
            getMapCenter()
            setMapCenter()
        }
        return viewModel.zoom
    }

    override fun zoomOut(): Float {
        var zoom = mapView?.getMap()?.cameraPosition?.zoom
        var minZoom = mapView?.getMap()?.minZoom
        if (viewModel.zoom + 1 >= minZoom!!) {
            if (zoom != null) {
                viewModel.zoom = zoom - 1
            }
            getMapCenter()
            setMapCenter()
        }

        return viewModel.zoom
    }

    override fun saveMapSettings() {
        viewModel.zoom = mapView?.getMap()?.cameraPosition?.zoom!!
        getMapCenter()
        viewModel.adjustMapSettings = true
    }

    override fun getMapCenter()
    {
        var pos = mapView!!.getMap().getCameraPosition()
        viewModel.centerLatitude = pos.target.latitude
        viewModel.centerLongitude = pos.target.longitude
    }

    override fun setMapCenter()
    {
        mapView!!.getMap().move(
            CameraPosition(Point(viewModel.centerLatitude, viewModel.centerLongitude), viewModel.zoom, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 0F),
            null
        )
    }

    override fun onPause() {}

    override fun onResume() {}

    override fun onStop() {
        mapView?.onStop()
        MapKitFactory.getInstance().onStop()
    }

    override fun onStart() {
        if (isMapKitInited) {
            MapKitFactory.getInstance().onStart()
            mapView?.onStart()
        }
    }

    override fun invalidate() {
        mapView.invalidate()
    }
}

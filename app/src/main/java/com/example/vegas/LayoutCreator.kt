package com.example.vegas


import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

class LayoutCreator {

    @Volatile
    var container: VideoLayoutContainer? = null

    fun provideMapView(context: Context): View {
        if (container == null) {
            container = createContainer(context)
        }
        return container!!.mapLayout
    }

    private fun createContainer(context: Context): VideoLayoutContainer {

        val mapView = com.yandex.mapkit.mapview.MapView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER,
            )
        }

        return VideoLayoutContainer(mapView)

    }

    data class VideoLayoutContainer(
        val mapLayout: com.yandex.mapkit.mapview.MapView,
    )

}

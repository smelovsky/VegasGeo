package com.example.vegas

import android.os.Environment
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.vegas.di.MapKitApi
import com.example.vegas.di.OpenStreetMapApi
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject


const val default_latitude = 43.3197033
const val default_longitude = 132.1192263

data class MapPoint(
    var latitude: Double,
    var longitude: Double,
)

data class TrackItem(
    var id: Int,
    var name: String,
    var track: List<MapPoint>,
    var isSelected: Boolean = false,
    var isEditMode: Boolean = false,
)

data class MarkerItem(
    var id: Int,
    var name: String,
    var point: MapPoint,
    var isSelected: Boolean = false,
    var isEditMode: Boolean = false,
)

data class TrackBaseItem(
    var id: Int,
)

data class MarkerBaseItem(
    var id: Int,
)

@HiltViewModel
class VegasViewModel @Inject constructor(
    val mapKitApi: MapKitApi,
    val openStreetMapApi: OpenStreetMapApi,
) : ViewModel() {

    var markerListBase by mutableStateOf(listOf<MarkerBaseItem>())
    var markerListDetails = mutableListOf<MarkerItem>()

    fun markerIsEditMode() : Boolean {

        Log.d("zzz", "markerIsEditMode")

        markerListDetails.forEach() {
            if (it.isEditMode) return true
        }
        return false
    }

    fun markerSelectItemAndSetEditMode(index: Int) {

        var i = 0
        while(i  <  markerListDetails.size){
            if (index == i) {
                markerListDetails[i].isSelected = true
                markerListDetails[i].isEditMode = true
            } else {
                markerListDetails[i].isSelected = false
                markerListDetails[i].isEditMode = false
            }
            i++
        }


        val list = markerListBase
        markerListBase = listOf<MarkerBaseItem>()
        markerListBase = list
    }

    fun markerSelectItem(index: Int) {

        var i = 0
        while(i  <  markerListDetails.size){
            markerListDetails[i].isSelected = if (index == i) {
                !markerListDetails[i].isSelected
            } else {
                false
            }
            i++
        }

        val list = markerListBase
        markerListBase = listOf<MarkerBaseItem>()
        markerListBase = list
    }

    fun markerChangeEditMode(index: Int) {

        markerListDetails[index].isEditMode = !markerListDetails[index].isEditMode

        val list = markerListBase
        markerListBase = listOf<MarkerBaseItem>()
        markerListBase = list
    }

    fun markerRenameItem(index: Int, name: String) {

        markerListDetails[index].isEditMode = false
        markerListDetails[index].name = name

        val list = markerListBase
        markerListBase = listOf<MarkerBaseItem>()
        markerListBase = list

        markerSaveAllItems()
    }

    fun markerDeleteSelectedItem() {

        val index = markerGetSelectedItem()
        if (index != -1) {
            markerListBase -= markerListBase[index]
            markerListDetails.removeAt(index)

            val list = markerListBase
            markerListBase = listOf<MarkerBaseItem>()
            markerListBase = list

            markerSaveAllItems()
        }
    }

    fun markerDeleteAllItems() {
        markerListDetails.clear()
        markerListBase = listOf<MarkerBaseItem>()

        markerListDetails = mutableListOf<MarkerItem>()
        File(fileNameMarker).delete()
    }

    fun markerGetSelectedItem() : Int {
        var i = 0
        while(i  <  markerListDetails.size){
            if (markerListDetails[i].isSelected) { return i }
            i++
        }
        return -1
    }

    fun markerAddItem(name: String, latitude: Double, longitude: Double) {
        val index = markerListDetails.lastIndex
        val id = if (index != -1) {
            markerListDetails[index].id + 1
        } else { 0 }

        markerListDetails.add(MarkerItem(id, name, MapPoint(latitude, longitude)))
        markerListBase += MarkerBaseItem(id)

        markerSaveAllItems()
    }

    fun markerSaveAllItems() {
        if (makeDir()) {
            val file = File(fileNameMarker)
            var firstItem = true
            markerListDetails.forEach() {

                var str = "${it.name}:${it.point.latitude},${it.point.longitude}\n"

                if (firstItem) {
                    file.writeText(str)
                    firstItem = false
                } else {
                    file.appendText(str)
                }
            }
        }
    }

    fun markerReadAllItems() {
        val file = File(fileNameMarker)
        if (file.exists()) {
            val lines = file.readLines()
            var id = 0
            lines.forEach() {
                var startIndex = it.lastIndexOf(':')
                val name = it.substring(0, startIndex)
                val points = it.substring(startIndex + 1)
                var list = points.split(",").map {it.trim().toDouble() }

                if (list.size == 2) {
                    markerListBase += MarkerBaseItem(id)
                    markerListDetails.add(MarkerItem(id++, name, MapPoint(list[0], list[1])))
                }

            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    var trackListBase by mutableStateOf(listOf<TrackBaseItem>())
    var trackListDetails = mutableListOf<TrackItem>()

    fun trackIsEditMode() : Boolean {

        //Log.d("zzz", "isEditMode")

        trackListDetails.forEach() {
            if (it.isEditMode) return true
        }
        return false
    }

    fun trackSelectItemAndSetEditMode(index: Int) {

        //Log.d("zzz", "selectAndEditMode")

        var i = 0
        while(i  <  trackListDetails.size){
            if (index == i) {
                trackListDetails[i].isSelected = true
                trackListDetails[i].isEditMode = true
            } else {
                trackListDetails[i].isSelected = false
                trackListDetails[i].isEditMode = false
            }
            i++
        }


        val list = trackListBase
        trackListBase = listOf<TrackBaseItem>()
        trackListBase = list
    }

    fun trackSelectItem(index: Int) {

        //Log.d("zzz", "selectItem")

        var i = 0
        while(i  <  trackListDetails.size){
            trackListDetails[i].isSelected = if (index == i) {
                !trackListDetails[i].isSelected
            } else {
                false
            }
            i++
        }
        //vegasListDetails[index].isSelected = !vegasListDetails[index].isSelected
        val list = trackListBase
        trackListBase = listOf<TrackBaseItem>()
        trackListBase = list
    }

    fun trackChangeEditMode(index: Int) {
        //Log.d("zzz", "changeEditMode")

        trackListDetails[index].isEditMode = !trackListDetails[index].isEditMode

        val list = trackListBase
        trackListBase = listOf<TrackBaseItem>()
        trackListBase = list
    }

    fun trackRenameItem(index: Int, name: String) {
        //Log.d("zzz", "renameItem")

        trackListDetails[index].isEditMode = false
        trackListDetails[index].name = name

        val list = trackListBase
        trackListBase = listOf<TrackBaseItem>()
        trackListBase = list

        trackSaveAllItems()
    }

    fun trackDeleteSelectedItem() {

        //Log.d("zzz", "deleteSelectedItem")

        val index = trackGetSelectedItem()

        if (index != -1) {
            trackListBase -= trackListBase[index]
            trackListDetails.removeAt(index)

            val list = trackListBase
            trackListBase = listOf<TrackBaseItem>()
            trackListBase = list

            trackSaveAllItems()
        }

    }

    fun trackDeleteAllItems() {
        //Log.d("zzz", "deleteAllItems")
        trackListDetails.clear()
        trackListBase = listOf<TrackBaseItem>()

        trackListDetails = mutableListOf<TrackItem>()
        File(fileNameTrack).delete()
    }

    fun trackGetSelectedItem() : Int {

        //Log.d("zzz", "selectedItem")

        var i = 0
        while(i  <  trackListDetails.size){
            if (trackListDetails[i].isSelected) { return i }
            i++
        }

        return -1
    }

    fun trackAddItem() {
        if (track.size > 0) {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val time = LocalDateTime.now().format(formatter)

            val index = trackListDetails.lastIndex
            val id = if (index != -1) {
                trackListDetails[index].id + 1
            } else { 0 }

            var itemTrack: MutableList<MapPoint> = mutableListOf()

            track.forEach {
                itemTrack.add(it)
            }

            trackListDetails.add(TrackItem(id, time, itemTrack))
            trackListBase += TrackBaseItem(id)

            trackSaveAllItems()
        }
    }

    fun trackSaveAllItems() {
        if (makeDir()) {
            val file = File(fileNameTrack)
            var firstItem = true
            trackListDetails.forEach() {
                var str = it.track.joinToString (separator = ",", prefix = "${it.name}:", postfix = "\n")
                { point -> "${point.latitude},${point.longitude}" }

                if (firstItem) {
                    file.writeText(str)
                    firstItem = false
                } else {
                    file.appendText(str)
                }
            }
        }
    }

    fun trackReadAllItems() {
        //Log.d("zzz", "readTrackingList")

        val file = File(fileNameTrack)
        if (file.exists()) {
            val lines = file.readLines()
            var id = 0
            lines.forEach() {

                var startIndex = it.lastIndexOf(':')
                var name = "empty"
                var track: MutableList<MapPoint> = mutableListOf<MapPoint>()

                if (startIndex != -1) {
                    name = it.substring(0, startIndex)
                    val points = it.substring(startIndex + 1)

                    var list = points.split(",").map {it.trim().toDouble() }

                    var index = 0
                    while (index < list.size) {
                        track.add(MapPoint(list[index],list[index+1]))
                        index += 2
                    }
                }

                //trackListDetails.add(VegasDetailsItem(id,"[${id}] ${name}"))
                trackListBase += TrackBaseItem(id)
                trackListDetails.add(TrackItem(id++, name, track))


            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    val pathVegas = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString() +
            File.separator +
            "Vegas" +
            File.separator
    ////////////////////////////////////////////////////////////////////////////////////////////////

    val minDistance = listOf(1, 10, 100)
    val maps = listOf("Yandex", "Open Street")
    var track: MutableList<MapPoint> = mutableListOf<MapPoint>()

    var track_index = -1
    var marker_index = -1

    var zoom = 15.0F
    var locationLatitude = 43.15795
    var locationLongitude = 131.91815

    var centerLatitude = 43.15795
    var centerLongitude = 131.91815
    var adjustMapSettings = false
    var trackEnabled = false
    var showSavedMarker = false
    var showSavedTrack = false

    ////////////////////////////////////////////////////////////////////////////////////////////////

    var currentTheme: Int = 0
    var askToExitFromApp: Boolean = true
    var askToDeleteSelectedItem: Boolean = true
    var askToSaveTracking: Boolean = false
    var wakeLock: Boolean = false
    var currentMinDistance: Int = minDistance[2]
    var currentMap: String = maps[0]

    ////////////////////////////////////////////////////////////////////////////////////////////////

    var fileNameMarker = pathVegas + "place_markers.txt"
    var fileNameTrack = pathVegas + "tracking_points.txt"


    fun makeDir(): Boolean {
        val dir = File(pathVegas)
        if (!dir.exists()) {
            return dir.mkdirs()
        }
        return true
    }

    init {
    }

    override fun onCleared() {
        super.onCleared()

    }
}

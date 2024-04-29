package com.vegas.geo.data.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "location",
    foreignKeys = [
        ForeignKey(
            entity = TrackEntity::class,
            parentColumns = ["track_id"],
            childColumns = ["track_id"],
            onDelete = ForeignKey.CASCADE
        ),
    ],
    indices = [
        Index(value = ["location_id"]),
        Index(value = ["track_id"]),
    ],

)

data class LocationEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "location_id")
    val locationId: Long = 0,

    @ColumnInfo(name = "track_id")
    val trackId: Long = 0,

    @ColumnInfo(name = "latitude")
    val latitude: Double,

    @ColumnInfo(name = "longitude")
    val longitude: Double,
)

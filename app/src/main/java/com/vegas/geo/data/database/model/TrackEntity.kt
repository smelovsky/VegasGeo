package com.vegas.geo.data.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "track",
)
data class TrackEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "track_id")
    val trackId: Long = 0,

    @ColumnInfo(name = "track_name")
    val trackName: String,

    @ColumnInfo(name = "is_selected")
    val isSelected: Boolean,

    @ColumnInfo(name = "is_edit_mode")
    val isEditMode: Boolean,
)

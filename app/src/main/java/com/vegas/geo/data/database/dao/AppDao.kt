package com.vegas.geo.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vegas.geo.data.database.model.LocationEntity
import com.vegas.geo.data.database.model.TrackEntity

@Dao
interface AppDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrack(trackEntity: TrackEntity): Long

    @Update
    suspend fun updateTrack(trackEntity: TrackEntity)

    @Query(
        value = """
            SELECT *
            FROM track
            where track_id = :trackId
        """
    )
    suspend fun selectTrack(trackId: Long): List<TrackEntity>

    @Query(
        value = """
            SELECT *
            FROM track
            where track_name = :trackName
        """
    )
    suspend fun selectTrack(trackName: String): List<TrackEntity>

    @Query(value = "SELECT * FROM track")
    fun getTrackList(): List<TrackEntity>

    @Query(value = """
        UPDATE track SET
        track_name = :trackName
        where track_id = :trackId
    """)
    suspend fun updateTrack(trackId: Long, trackName: String)

    @Query(value = """
        UPDATE track SET
        is_selected = :isSelected, is_edit_mode = :isEditMode
        where track_id = :trackId
    """)
    suspend fun selectTrackById(trackId: Long, isSelected: Boolean, isEditMode: Boolean)

    @Query(value = """
        UPDATE track SET
        is_edit_mode = false, is_selected = false
    """)
    suspend fun unselectAllTracks()

    @Query(value = """
        DELETE FROM track
        where track_id = :trackId
    """)
    suspend fun deleteTrack(trackId: Long)

    @Query(value = """
        DELETE FROM track
        where is_selected = true
    """)
    suspend fun deleteSelectedTracks()

    @Query(value = """
        DELETE FROM track
    """)
    suspend fun deleteAllTracks()


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLocation(locationEntity: LocationEntity): Long

    @Query(
        value = """
            SELECT *
            FROM location
            where track_id = :trackId
        """
    )
    suspend fun selectLocation(trackId: Long): List<LocationEntity>

    @Query(
        value = """
            SELECT * FROM location where track_id in 
            (SELECT track_id FROM track WHERE is_selected = true) 
        """
    )
    fun getLocationList(): List<LocationEntity>

    @Query(
        value = """
            SELECT * FROM location where track_id = :trackId 
        """
    )
    fun getLocationListById(trackId: Long): List<LocationEntity>

    @Query(value = """
        DELETE FROM location
        where track_id in (SELECT track_id FROM track where is_selected = true)
    """)
    suspend fun deleteLocationsSelectedTracks()

}
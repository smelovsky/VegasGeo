package com.vegas.geo.data.repository

import com.vegas.geo.data.database.model.LocationEntity
import kotlinx.coroutines.flow.Flow

interface RepositoryApi {
    suspend fun insertOrUpdate(
        track: String,
        latitude:Double,
        longitude:Double,
    ): Long

    fun getTrack(): Flow<List<LocationEntity>>
}
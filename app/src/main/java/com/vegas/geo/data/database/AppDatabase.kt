package com.vegas.geo.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.vegas.geo.data.database.dao.AppDao
import com.vegas.geo.data.database.model.LocationEntity
import com.vegas.geo.data.database.model.TrackEntity

@Database(
    version = 1,
    entities = [
        LocationEntity::class,
        TrackEntity::class,
    ]
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun appDao(): AppDao

}
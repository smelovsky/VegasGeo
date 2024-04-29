package com.vegas.geo.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.example.vegasbluetooth.sound.SoundApi
import com.example.vegasbluetooth.sound.SoundImpl
import com.vegas.geo.data.database.AppDatabase
import com.vegas.geo.data.database.dao.AppDao
import com.vegas.geo.map.OpenStreetMapApi
import com.vegas.geo.permissions.PermissionsApi
import com.vegas.geo.permissions.PermissionsImpl
import com.vegas.geo.settings.GlobalSettingsRepositoryApi
import com.vegas.geo.settings.GlobalSettingsRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PermissionsModule {
    @Provides
    fun providePermissionsApi(@ApplicationContext appContext: Context): PermissionsApi {

        return PermissionsImpl(appContext)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object GlobalSettingsRepositoryModule {
    @Provides
    fun provideGlobalSettingsRepositoryApi(@ApplicationContext appContext: Context): GlobalSettingsRepositoryApi {

        return GlobalSettingsRepositoryImpl(appContext)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java, "location_db"
        ).build()
    }

    @Provides
    fun provideAppDao(database: AppDatabase): AppDao {
        return database.appDao()
    }

}

@Module
@InstallIn(SingletonComponent::class)
object OpenStreetMapModule {
    @Provides
    fun provideMapApi(): OpenStreetMapApi {
        return OpenStreetMapApi()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object SoundModule {
    @Provides
    @Singleton
    fun provideSoundApi(app: Application): SoundApi {

        return SoundImpl(app)
    }
}

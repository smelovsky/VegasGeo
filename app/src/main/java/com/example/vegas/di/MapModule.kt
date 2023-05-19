package com.example.vegas.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object MapKitModule {
    @Provides
    fun provideMapKitApi(): MapKitApi {
        return MapKitApi()
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
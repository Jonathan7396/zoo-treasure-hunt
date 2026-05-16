package com.math0490.flinders.zootreasurehunt.di

import android.content.Context
import com.math0490.flinders.zootreasurehunt.data.SightingDao
import com.math0490.flinders.zootreasurehunt.data.ZooDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideZooDatabase(
        @ApplicationContext context: Context
    ): ZooDatabase {
        return ZooDatabase.getDatabase(context)
    }

    @Provides
    fun provideSightingDao(database: ZooDatabase): SightingDao {
        return database.sightingDao()
    }
}
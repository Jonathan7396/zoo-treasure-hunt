package com.math0490.flinders.zootreasurehunt.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.math0490.flinders.zootreasurehunt.model.SightingEntity

@Database(
    entities = [SightingEntity::class],
    version = 2,
    exportSchema = false
)
abstract class ZooDatabase : RoomDatabase() {

    abstract fun sightingDao(): SightingDao

    companion object {
        @Volatile
        private var INSTANCE: ZooDatabase? = null

        fun getDatabase(context: Context): ZooDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ZooDatabase::class.java,
                    "zoo_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
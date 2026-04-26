package com.math0490.flinders.zootreasurehunt.data

import androidx.room.*
import com.math0490.flinders.zootreasurehunt.model.SightingEntity

@Dao
interface SightingDao {
    @Query("Select * FROM sightings")
    suspend fun getAll(): List<SightingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sighting: SightingEntity)

    @Update
    suspend fun update(sighting: SightingEntity)
    @Delete
    suspend fun delete(sighting: SightingEntity)

    @Query("Select * FROM sightings ORDER BY name ASC")
    suspend fun getSortedByName(): List<SightingEntity>

    @Query("SELECT * FROM sightings ORDER BY isFound DESC")
    suspend fun getSortedByFound(): List<SightingEntity>

}






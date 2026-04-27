package com.math0490.flinders.zootreasurehunt.data

import androidx.room.*
import com.math0490.flinders.zootreasurehunt.model.SightingEntity


//Dao provides direct database access for sighting entities
@Dao
interface SightingDao {
    //Retrieves all sightings from the database
    @Query("Select * FROM sightings")
    suspend fun getAll(): List<SightingEntity>

    //Inserts or updates a sighting in the database
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sighting: SightingEntity)

    //Updates a sighting in the database
    @Update
    suspend fun update(sighting: SightingEntity)

    //Deletes a sighting from the database
    @Delete
    suspend fun delete(sighting: SightingEntity)

    //Sorts the sightings by name in ascending order
    @Query("Select * FROM sightings ORDER BY name ASC")
    suspend fun getSortedByName(): List<SightingEntity>

    //Sorts the sightings by found status in descending order
    @Query("SELECT * FROM sightings ORDER BY isFound DESC")
    suspend fun getSortedByFound(): List<SightingEntity>

}






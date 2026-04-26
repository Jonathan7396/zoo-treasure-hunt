package com.math0490.flinders.zootreasurehunt.data
import com.math0490.flinders.zootreasurehunt.model.Sighting

interface SightingRepository {
    suspend fun saveSightings(sightings: List<Sighting>)

    suspend fun loadSightings(): List<Sighting>

    suspend fun addSighting(sighting: Sighting)

    suspend fun updateSighting(sighting: Sighting)

    suspend fun deleteSighting(sighting: Sighting)
    suspend fun getSortedByName(): List<Sighting>

    suspend fun getSortedByFound(): List<Sighting>
}


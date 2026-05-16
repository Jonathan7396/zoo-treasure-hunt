package com.math0490.flinders.zootreasurehunt.data

import com.math0490.flinders.zootreasurehunt.model.Sighting
import com.math0490.flinders.zootreasurehunt.model.SightingEntity
import javax.inject.Inject

// Room based implementation of the sighting repository
class RoomSightingRepository @Inject constructor(
    private val dao: SightingDao
) : SightingRepository {

    // Loads sightings from the database or creates default sightings if the database is empty
    override suspend fun loadSightings(): List<Sighting> {
        val current = dao.getAll()

        if (current.isEmpty()) {
            val defaults = getDefaultSightings()
            defaults.forEach {
                dao.insert(it.toEntity())
            }
            return defaults
        }

        return current.map { it.toSighting() }
    }

    override suspend fun saveSightings(sightings: List<Sighting>) {
        sightings.forEach {
            dao.insert(it.toEntity())
        }
    }

    override suspend fun addSighting(sighting: Sighting) {
        dao.insert(sighting.toEntity())
    }

    // Updates an existing sighting in the database
    override suspend fun updateSighting(sighting: Sighting) {
        dao.update(sighting.toEntity())
    }

    override suspend fun deleteSighting(sighting: Sighting) {
        dao.delete(sighting.toEntity())
    }

    // Retrieves sightings sorted by name using a database query
    override suspend fun getSortedByName(): List<Sighting> {
        return dao.getSortedByName().map { it.toSighting() }
    }

    // Retrieves sightings sorted by found status using a database query
    override suspend fun getSortedByFound(): List<Sighting> {
        return dao.getSortedByFound().map { it.toSighting() }
    }

    // Provides default sightings for the app when the database is empty
    // Provides default sightings for the app when the database is empty
    private fun getDefaultSightings(): List<Sighting> {
        return listOf(
            Sighting(
                name = "Lion",
                imageUrl = "https://wilk0077.github.io/comp2012-images/assets-sm/african-lion-ai.jpg",
                latitude = -34.9126,
                longitude = 138.6062
            ),
            Sighting(
                name = "Red Panda",
                imageUrl = "https://wilk0077.github.io/comp2012-images/assets-sm/red-panda-ai.jpg",
                latitude = -34.9128,
                longitude = 138.6065
            ),
            Sighting(
                name = "Giraffe",
                imageUrl = "https://wilk0077.github.io/comp2012-images/assets-sm/giraffe-ai.jpg",
                latitude = -34.9130,
                longitude = 138.6068
            ),
            Sighting(
                name = "Kangaroo",
                imageUrl = "https://wilk0077.github.io/comp2012-images/assets-sm/red-kangaroo-ai.jpg",
                latitude = -34.9132,
                longitude = 138.6070
            ),
            Sighting(
                name = "Penguin",
                imageUrl = "https://wilk0077.github.io/comp2012-images/assets-sm/penguin-ai.jpg",
                latitude = -34.9134,
                longitude = 138.6072
            )
        )
    }
}

// Converts a database entity into a domain model object
fun SightingEntity.toSighting(): Sighting {
    return Sighting(
        id = id,
        name = name,
        isFound = isFound,
        notes = notes,
        imageUrl = imageUrl,
        photoPath = photoPath,
        timestamp = timestamp,
        latitude = latitude,
        longitude = longitude
    )
}

// Converts a domain model object into a database entity
fun Sighting.toEntity(): SightingEntity {
    return SightingEntity(
        id = id,
        name = name,
        imageUrl = imageUrl,
        photoPath = photoPath,
        isFound = isFound,
        notes = notes,
        timestamp = timestamp,
        latitude = latitude,
        longitude = longitude
    )
}
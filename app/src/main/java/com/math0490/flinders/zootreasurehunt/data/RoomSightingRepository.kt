package com.math0490.flinders.zootreasurehunt.data


import android.util.Log
import com.math0490.flinders.zootreasurehunt.model.Sighting
import com.math0490.flinders.zootreasurehunt.model.SightingEntity

class RoomSightingRepository(
    private val dao: SightingDao
) : SightingRepository {

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

    override suspend fun updateSighting(sighting: Sighting) {

        dao.update(sighting.toEntity())
    }

    override suspend fun deleteSighting(sighting: Sighting) {
        dao.delete(sighting.toEntity())
    }
    override suspend fun getSortedByName(): List<Sighting> {
        return dao.getSortedByName().map { it.toSighting() }
    }

    override suspend fun getSortedByFound(): List<Sighting> {
        return dao.getSortedByFound().map { it.toSighting() }
    }
    private fun getDefaultSightings(): List<Sighting> {
        return listOf(
            Sighting(
                name = "Lion",
                imageUrl = "https://wilk0077.github.io/comp2012-images/assets-sm/african-lion-ai.jpg"
            ),
            Sighting(
                name = "Red Panda",
                imageUrl = "https://wilk0077.github.io/comp2012-images/assets-sm/red-panda-ai.jpg"
            ),
            Sighting(
                name = "Giraffe",
                imageUrl = "https://wilk0077.github.io/comp2012-images/assets-sm/giraffe-ai.jpg"
            ),
            Sighting(
                name = "Kangaroo",
                imageUrl = "https://wilk0077.github.io/comp2012-images/assets-sm/red-kangaroo-ai.jpg"
            ),
            Sighting(
                name = "Penguin",
                imageUrl = "https://wilk0077.github.io/comp2012-images/assets-sm/penguin-ai.jpg"
            )
        )
    }
}
private fun SightingEntity.toSighting(): Sighting {
    return Sighting(
        id = id,
        name = name,
        isFound = isFound,
        notes = notes,
        imageUrl = imageUrl,
        photoPath = photoPath
    )
}

private fun Sighting.toEntity(): SightingEntity {
    return SightingEntity(
        id = id,
        name = name,
        imageUrl = imageUrl,
        photoPath = photoPath,
        isFound = isFound,
        notes = notes
    )
}
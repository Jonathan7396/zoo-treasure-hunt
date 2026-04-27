package com.math0490.flinders.zootreasurehunt.data

import android.content.Context
import com.math0490.flinders.zootreasurehunt.model.Sighting
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

//File based implementation of the sighting repository.
class FileSightingRepository(private val context: Context): SightingRepository {

    private val fileName = "sightings.json"

    override suspend fun saveSightings(sightings: List<Sighting>) {
        withContext(Dispatchers.IO) {
            val jsonString = Json.encodeToString(sightings)

            context.openFileOutput(fileName, Context.MODE_PRIVATE).use { outputStream ->
                outputStream.write(jsonString.toByteArray())
            }
        }
    }

    //Default data used when no file exists
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

    //Loads sightings from file or creates default sightings if file does not exist
    override suspend fun loadSightings(): List<Sighting> {
        return withContext(Dispatchers.IO) {
            val file = File(context.filesDir, fileName)

            if (!file.exists()) return@withContext getDefaultSightings()

            try {
                val jsonString = context.openFileInput(fileName)
                    .bufferedReader()
                    .use { it.readText() }

                Json.decodeFromString<List<Sighting>>(jsonString)
            } catch (e: Exception) {
                getDefaultSightings()
            }
        }
    }

    // Adds a new sighting to the list
    override suspend fun addSighting(sighting: Sighting) {
        val currentList = loadSightings().toMutableList()
        currentList.add(sighting)
        saveSightings(currentList)
    }

    //Updates an existing sighting based on the ID
    override suspend fun updateSighting(sighting: Sighting) {
        val currentList = loadSightings().map {
            if (it.id == sighting.id) sighting else it
        }
        saveSightings(currentList)
    }
    //Removes a sighting from the list
    override suspend fun deleteSighting(sighting: Sighting) {
        val currentList = loadSightings().filter { it.id != sighting.id }
        saveSightings(currentList)
    }
    //Returns sightings sorted by name
    override suspend fun getSortedByName(): List<Sighting> {
        return loadSightings().sortedBy { it.name }
    }
    //Returns sightings sorted by found status
    override suspend fun getSortedByFound(): List<Sighting> {
        return loadSightings().sortedByDescending { it.isFound }
    }
}

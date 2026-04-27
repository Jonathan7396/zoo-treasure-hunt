package com.math0490.flinders.zootreasurehunt.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.math0490.flinders.zootreasurehunt.model.Sighting
import com.math0490.flinders.zootreasurehunt.data.SightingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.workDataOf
import com.math0490.flinders.zootreasurehunt.worker.CongratulationWorker
import com.math0490.flinders.zootreasurehunt.data.SettingsRepository
import com.math0490.flinders.zootreasurehunt.model.ZooUiState
import kotlinx.coroutines.flow.asStateFlow
import android.util.Log

// ZooView Model is responsible for managing the sighting data, user preferences and coordinating all the  database operations within the UI
class ZooViewModel(
    private val repository: SightingRepository,
    private val settingsRepository: SettingsRepository,
    application: Application) : AndroidViewModel(application) {

    private val TAG = "ZooViewModel"
    private val workManager = WorkManager.getInstance(application)
    private val _sightings = MutableStateFlow<List<Sighting>>(emptyList())
    val sightings: StateFlow<List<Sighting>> = _sightings
    private val _rawSightings = MutableStateFlow<List<Sighting>>(emptyList())
    private val _uiState = MutableStateFlow(ZooUiState())
    val uiState: StateFlow<ZooUiState> = _uiState.asStateFlow()

    // Load sightings from the repository when the ViewModel is created
    init {
        viewModelScope.launch {
            try {
                _rawSightings.value = repository.loadSightings()
            } catch (e: Exception) {
                Log.e(TAG, "Error loading sightings", e)
            }
        }

        viewModelScope.launch {
            combine(_rawSightings, settingsRepository.sortByNameFlow) { _, sortByName ->
                // Sort the sightings based on the user's preference
                val sortedList = try {
                    if (sortByName) {
                        repository.getSortedByName()
                    } else {
                        repository.getSortedByFound()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error sorting sightings", e)
                    _rawSightings.value
                }

                _uiState.value.copy(
                    sightings = sortedList,
                    isSortByName = sortByName
                )

            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun updateSighting(updated: Sighting) {
        val updatedWithTimestamp = updated.copy(timestamp = System.currentTimeMillis())
        val oldSighting = _rawSightings.value.find { it.id == updated.id }

        if (updatedWithTimestamp.isFound && oldSighting?.isFound != true)  {
            val workRequest = OneTimeWorkRequestBuilder<CongratulationWorker>()
                .setInputData(workDataOf("ANIMAL_NAME" to updated.name))
                .build()
            workManager.enqueue(workRequest)
        }

        viewModelScope.launch {
            try {
                val exists = _rawSightings.value.any { it.id == updatedWithTimestamp.id }

                if (exists) {
                    repository.updateSighting(updatedWithTimestamp)
                } else {
                    repository.addSighting(updatedWithTimestamp)
                }

                _rawSightings.value = repository.loadSightings()
            } catch (e: Exception) {
                Log.e(TAG, "Error saving the  sighting", e)
            }
        }
    }
    //Deletes a sighting and the reloads the list
    fun deleteSighting(sighting: Sighting) {
        viewModelScope.launch {
            try {
                repository.deleteSighting(sighting)
                _rawSightings.value = repository.loadSightings()
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting the sighting", e)
            }
        }
    }
    //Updates the user's sort preference
    fun toggleSortOrder(sortByName: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSortByName(sortByName)
        }
    }
    //Sets the selected sighting and controls the visibility of the dialog
    fun selectSightingForEdit(sighting: Sighting?) {

        _uiState.value = _uiState.value.copy(selectedSighting = sighting, isDialogVisible = sighting != null)

    }
    //Closes the edit dialog
    fun dismissDialog(){
        _uiState.value = _uiState.value.copy(selectedSighting = null, isDialogVisible = false)
    }



}
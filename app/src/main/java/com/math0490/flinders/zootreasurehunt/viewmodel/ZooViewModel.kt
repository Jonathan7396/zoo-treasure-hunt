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

class ZooViewModel(
    private val repository: SightingRepository,
    private val settingsRepository: SettingsRepository,
    application: Application) : AndroidViewModel(application) {



    private val workManager = WorkManager.getInstance(application)
    private val _sightings = MutableStateFlow<List<Sighting>>(emptyList())
    val sightings: StateFlow<List<Sighting>> = _sightings
    private val _rawSightings = MutableStateFlow<List<Sighting>>(emptyList())
    private val _uiState = MutableStateFlow(ZooUiState())
    val uiState: StateFlow<ZooUiState> = _uiState.asStateFlow()

    val isSortByName = settingsRepository.sortByNameFlow
    init {
        viewModelScope.launch {
            _rawSightings.value = repository.loadSightings()
        }

        viewModelScope.launch {
            combine(_rawSightings, settingsRepository.sortByNameFlow) { _, sortByName ->

                val sortedList =
                    if (sortByName) {
                        repository.getSortedByName()
                    } else {
                        repository.getSortedByFound()
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

        if (updatedWithTimestamp.isFound && oldSighting?.isFound == false) {
            val workRequest = OneTimeWorkRequestBuilder<CongratulationWorker>()
                .setInputData(workDataOf("ANIMAL_NAME" to updated.name))
                .build()
            workManager.enqueue(workRequest)
        }

        viewModelScope.launch {
            repository.updateSighting(updatedWithTimestamp)
            _rawSightings.value = repository.loadSightings()
        }
    }
    fun deleteSighting(sighting: Sighting) {
        viewModelScope.launch {
            repository.deleteSighting(sighting)
            _rawSightings.value = repository.loadSightings()
        }
    }
    fun toggleSortOrder(sortByName: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSortByName(sortByName)
        }
    }
    fun selectSightingForEdit(sighting: Sighting?) {

        _uiState.value = _uiState.value.copy(selectedSighting = sighting, isDialogVisible = sighting != null)

    }
    fun dismissDialog(){
        _uiState.value = _uiState.value.copy(selectedSighting = null, isDialogVisible = false)
    }



}
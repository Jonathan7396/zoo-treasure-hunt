package com.math0490.flinders.zootreasurehunt.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.math0490.flinders.zootreasurehunt.Sighting
import com.math0490.flinders.zootreasurehunt.data.SightingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.workDataOf
import com.math0490.flinders.zootreasurehunt.worker.CongratulationWorker
import com.math0490.flinders.zootreasurehunt.data.SettingsRepository
class ZooViewModel(application: Application) : AndroidViewModel(application) {


    private val repository = SightingRepository(application)
    private val settingsRepository = SettingsRepository(application)
    private val workManager = WorkManager.getInstance(application)
    private val _sightings = MutableStateFlow<List<Sighting>>(emptyList())
    val sightings: StateFlow<List<Sighting>> = _sightings
    private val _rawSightings = MutableStateFlow<List<Sighting>>(emptyList())

    val isSortByName = settingsRepository.sortByNameFlow
    init {
        viewModelScope.launch { _rawSightings.value = repository.loadSightings() }
        viewModelScope.launch {
            combine(_rawSightings, settingsRepository.sortByNameFlow) { list, sortByName ->
                if (sortByName) {
                    list.sortedBy { it.name }
                } else {
                    list.sortedByDescending { it.isFound }
                }
            }.collect { sortedList ->
                _sightings.value = sortedList
            }
        }

    }
    private fun updateAndSave(newList: List<Sighting>){
        _rawSightings.value = newList
        viewModelScope.launch {
            repository.saveSightings(newList)
        }

    }
    fun updateSighting(updated: Sighting){
        val oldSighting = _rawSightings.value.find { it.id == updated.id }
        if (updated.isFound && oldSighting?.isFound == false){
            val workRequest = OneTimeWorkRequestBuilder<CongratulationWorker>()
                .setInputData(workDataOf("ANIMAL_NAME" to updated.name))
                .build()
            workManager.enqueue(workRequest)

        }
        val newList = _rawSightings.value.map { if (it.id == updated.id) updated else it }
        updateAndSave(newList)


    }
    fun deleteSighting(sighting: Sighting) {
        val newList = _rawSightings.value.filter { it.id != sighting.id }
        updateAndSave(newList)
    }
    fun toggleSortOrder(sortByName: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSortByName(sortByName)
        }
    }


}
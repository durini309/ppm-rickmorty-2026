package com.gt.uvg.rickandmorty.location.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.gt.uvg.rickandmorty.location.data.LocationDb
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class LocationDetailViewModel(
    savedStateHandle: SavedStateHandle
): ViewModel() {
    private val _state = MutableStateFlow(LocationDetailState())
    val state = _state.asStateFlow()

    var job: Job? = null

    private val destination = savedStateHandle.toRoute<LocationDetailDestination>()
    private val locationId = destination.locationId

    val db by lazy {
        LocationDb()
    }

    init {
        fetchData()
    }

    fun onEvent(event: LocationDetailEvent) {
        when (event) {
            LocationDetailEvent.LoadingClick -> loadingClick()
            LocationDetailEvent.RetryClick -> retryClick()
        }
    }

    private fun fetchData() {
        job?.cancel()
        job = viewModelScope.launch {
            delay(2.seconds)
            val location = db.getLocationById(locationId)
            _state.update {
                it.copy(
                    isLoading = false,
                    location = location
                )
            }
        }
    }

    private fun loadingClick() {
        job?.cancel()
        _state.update {
            it.copy(
                isLoading = false,
                hasError = true
            )
        }
    }

    private fun retryClick() {
        job?.cancel()
        _state.update {
            it.copy(
                isLoading = true,
                hasError = false
            )
        }
        fetchData()
    }

}

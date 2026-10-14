package com.gt.uvg.rickandmorty.location.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gt.uvg.rickandmorty.location.data.LocationDb
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class LocationsViewModel: ViewModel() {
    private val _state = MutableStateFlow(LocationsState())
    val state = _state.asStateFlow()

    var job: Job? = null

    val db by lazy {
        LocationDb()
    }

    init {
        fetchData()
    }

    fun onEvent(event: LocationsEvent) {
        when (event) {
            LocationsEvent.LoadingClick -> loadingClick()
            LocationsEvent.RetryClick -> retryClick()
        }
    }

    private fun fetchData() {
        job?.cancel()
        job = viewModelScope.launch {
            delay(4.seconds)
            val locations = db.getAllLocations()
            _state.update {
                it.copy(
                    isLoading = false,
                    locations = locations
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

package com.gt.uvg.rickandmorty.location.ui.list

import com.gt.uvg.rickandmorty.location.data.Location

data class LocationsState(
    val locations: List<Location> = emptyList(),
    val isLoading: Boolean = true,
    val hasError: Boolean = false
)

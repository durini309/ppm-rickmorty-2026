package com.gt.uvg.rickandmorty.location.ui.detail

import com.gt.uvg.rickandmorty.location.data.Location

data class LocationDetailState(
    val location: Location? = null,
    val isLoading: Boolean = true,
    val hasError: Boolean = false
)

package com.gt.uvg.rickandmorty.location.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.navigation
import com.gt.uvg.rickandmorty.core.navigation.TopLevelRoute
import com.gt.uvg.rickandmorty.location.ui.detail.locationDetailScreen
import com.gt.uvg.rickandmorty.location.ui.detail.navigateToLocationDetail
import com.gt.uvg.rickandmorty.location.ui.list.LocationsDestination
import com.gt.uvg.rickandmorty.location.ui.list.locationsScreen
import kotlinx.serialization.Serializable

// Nested graph de Locations: lista -> detalle
@Serializable
data object LocationsGraph : TopLevelRoute

fun NavController.navigateToLocationsGraph(navOptions: NavOptions? = null) {
    navigate(LocationsGraph, navOptions)
}

fun NavGraphBuilder.locationsGraph(navController: NavController) {
    navigation<LocationsGraph>(startDestination = LocationsDestination) {
        locationsScreen(onLocationClick = navController::navigateToLocationDetail)
        locationDetailScreen(onBackClick = navController::navigateUp)
    }
}

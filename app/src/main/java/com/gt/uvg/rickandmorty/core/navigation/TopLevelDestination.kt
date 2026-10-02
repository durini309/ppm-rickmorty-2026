package com.gt.uvg.rickandmorty.core.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.gt.uvg.rickandmorty.R
import com.gt.uvg.rickandmorty.character.ui.navigation.CharactersGraph
import com.gt.uvg.rickandmorty.location.ui.navigation.LocationsGraph
import com.gt.uvg.rickandmorty.profile.ui.ProfileDestination

/**
 * Opciones del BottomNavigation. [route] es el graph (o pantalla) al que navega cada opción.
 */
enum class TopLevelDestination(
    val route: TopLevelRoute,
    @DrawableRes val icon: Int,
    @StringRes val label: Int
) {
    CHARACTERS(
        route = CharactersGraph,
        icon = R.drawable.ic_group,
        label = R.string.characters_title
    ),
    LOCATIONS(
        route = LocationsGraph,
        icon = R.drawable.ic_public,
        label = R.string.locations_title
    ),
    PROFILE(
        route = ProfileDestination,
        icon = R.drawable.ic_person,
        label = R.string.profile_title
    )
}

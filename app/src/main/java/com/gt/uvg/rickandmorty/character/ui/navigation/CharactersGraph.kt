package com.gt.uvg.rickandmorty.character.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.navigation
import com.gt.uvg.rickandmorty.character.ui.detail.characterDetailScreen
import com.gt.uvg.rickandmorty.character.ui.detail.navigateToCharacterDetail
import com.gt.uvg.rickandmorty.character.ui.list.CharactersDestination
import com.gt.uvg.rickandmorty.character.ui.list.charactersScreen
import com.gt.uvg.rickandmorty.core.navigation.TopLevelRoute
import kotlinx.serialization.Serializable

// Nested graph de Characters: lista -> detalle
@Serializable
data object CharactersGraph : TopLevelRoute

fun NavController.navigateToCharactersGraph(navOptions: NavOptions? = null) {
    navigate(CharactersGraph, navOptions)
}

fun NavGraphBuilder.charactersGraph(navController: NavController) {
    navigation<CharactersGraph>(startDestination = CharactersDestination) {
        charactersScreen(onCharacterClick = navController::navigateToCharacterDetail)
        characterDetailScreen(onBackClick = navController::navigateUp)
    }
}

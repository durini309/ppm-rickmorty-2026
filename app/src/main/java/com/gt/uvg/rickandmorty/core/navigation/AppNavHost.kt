package com.gt.uvg.rickandmorty.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.gt.uvg.rickandmorty.character.ui.detail.characterDetailScreen
import com.gt.uvg.rickandmorty.character.ui.detail.navigateToCharacterDetail
import com.gt.uvg.rickandmorty.character.ui.list.charactersScreen
import com.gt.uvg.rickandmorty.character.ui.list.navigateToCharacters
import com.gt.uvg.rickandmorty.login.ui.LoginDestination
import com.gt.uvg.rickandmorty.login.ui.loginScreen

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = LoginDestination,
        modifier = modifier
    ) {
        loginScreen(
            onStartClick = {
                // Sacamos Login del backstack: back en Characters cierra la app
                navController.navigateToCharacters(
                    navOptions {
                        popUpTo(LoginDestination) { inclusive = true }
                    }
                )
            }
        )
        charactersScreen(onCharacterClick = navController::navigateToCharacterDetail)
        characterDetailScreen(onBackClick = navController::navigateUp)
    }
}

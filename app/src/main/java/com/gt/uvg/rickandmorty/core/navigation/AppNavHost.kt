package com.gt.uvg.rickandmorty.core.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.gt.uvg.rickandmorty.character.ui.list.CharactersDestination
import com.gt.uvg.rickandmorty.character.ui.navigation.charactersGraph
import com.gt.uvg.rickandmorty.character.ui.navigation.navigateToCharactersGraph
import com.gt.uvg.rickandmorty.location.ui.navigation.locationsGraph
import com.gt.uvg.rickandmorty.login.ui.LoginDestination
import com.gt.uvg.rickandmorty.login.ui.loginScreen
import com.gt.uvg.rickandmorty.login.ui.navigateToLogin
import com.gt.uvg.rickandmorty.profile.ui.profileScreen
import com.gt.uvg.rickandmorty.splash.ui.SplashDestination
import com.gt.uvg.rickandmorty.splash.ui.splashScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    viewModel: AppNavigationViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    // Buscamos qué opción del BottomNavigation corresponde a la pantalla actual.
    // hierarchy incluye los graphs padre, así "Characters" sigue marcado en el detalle.
    // En Login no hay ninguna (null) y por eso no se muestra el BottomNavigation.
    val selectedDestination = TopLevelDestination.entries.firstOrNull { destination ->
        currentDestination?.hierarchy?.any { it.hasRoute(destination.route::class) } == true
    }

    // La navegación de la sesión se decide solo aquí, según el nombre guardado en DataStore.
    // Login y Profile solo guardan o borran el nombre; cuando cambia, este bloque vuelve a correr.
    LaunchedEffect(state.userName) {
        val currentDestination = navController.currentDestination
        val isInSplash = currentDestination?.hasRoute(SplashDestination::class) == true
        val isInLogin = currentDestination?.hasRoute(LoginDestination::class) == true

        val userName = state.userName
        when {
            // Unit = No hacer nada
            userName == null -> Unit

            userName.isEmpty() -> {
                // No hay sesión: vamos al Login (si no estamos ya ahí)
                if (!isInLogin) {
                    navController.navigateToLogin(
                        navOptions {
                            popUpTo(0) { inclusive = true }
                        }
                    )
                }
            }

            else -> {
                // Hay sesión: si estamos en Splash o Login, vamos a la lista de personajes.
                // Si ya estamos adentro (por ejemplo, al rotar la pantalla en Profile), no hacemos nada.
                if (isInSplash || isInLogin) {
                    navController.navigateToCharactersGraph(
                        navOptions {
                            popUpTo(0) { inclusive = true }
                        }
                    )
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        // Cada pantalla maneja sus propios insets (status bar) con su Scaffold
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (selectedDestination != null) {
                AppBottomBar(
                    selectedDestination = selectedDestination,
                    onDestinationClick = { destination ->
                        navController.navigate(
                            destination.route,
                            navOptions {
                                // Sacamos todo lo que esté encima de la lista de personajes (la primera
                                // pantalla después del login), así back desde otra pestaña regresa ahí.
                                // Ojo: es a la pantalla y no a CharactersGraph; si fuera al graph,
                                // quedaría un graph vacío en el backstack y back cerraría la app.
                                // saveState guarda las pantallas que se sacan para restaurarlas después.
                                popUpTo(CharactersDestination) { saveState = true }
                                // Si ya estoy en esa pestaña, no la agrega otra vez
                                launchSingleTop = true
                                // Al volver a una pestaña, restaura lo que se guardó con saveState
                                restoreState = true
                            }
                        )
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = SplashDestination,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            splashScreen()
            loginScreen()
            charactersGraph(navController)
            locationsGraph(navController)
            profileScreen()
        }
    }
}

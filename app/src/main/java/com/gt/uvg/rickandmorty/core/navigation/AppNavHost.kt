package com.gt.uvg.rickandmorty.core.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.gt.uvg.rickandmorty.character.ui.navigation.CharactersGraph
import com.gt.uvg.rickandmorty.character.ui.navigation.charactersGraph
import com.gt.uvg.rickandmorty.character.ui.navigation.navigateToCharactersGraph
import com.gt.uvg.rickandmorty.location.ui.navigation.locationsGraph
import com.gt.uvg.rickandmorty.login.ui.LoginDestination
import com.gt.uvg.rickandmorty.login.ui.loginScreen
import com.gt.uvg.rickandmorty.login.ui.navigateToLogin
import com.gt.uvg.rickandmorty.profile.ui.profileScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    viewModel: AppNavigationViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Mientras no hemos leído DataStore no dibujamos nada: el splash del sistema sigue encima (ver MainActivity)
    val userName = state.userName ?: return

    // La primera pantalla se decide una sola vez, con lo que había en DataStore al abrir la app.
    // Así no hay que pasar por el Login para luego redirigir a la lista de personajes.
    val startDestination: Any = remember {
        if (userName.isEmpty()) LoginDestination else CharactersGraph
    }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    // Buscamos qué opción del BottomNavigation corresponde a la pantalla actual.
    // hierarchy incluye los graphs padre, así "Characters" sigue marcado en el detalle.
    // En Login no hay ninguna (null) y por eso no se muestra el BottomNavigation.
    val selectedDestination = TopLevelDestination.entries.firstOrNull { destination ->
        currentDestination?.hierarchy?.any { it.hasRoute(destination.route::class) } == true
    }

    // Cuando la sesión cambia (Login guarda el nombre o Profile lo borra), navegamos desde aquí.
    // Revisamos si estamos en el Login para no navegar de más, por ejemplo al rotar la pantalla.
    LaunchedEffect(userName) {
        val isInLogin = navController.currentDestination?.hasRoute(LoginDestination::class) == true

        // popUpTo(0) vacía todo el backstack: back en Login o en Characters cierra la app
        when {
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
                // Hay sesión: si estamos en el Login, vamos a la lista de personajes
                if (isInLogin) {
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
            startDestination = startDestination,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            loginScreen()
            charactersGraph(navController)
            locationsGraph(navController)
            profileScreen()
        }
    }
}

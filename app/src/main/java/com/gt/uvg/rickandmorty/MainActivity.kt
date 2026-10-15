package com.gt.uvg.rickandmorty

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.gt.uvg.rickandmorty.core.navigation.AppNavHost
import com.gt.uvg.rickandmorty.core.navigation.AppNavigationViewModel
import com.gt.uvg.rickandmorty.core.ui.theme.RickAndMortyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Tiene que ir antes de super.onCreate()
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Aunque sea una variable local, al rotar se recibe el mismo ViewModel (lo guarda la Activity)
        val appNavigationViewModel: AppNavigationViewModel by viewModels()

        // El splash del sistema se queda en pantalla mientras no hayamos leído DataStore (userName == null)
        splashScreen.setKeepOnScreenCondition {
            appNavigationViewModel.state.value.userName == null
        }

        enableEdgeToEdge()
        setContent {
            RickAndMortyTheme {
                AppNavHost(viewModel = appNavigationViewModel)
            }
        }
    }
}

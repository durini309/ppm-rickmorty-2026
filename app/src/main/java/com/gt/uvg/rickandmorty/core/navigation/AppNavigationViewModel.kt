package com.gt.uvg.rickandmorty.core.navigation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gt.uvg.rickandmorty.core.data.SessionPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel de la navegación de la app: revisa en DataStore si el usuario ya inició sesión.
 */
class AppNavigationViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionPreferences = SessionPreferences(application)

    // stateIn convierte el Flow de DataStore en un StateFlow.
    // Empieza con AppNavigationState() (userName = null) mientras DataStore responde,
    // y luego cambia a "" (sin sesión) o al nombre guardado.
    val state = sessionPreferences.name
        .map { name ->
            AppNavigationState(userName = name)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppNavigationState()
        )
}

package com.gt.uvg.rickandmorty.profile.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gt.uvg.rickandmorty.core.data.SessionPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()

    private val sessionPreferences = SessionPreferences(application)

    init {
        // Leemos el nombre guardado en DataStore (sigue ahí aunque se cierre la app)
        viewModelScope.launch {
            val name = sessionPreferences.getName()
            _state.update {
                it.copy(name = name)
            }
        }
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.LogoutClick -> logoutClick()
        }
    }

    private fun logoutClick() {
        // Solo borramos el nombre. AppNavigationViewModel se entera del cambio y AppNavHost navega
        viewModelScope.launch {
            sessionPreferences.clearName()
        }
    }
}

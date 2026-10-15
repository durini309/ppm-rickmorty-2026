package com.gt.uvg.rickandmorty.login.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gt.uvg.rickandmorty.core.data.SessionPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    private val sessionPreferences = SessionPreferences(application)

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.NameChange -> nameChange(event.name)
            LoginEvent.LoginClick -> loginClick()
        }
    }

    private fun nameChange(name: String) {
        _state.update {
            it.copy(name = name)
        }
    }

    private fun loginClick() {
        val name = _state.value.name.trim()
        // Un nombre vacío significa "sin sesión", así que no lo dejamos pasar
        if (name.isEmpty()) return

        // Solo guardamos el nombre. AppNavigationViewModel se entera del cambio y AppNavHost navega
        viewModelScope.launch {
            sessionPreferences.saveName(name)
        }
    }
}

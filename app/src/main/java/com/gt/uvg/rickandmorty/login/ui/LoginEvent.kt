package com.gt.uvg.rickandmorty.login.ui

sealed interface LoginEvent {
    data class NameChange(val name: String): LoginEvent
    data object LoginClick: LoginEvent
}

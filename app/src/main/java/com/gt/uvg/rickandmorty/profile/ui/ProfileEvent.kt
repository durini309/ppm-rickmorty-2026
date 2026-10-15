package com.gt.uvg.rickandmorty.profile.ui

sealed interface ProfileEvent {
    data object LogoutClick: ProfileEvent
}

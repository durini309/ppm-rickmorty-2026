package com.gt.uvg.rickandmorty.location.ui.detail

sealed interface LocationDetailEvent {
    data object LoadingClick: LocationDetailEvent
    data object RetryClick: LocationDetailEvent
}

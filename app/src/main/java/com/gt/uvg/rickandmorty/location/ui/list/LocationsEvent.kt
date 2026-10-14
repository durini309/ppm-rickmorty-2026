package com.gt.uvg.rickandmorty.location.ui.list

sealed interface LocationsEvent {
    data object LoadingClick: LocationsEvent
    data object RetryClick: LocationsEvent
}

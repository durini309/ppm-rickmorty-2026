package com.gt.uvg.rickandmorty.character.ui.list

sealed interface CharactersEvent {
    data object LoadingClick: CharactersEvent
    data object RetryClick: CharactersEvent
}

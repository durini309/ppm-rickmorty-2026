package com.gt.uvg.rickandmorty.character.ui.detail

sealed interface CharacterDetailEvent {
    data object LoadingClick: CharacterDetailEvent
    data object RetryClick: CharacterDetailEvent
}
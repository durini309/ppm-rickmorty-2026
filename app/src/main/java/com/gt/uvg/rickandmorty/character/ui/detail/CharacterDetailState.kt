package com.gt.uvg.rickandmorty.character.ui.detail

import com.gt.uvg.rickandmorty.character.data.Character

data class CharacterDetailState(
    val character: Character? = null,
    val isLoading: Boolean = true,
    val hasError: Boolean = false
)
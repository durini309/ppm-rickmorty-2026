package com.gt.uvg.rickandmorty.character.ui.list

import com.gt.uvg.rickandmorty.character.data.Character

data class CharactersState(
    val characters: List<Character> = emptyList(),
    val isLoading: Boolean = true,
    val hasError: Boolean = false
)

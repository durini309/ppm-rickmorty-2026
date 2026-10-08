package com.gt.uvg.rickandmorty.character.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.gt.uvg.rickandmorty.character.data.CharacterDb
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class CharacterDetailViewModel(
    savedStateHandle: SavedStateHandle
): ViewModel() {
    private val _state = MutableStateFlow(CharacterDetailState())
    val state = _state.asStateFlow()

    var job: Job? = null

    private val destination = savedStateHandle.toRoute<CharacterDetailDestination>()
    private val characterId = destination.characterId

    val db by lazy {
        CharacterDb()
    }

    init {
        fetchData()
    }

    fun onEvent(event: CharacterDetailEvent) {
        when (event) {
            CharacterDetailEvent.LoadingClick -> loadingClick()
            CharacterDetailEvent.RetryClick -> retryClick()
        }
    }

    private fun fetchData() {
        job?.cancel()
        job = viewModelScope.launch {
            delay(2.seconds)
            val character = db.getCharacterById(characterId)
            _state.update {
                it.copy(
                    isLoading = false,
                    character = character
                )
            }
        }

    }

    private fun loadingClick() {
        job?.cancel()
        _state.update {
            it.copy(
                isLoading = false,
                hasError = true
            )
        }
    }

    private fun retryClick() {
        job?.cancel()
        _state.update {
            it.copy(
                isLoading = true,
                hasError = false
            )
        }
        fetchData()
    }

}
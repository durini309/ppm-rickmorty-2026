package com.gt.uvg.rickandmorty.character.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gt.uvg.rickandmorty.character.data.CharacterDb
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class CharactersViewModel: ViewModel() {
    private val _state = MutableStateFlow(CharactersState())
    val state = _state.asStateFlow()

    var job: Job? = null

    val db by lazy {
        CharacterDb()
    }

    init {
        fetchData()
    }

    fun onEvent(event: CharactersEvent) {
        when (event) {
            CharactersEvent.LoadingClick -> loadingClick()
            CharactersEvent.RetryClick -> retryClick()
        }
    }

    private fun fetchData() {
        job?.cancel()
        job = viewModelScope.launch {
            delay(4.seconds)
            val characters = db.getAllCharacters()
            _state.update {
                it.copy(
                    isLoading = false,
                    characters = characters
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

package com.ritesh.rickmortywiki.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ritesh.network.models.domain.CharacterPage
import com.ritesh.rickmortywiki.repositories.CharacterRepository
import com.ritesh.rickmortywiki.screens.HomeScreenViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val characterRepository: CharacterRepository
): ViewModel(){
    private val _viewState = MutableStateFlow<HomeScreenViewState>(HomeScreenViewState.Loading)
    val viewState: StateFlow<HomeScreenViewState> = _viewState.asStateFlow()

    private val fetchedCharacterPages = mutableListOf<CharacterPage>()

    fun fetchInitialPage() = viewModelScope.launch{
        if (fetchedCharacterPages.isNotEmpty()) return@launch
        val initialPage = characterRepository.fetchCharacterPage(page = 1)
        initialPage
            .onSuccess { characterPage ->
                fetchedCharacterPages.clear()
                fetchedCharacterPages.add(characterPage)

                _viewState.update{
                    return@update HomeScreenViewState.GridDisplay(characters = characterPage.characters)
                }
            }
            .onFailure {
                //Todo
            }
    }

    fun fetchNextPage() = viewModelScope.launch{
        val nextPageIndex = fetchedCharacterPages.size + 1
        characterRepository.fetchCharacterPage(page = nextPageIndex)
            .onSuccess { characterPage->
                fetchedCharacterPages.add(characterPage)
                _viewState.update { currentState->
                    val currentCharacters = (currentState as? HomeScreenViewState.GridDisplay)?.characters ?: emptyList()
                    return@update HomeScreenViewState.GridDisplay(characters = currentCharacters + characterPage.characters)
                }
            }
            .onFailure {
                // Todo
            }
    }
}
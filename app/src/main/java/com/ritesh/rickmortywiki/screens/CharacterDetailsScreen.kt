package com.ritesh.rickmortywiki.screens


import VerticalOverscroll
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ritesh.network.models.domain.Character
import com.ritesh.rickmortywiki.components.character.CharacterDetailsNamePlateComponent
import com.ritesh.rickmortywiki.components.common.CharacterImage
import com.ritesh.rickmortywiki.components.common.DataPoint
import com.ritesh.rickmortywiki.components.common.DataPointComponent
import com.ritesh.rickmortywiki.components.common.LoadingState
import com.ritesh.rickmortywiki.components.common.SimpleToolbar
import com.ritesh.rickmortywiki.repositories.CharacterRepository
import com.ritesh.rickmortywiki.ui.theme.RickAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class CharacterDetailsViewModel @Inject constructor(
    private val characterRepository: CharacterRepository
): ViewModel() {
    private val _internalStorageFlow = MutableStateFlow<CharacterDetailsViewState>(
        value = CharacterDetailsViewState.Loading
    )
    val stateFlow = _internalStorageFlow.asStateFlow()
    fun fetchCharacter(characterId: Int) = viewModelScope.launch{
        _internalStorageFlow.update { return@update CharacterDetailsViewState.Loading }
        characterRepository.fetchCharacter(characterId)
            .onSuccess { character ->
                val dataPoints = buildList {
                        add(DataPoint("Last known location", character.location.name))
                        add(DataPoint("Species", character.species))
                        add(DataPoint("Gender", character.gender.displayName))
                        character.type.takeIf { it.isNotEmpty() }?.let { type ->
                            add(DataPoint("Type", type))
                        }
                        add(DataPoint("Origin", character.origin.name))
                        add(DataPoint("Episode count", character.episodeIds.size.toString()))
                }
                _internalStorageFlow.update {
                    return@update CharacterDetailsViewState.Success(
                        character = character,
                        characterDataPoints = dataPoints
                    )
                }

            }
            .onFailure {exception ->
            _internalStorageFlow.update {
                return@update CharacterDetailsViewState.Error(
                    message = exception.message ?: "Unknown Error Occurred"
                )
            }
        }
    }

}

sealed interface CharacterDetailsViewState{
    object Loading: CharacterDetailsViewState
    data class Error(val message: String): CharacterDetailsViewState
    data class Success(
        val character: Character,
        val characterDataPoints: List<DataPoint>
    ): CharacterDetailsViewState
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CharacterDetailsScreen (
    characterId: Int,
    viewModel: CharacterDetailsViewModel = hiltViewModel(),
    onEpisodeClicked: (Int) -> Unit,
    onBackClicked: () -> Unit,
) {

    LaunchedEffect(key1 = Unit, block = {
        viewModel.fetchCharacter(characterId)
    })
    val state by viewModel.stateFlow.collectAsState()
    val coroScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val overscrollEffect = remember(coroScope) { VerticalOverscroll(coroScope) }

    val topPadding = remember {
        derivedStateOf {
            // If the list is at the top (scroll offset is 0), set padding to 60.dp.
            // If the user starts scrolling (scroll offset > 0), set padding to 0.dp.
            if (listState.firstVisibleItemScrollOffset == 0) 60.dp else 0.dp
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn (
            state = listState,
            userScrollEnabled = false,
            modifier =
            Modifier.fillMaxSize()
                .padding(top = topPadding.value) // Apply top padding
                .zIndex(0f)
                .overscroll(overscrollEffect)
                .scrollable(
                    orientation = Orientation.Vertical,
                    reverseDirection = true,
                    state = listState,
                    overscrollEffect = overscrollEffect
                )
                ,
            contentPadding = PaddingValues(all = 16.dp)
        ) {
            when (val viewState = state){
                CharacterDetailsViewState.Loading -> item{ LoadingState() }
                is CharacterDetailsViewState.Error -> {
                    //Todo
                }
                is CharacterDetailsViewState.Success -> {

                    // Name Plate
                    item {
                        CharacterDetailsNamePlateComponent(
                            name = viewState.character.name,
                            status = viewState.character.status
                        )
                    }
                    item{ Spacer(modifier = Modifier.height(8.dp)) }

                    //Character Image
                    item{
                        CharacterImage(imageUrl = viewState.character.imageUrl)
                    }

                    //Data Points
                    items(viewState.characterDataPoints){
                        Spacer(modifier = Modifier.height(32.dp))
                        DataPointComponent(dataPoint = it)
                    }

                    item{ Spacer(modifier = Modifier.height(32.dp)) }

                    // Button
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(), // Ensures the Box takes up the full width
                            contentAlignment = Alignment.Center // Centers content within the Box
                        ) {
                            Text(
                                text = "View All Episodes",
                                color = RickAction,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .border(
                                        width = 1.dp,
                                        color = RickAction,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        onEpisodeClicked(characterId)
                                    }
                                    .padding(vertical = 10.dp, horizontal = 25.dp)
                            )
                        }
                    }
                    item{ Spacer(modifier = Modifier.height(64.dp))}
                }
            }
        }


    }


}







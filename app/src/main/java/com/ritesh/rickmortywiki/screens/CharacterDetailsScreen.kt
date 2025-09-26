package com.ritesh.rickmortywiki.screens


import VerticalOverscroll
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButtonDefaults.Icon
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

@Composable
fun CharacterDetailsScreen(
    characterId: Int,
    viewModel: CharacterDetailsViewModel = hiltViewModel(),
    onEpisodeClicked: (Int) -> Unit,
    onBackClicked: () -> Unit,
) {
    LaunchedEffect(key1 = Unit, block = {
        viewModel.fetchCharacter(characterId)
    })

    val state by viewModel.stateFlow.collectAsState()
    val scrollState = rememberLazyListState()

    LazyColumn(
        state = scrollState,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 90.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (val viewState = state) {
            CharacterDetailsViewState.Loading -> item { LoadingState() }
            is CharacterDetailsViewState.Error -> {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = viewState.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
            is CharacterDetailsViewState.Success -> {
                item {
                    CharacterDetailsNamePlateComponent(
                        name = viewState.character.name,
                        status = viewState.character.status
                    )
                }

                item {
                    CharacterImage(imageUrl = viewState.character.imageUrl)
                }

                items(viewState.characterDataPoints) { dataPoint ->
                    DataPointComponent(dataPoint = dataPoint)
                }

                item {
                    // Material 3 Expressive Button
                    Button(
                        onClick = { onEpisodeClicked(characterId) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(20.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 12.dp,
                            hoveredElevation = 8.dp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "View All Episodes",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                item {
                    Spacer(
                        modifier = Modifier.height(74.dp)
                    )
                }
            }
        }
    }
}







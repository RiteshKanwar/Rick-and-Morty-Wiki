package com.ritesh.rickmortywiki.screens

import VerticalOverscroll
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ritesh.network.models.domain.Episode
import com.ritesh.rickmortywiki.components.common.LoadingState
import com.ritesh.rickmortywiki.components.episode.EpisodeRowComponent
import com.ritesh.rickmortywiki.ui.theme.RickAction
import com.ritesh.rickmortywiki.view_models.AllEpisodesViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AllEpisodesScreen(
    episodesViewModel: AllEpisodesViewModel = hiltViewModel()
) {
    val uiState by episodesViewModel.uiState.collectAsState()

    LaunchedEffect(key1 = Unit) {
        episodesViewModel.refreshAllEpisodes()
    }
    when (val state = uiState) {
        AllEpisodesUiState.Error -> {
            //todo
        }
        AllEpisodesUiState.Loading -> LoadingState()
        is AllEpisodesUiState.Success -> {
            val scrollState = rememberLazyListState()
            val coroScope = rememberCoroutineScope()
            val overscrollEffect = remember(coroScope) { VerticalOverscroll(coroScope) }
            val topPadding = remember {
                derivedStateOf {
                    // If the list is at the top (scroll offset is 0), set padding to 60.dp.
                    // If the user starts scrolling (scroll offset > 0), set padding to 0.dp.
                    if (scrollState.firstVisibleItemScrollOffset == 0) 70.dp else 60.dp
                }
            }
            Column {
                LazyColumn(
                    state = scrollState,
                    userScrollEnabled = false,
                    modifier =
                    Modifier.fillMaxWidth()
                        .overscroll(overscrollEffect)
                        .scrollable(
                            orientation = Orientation.Vertical,
                            reverseDirection = true,
                            state = scrollState,
                            overscrollEffect = overscrollEffect
                        ),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    state.data.forEach{ mapEntry ->
                        val uniqueCharacterCount = mapEntry.value.flatMap { it.characterIdsInEpisode }.toSet().size
                        stickyHeader(key = mapEntry.key){
                            Column(modifier = Modifier.fillMaxWidth().background(color = MaterialTheme.colorScheme.background).padding(top = topPadding.value)) {
                                Text(text = mapEntry.key, color = RickAction, fontSize = 32.sp)
                                Text(text = "$uniqueCharacterCount unique characters", color = Color.White, fontSize = 18.sp)
                                Spacer(modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .padding(top = 4.dp)
                                    .background(color = RickAction))
                            }

                        }
                        mapEntry.value.forEach{ episode ->
                            item(key = episode.id){ EpisodeRowComponent(episode = episode) }

                        }

                    }
                }
            }
        }
    }
}

sealed interface AllEpisodesUiState{
    object Error: AllEpisodesUiState
    object Loading: AllEpisodesUiState
    data class Success(val data: Map<String, List<Episode>>): AllEpisodesUiState
}
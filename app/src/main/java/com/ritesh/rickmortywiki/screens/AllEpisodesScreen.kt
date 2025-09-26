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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ritesh.network.models.domain.Episode
import com.ritesh.rickmortywiki.components.common.LoadingState
import com.ritesh.rickmortywiki.components.episode.EpisodeRowComponent
import com.ritesh.rickmortywiki.ui.theme.RickAction
import com.ritesh.rickmortywiki.view_models.AllEpisodesViewModel

// AllEpisodesScreen.kt
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
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Failed to load episodes. Please try again.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        AllEpisodesUiState.Loading -> LoadingState()
        is AllEpisodesUiState.Success -> {
            val scrollState = rememberLazyListState()
            val topPadding = remember {
                derivedStateOf {
                    if (scrollState.firstVisibleItemScrollOffset == 0) 90.dp else 90.dp
                }
            }

            LazyColumn(
                state = scrollState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                state.data.forEach { mapEntry ->
                    val uniqueCharacterCount = mapEntry.value.flatMap { it.characterIdsInEpisode }.toSet().size
                    stickyHeader(key = mapEntry.key) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = topPadding.value),
                            color = MaterialTheme.colorScheme.background,
                            shadowElevation = 0.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 16.dp)
                            ) {
                                Text(
                                    text = mapEntry.key,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$uniqueCharacterCount unique characters",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                HorizontalDivider(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    thickness = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    mapEntry.value.forEach { episode ->
                        item(key = episode.id) {
                            EpisodeRowComponent(episode = episode)
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
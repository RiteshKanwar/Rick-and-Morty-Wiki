package com.ritesh.rickmortywiki.screens

import VerticalOverscroll
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.ritesh.network.KtorClient
import com.ritesh.network.models.domain.Character
import com.ritesh.network.models.domain.Episode
import com.ritesh.rickmortywiki.components.common.CharacterImage
import com.ritesh.rickmortywiki.components.common.CharacterNameComponent
import com.ritesh.rickmortywiki.components.common.DataPoint
import com.ritesh.rickmortywiki.components.common.DataPointComponent
import com.ritesh.rickmortywiki.components.common.LoadingState
import com.ritesh.rickmortywiki.components.episode.EpisodeRowComponent
import kotlinx.coroutines.launch

// CharacterEpisodeScreen.kt
@Composable
fun CharacterEpisodeScreen(
    characterId: Int,
    ktorClient: KtorClient,
    onBackClicked: () -> Unit
) {
    var characterState by remember { mutableStateOf<Character?>(null) }
    var episodeState by remember { mutableStateOf<List<Episode>>(emptyList()) }

    LaunchedEffect(key1 = Unit, block = {
        ktorClient.getCharacter(characterId).onSuccess { character ->
            characterState = character
            launch {
                ktorClient.getEpisodes(character.episodeIds).onSuccess { episode ->
                    episodeState = episode
                }.onFailure {
                    // Handle exception
                }
            }
        }.onFailure {
            // Handle exception
        }
    })

    characterState?.let { character ->
        MainScreen(character = character, episodes = episodeState, onBackClicked)
    } ?: LoadingState()
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MainScreen(character: Character, episodes: List<Episode>, onBackClicked: () -> Unit) {
    val listState = rememberLazyListState()

    val topStickyHeaderPadding = remember {
        derivedStateOf {
            if (listState.firstVisibleItemScrollOffset == 0) 8.dp else 110.dp
        }
    }
    val topToolBarPadding = remember {
        derivedStateOf {
            if (listState.firstVisibleItemScrollOffset == 0) 90.dp else 0.dp
        }
    }
    val episodeBySeasonMap = episodes.groupBy { it.seasonNumber }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = topToolBarPadding.value),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CharacterNameComponent(name = character.name)
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                episodeBySeasonMap.forEach { mapEntry ->
                    val title = "Season ${mapEntry.key}"
                    val description = "${mapEntry.value.size} ep"
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            DataPointComponent(
                                dataPoint = DataPoint(title, description),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            CharacterImage(imageUrl = character.imageUrl)
        }

        episodeBySeasonMap.forEach { mapEntry ->
            stickyHeader {
                SeasonHeader(
                    seasonNumber = mapEntry.key,
                    topPadding = topStickyHeaderPadding.value
                )
            }
            items(mapEntry.value) { episode ->
                EpisodeRowComponent(episode = episode)
            }
        }
        item {
            Spacer(
                modifier = Modifier.height(74.dp)
            )
        }
    }
}

@Composable
private fun SeasonHeader(seasonNumber: Int, topPadding: Dp) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = topPadding, bottom = 16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        shadowElevation = 0.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = "Season $seasonNumber",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(vertical = 8.dp)
        )
    }
}

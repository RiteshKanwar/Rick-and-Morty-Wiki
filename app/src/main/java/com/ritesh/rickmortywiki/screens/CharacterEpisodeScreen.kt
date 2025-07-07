package com.ritesh.rickmortywiki.screens

import VerticalOverscroll
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
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
import androidx.compose.material3.MaterialTheme
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
import com.ritesh.rickmortywiki.components.common.SimpleToolbar
import com.ritesh.rickmortywiki.components.episode.EpisodeRowComponent
import com.ritesh.rickmortywiki.ui.theme.RickTextPrimary
import kotlinx.coroutines.launch

@Composable
fun CharacterEpisodeScreen(characterId: Int, ktorClient: KtorClient, onBackClicked: () -> Unit){
    var characterState by remember { mutableStateOf<Character?>(null) }
    var episodeState by remember { mutableStateOf<List<Episode>>(emptyList()) }

    LaunchedEffect(key1 = Unit, block = {
        ktorClient.getCharacter(characterId).onSuccess { character ->
            characterState = character
            launch{
                ktorClient.getEpisodes(character.episodeIds).onSuccess { episode ->
                    episodeState =  episode
                }.onFailure {
                    //todo handle exception
                }
            }
        }.onFailure {
            //todo handle exception
        }
    })
    characterState?.let { character ->
        MainScreen(character = character, episodes = episodeState, onBackClicked)
    } ?: LoadingState()

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MainScreen(character: Character, episodes: List<Episode>, onBackClicked: () -> Unit) {
    val coroScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val overscrollEffect = remember(coroScope) { VerticalOverscroll(coroScope) }

    val topStickyHeaderPadding = remember {
        derivedStateOf {
            // If the list is at the top (scroll offset is 0), set padding to 60.dp.
            // If the user starts scrolling (scroll offset > 0), set padding to 0.dp.
            if (listState.firstVisibleItemScrollOffset == 0) 8.dp else 70.dp
        }
    }
    val topToolBarPadding = remember {
        derivedStateOf {
            // If the list is at the top (scroll offset is 0), set padding to 60.dp.
            // If the user starts scrolling (scroll offset > 0), set padding to 0.dp.
            if (listState.firstVisibleItemScrollOffset == 0) 60.dp else 0.dp
        }
    }
    val episodeBySeasonMap = episodes.groupBy { it.seasonNumber }
    Box(modifier = Modifier.fillMaxSize()  ) {
        LazyColumn (
            state = listState,
            userScrollEnabled = false,
            modifier =
            Modifier.fillMaxWidth()
                .overscroll(overscrollEffect)
                .scrollable(
                    orientation = Orientation.Vertical,
                    reverseDirection = true,
                    state = listState,
                    overscrollEffect = overscrollEffect
                )
                .padding(top = topToolBarPadding.value) // Apply top padding
                .zIndex(0f),
            contentPadding = PaddingValues(all = 16.dp)
        ) {
            item{ CharacterNameComponent(name = character.name)}
            item{ Spacer(modifier = Modifier.height(8.dp))}
            item{
                LazyRow {
                    episodeBySeasonMap.forEach { mapEntry ->
                        val title = "Season ${mapEntry.key}"
                        val description = "${mapEntry.value.size} ep"
                        item{
                            DataPointComponent(dataPoint = DataPoint(title, description))
                            Spacer(modifier = Modifier.width(32.dp))
                        }
                    }
                }
            }
            item{ Spacer(modifier = Modifier.height(16.dp)) }
            item{ CharacterImage(imageUrl = character.imageUrl)}
            item{ Spacer(modifier = Modifier.height(8.dp)) }

            episodeBySeasonMap.forEach { mapEntry ->
                stickyHeader { SeasonHeader(seasonNumber = mapEntry.key, topStickyHeaderPadding.value)}
                item{ Spacer(modifier = Modifier.height(16.dp)) }
                items(mapEntry.value){ episode ->
                    EpisodeRowComponent(episode = episode)
                    Spacer(modifier = Modifier.height(16.dp))
                }

            }

        }


    }
    Column {


    }
}

@Composable
private fun SeasonHeader(seasonNumber: Int, topPadding : Dp ){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = topPadding, bottom = 16.dp)
    ) {
        Text(
            text = "Season $seasonNumber",
            color = RickTextPrimary,
            fontSize = 32.sp,
            lineHeight = 32.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = RickTextPrimary,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(vertical = 8.dp)
        )
    }
}
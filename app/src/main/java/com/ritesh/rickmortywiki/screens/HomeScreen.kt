package com.ritesh.rickmortywiki.screens

import VerticalOverscroll
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.overscroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ritesh.network.models.domain.Character
import com.ritesh.rickmortywiki.components.character.CharacterGridItem
import com.ritesh.rickmortywiki.components.common.LoadingState
import com.ritesh.rickmortywiki.components.common.SimpleToolbar
import com.ritesh.rickmortywiki.view_models.HomeScreenViewModel

sealed interface HomeScreenViewState{
    object Loading: HomeScreenViewState
    data class GridDisplay(
        val characters: List<Character> = emptyList()
    ): HomeScreenViewState
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onCharacterSelected: (Int) -> Unit,
    viewModel: HomeScreenViewModel = hiltViewModel()
){
    val viewState by viewModel.viewState.collectAsState()

    LaunchedEffect(key1 = viewModel, block = { viewModel.fetchInitialPage() })

    val scrollState = rememberLazyGridState()
    val topPadding = remember {
        derivedStateOf {
            // If the list is at the top (scroll offset is 0), set padding to 60.dp.
            // If the user starts scrolling (scroll offset > 0), set padding to 0.dp.
            if (scrollState.firstVisibleItemScrollOffset == 0) 60.dp else 0.dp
        }
    }
    val fetchNextPage: Boolean by remember {
        derivedStateOf {
            val currentCharacterCount =
                (viewState as? HomeScreenViewState.GridDisplay)?.characters?.size
                ?:return@derivedStateOf false
            val lastDisplayedIndex = scrollState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                ?:return@derivedStateOf false
            return@derivedStateOf  lastDisplayedIndex >= currentCharacterCount - 10

        }
    }

    LaunchedEffect(key1 = fetchNextPage, block = {
        if (fetchNextPage) viewModel.fetchNextPage()
    })
    val coroScope = rememberCoroutineScope()
    val overscrollEffect = remember(coroScope) { VerticalOverscroll(coroScope) }

    when ( val state = viewState){
        HomeScreenViewState.Loading -> LoadingState()
        is HomeScreenViewState.GridDisplay -> {
            Column {

                LazyVerticalGrid(
                    state = scrollState,
                    userScrollEnabled = false,
                    modifier =
                    Modifier.fillMaxWidth().fillMaxHeight().padding(top = topPadding.value)
                        .overscroll(overscrollEffect)
                        .scrollable(
                            orientation = Orientation.Vertical,
                            reverseDirection = true,
                            state = scrollState,
                            overscrollEffect = overscrollEffect
                        ),
                    contentPadding = PaddingValues(all = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    columns = GridCells.Fixed(2),
                    content = {
                        items(
                            items = state.characters,
                            key = { it.id }
                        ) { character ->
                            CharacterGridItem(modifier = Modifier, character = character) {
                                onCharacterSelected(character.id)
                            }
                        }
                    })
            }
        }
    }

}
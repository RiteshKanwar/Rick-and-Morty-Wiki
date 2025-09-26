package com.ritesh.rickmortywiki.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.delete
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ritesh.network.models.domain.CharacterStatus
import com.ritesh.rickmortywiki.components.character.CharacterListItem
import com.ritesh.rickmortywiki.components.common.DataPoint
import com.ritesh.rickmortywiki.ui.theme.RickAction
import com.ritesh.rickmortywiki.ui.theme.RickPrimary
import com.ritesh.rickmortywiki.view_models.SearchViewModel

/// SearchScreen.kt
@Composable
fun SearchScreen(
    onCharacterClicked: (Int) -> Unit,
    searchViewModel: SearchViewModel = hiltViewModel()
) {
    DisposableEffect(key1 = Unit) {
        val job = searchViewModel.observeUserSearch()
        onDispose { job.cancel() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 90.dp)
    ) {
        val screenState by searchViewModel.uiState.collectAsStateWithLifecycle()

        AnimatedVisibility(visible = screenState is SearchViewModel.ScreenState.Searching) {
            LinearProgressIndicator(
                modifier = Modifier
                    .height(4.dp)
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(30.dp)
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = "Search icon",
                tint = MaterialTheme.colorScheme.primary
            )
            BasicTextField(
                state = searchViewModel.searchTextFieldState,
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorator = { innerTextField ->
                    if (searchViewModel.searchTextFieldState.text.isEmpty()) {
                        Text(
                            text = "Search characters...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    innerTextField()
                }
            )
            AnimatedVisibility(visible = searchViewModel.searchTextFieldState.text.isNotBlank()) {
                IconButton(
                    onClick = {
                        searchViewModel.searchTextFieldState.edit { delete(0, length) }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Clear,
                        contentDescription = "Clear search",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        when (val state = screenState) {
            SearchViewModel.ScreenState.Empty -> {
                Text(
                    text = "Search for characters!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    textAlign = TextAlign.Center
                )
            }

            SearchViewModel.ScreenState.Searching -> {}
            is SearchViewModel.ScreenState.Error -> {
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    textAlign = TextAlign.Center
                )

                // Material 3 Expressive Button
                Button(
                    onClick = { searchViewModel.searchTextFieldState.clearText() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(20.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 12.dp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 84.dp)
                ) {
                    Text(
                        text = "Clear search",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            is SearchViewModel.ScreenState.Content -> SearchScreenContent(
                content = state,
                onStatusClicked = searchViewModel::toggleStatus,
                onCharacterClicked = { onCharacterClicked(it) }
            )
        }
    }
}

@Composable
private fun SearchScreenContent(
    content: SearchViewModel.ScreenState.Content,
    onStatusClicked: (CharacterStatus) -> Unit,
    onCharacterClicked: (Int) -> Unit
) {
    Text(
        text = "${content.results.size} results for '${content.userQuery}'",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
    )

    // Material 3 Filter Chips
    LazyRow(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(content.filterState.statuses) { status ->
            val isSelected = content.filterState.selectedStatuses.contains(status)
            val count = content.results.filter { it.status == status }.size

            FilterChip(
                onClick = { onStatusClicked(status) },
                label = {
                    Text(
                        text = "${status.displayName} ($count)",
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                selected = isSelected,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(16.dp)
            )
        }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        val filteredResults = content.results.filter { character ->
            content.filterState.selectedStatuses.contains(character.status)
        }
        items(
            items = filteredResults,
            key = { character -> character.id }
        ) { character ->
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
            CharacterListItem(
                character = character,
                characterDataPoints = dataPoints,
                onClick = { onCharacterClicked(character.id) },
                modifier = Modifier.animateItem()
            )
        }
    }
}

@Composable
private fun StatusFilterRow(
    content: SearchViewModel.ScreenState.Content,
    onStatusClicked: (CharacterStatus) -> Unit
) {
    Row(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        content.filterState.statuses.forEach { status ->
            val isSelected = content.filterState.selectedStatuses.contains(status)
            val contentColor = if (isSelected) RickAction else Color.LightGray
            val count = content.results.filter { it.status == status }.size
            Row(
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = contentColor,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable {
                        onStatusClicked(status)
                    }
                    .clip(RoundedCornerShape(8.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = count.toString(),
                    color = RickPrimary,
                    modifier = Modifier
                        .background(color = contentColor)
                        .padding(4.dp),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = status.displayName,
                    color = contentColor,
                    modifier = Modifier.padding(horizontal = 6.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
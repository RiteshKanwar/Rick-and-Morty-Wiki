package com.ritesh.rickmortywiki.components.episode

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ritesh.network.models.domain.Episode
import com.ritesh.rickmortywiki.components.common.DataPoint
import com.ritesh.rickmortywiki.components.common.DataPointComponent
import com.ritesh.rickmortywiki.ui.theme.RickTextPrimary

@Composable
fun EpisodeRowComponent(episode: Episode) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = episode.episodeNumber.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = episode.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = episode.airDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}

@Preview
@Composable
private fun EpisodeRowComponentPreview() {
    val episode = Episode(
        id = 28,
        name = "The Ricklantis Mixup",
        seasonNumber = 3,
        episodeNumber = 7,
        airDate = "September 10, 2017",
        characterIdsInEpisode = listOf(
            1,
            2,
            4,
            8,
            18,
            22,
            27,
            43,
            44,
            48,
            56,
            61,
            72,
            73,
            74,
            78,
            85,
            86,
            118,
            123,
            135,
            143,
            165,
            180,
            187,
            206,
            220,
            229,
            233,
            235,
            267,
            278,
            281,
            283,
            284,
            287,
            288,
            289,
            291,
            292,
            322,
            325,
            328,
            345,
            366,
            367,
            392,
            472,
            473,
            474,
            475,
            476,
            477,
            478,
            479,
            480,
            481,
            482,
            483,
            484,
            485,
            486,
            487,
            488,
            489
        )
    )

    EpisodeRowComponent(episode = episode)
}
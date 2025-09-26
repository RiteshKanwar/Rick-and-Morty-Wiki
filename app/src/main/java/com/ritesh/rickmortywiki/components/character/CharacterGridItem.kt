package com.ritesh.rickmortywiki.components.character

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ritesh.network.models.domain.Character
import com.ritesh.network.models.domain.CharacterGender
import com.ritesh.network.models.domain.CharacterStatus
import com.ritesh.rickmortywiki.components.common.CharacterImage
import com.ritesh.rickmortywiki.ui.theme.RickAction


@Composable
fun CharacterGridItem(
    modifier: Modifier,
    character: Character,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column {
            Box {
                CharacterImage(imageUrl = character.imageUrl)
                CharacterStatusCircle(
                    status = character.status,
                    modifier = Modifier.padding(8.dp)
                )
            }
            Text(
                text = character.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(12.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
@Preview
@Composable
private fun CharacterGridItemPreview() {
    CharacterGridItem(
        modifier = Modifier.fillMaxWidth(),
        character = Character(
            created = "timestamp",
            episodeIds = listOf(1, 2, 3, 4, 5),
            gender = CharacterGender.Male,
            id = 123,
            imageUrl = "https://rickandmortyapi.com/api/character/avatar/2.jpeg",
            location = Character.Location(
                name = "Earth",
                url = ""
            ),
            name = "Morty Smith",
            origin = Character.Origin(
                name = "Earth",
                url = ""
            ),
            species = "Human",
            status = CharacterStatus.Alive,
            type = ""
        ),
        onClick = {}
    )
}

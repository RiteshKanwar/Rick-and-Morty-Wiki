package com.ritesh.rickmortywiki.components.character

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ritesh.network.models.domain.CharacterStatus
import com.ritesh.rickmortywiki.ui.theme.RickMortyWikiTheme
import com.ritesh.rickmortywiki.ui.theme.RickTextPrimary

@Composable
fun CharacterStatusComponent(characterStatus: CharacterStatus){
    Row (
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(
                color = characterStatus.color,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(text = "Status: ${characterStatus.displayName}",
            fontSize = 20.sp,
            color = RickTextPrimary
        )
    }
}

@Preview(showBackground = false)
@Composable
fun CharacterStatusComponentPreviewAlive() {
    RickMortyWikiTheme() {
        CharacterStatusComponent(characterStatus = CharacterStatus.Alive)
    }
}


@Preview
@Composable
fun CharacterStatusComponentPreviewDead() {
    RickMortyWikiTheme {
        CharacterStatusComponent(characterStatus = CharacterStatus.Dead)
    }
}

@Preview
@Composable
fun CharacterStatusComponentPreviewUnknown() {
    RickMortyWikiTheme {
        CharacterStatusComponent(characterStatus = CharacterStatus.Unknown)
    }
}

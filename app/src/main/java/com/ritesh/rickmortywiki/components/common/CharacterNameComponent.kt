package com.ritesh.rickmortywiki.components.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.ritesh.rickmortywiki.ui.theme.RickAction

@Composable
fun CharacterNameComponent(name: String) {
    Text(
        text = name,
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Preview
@Composable
private fun CharacterNameComponentPreview() {
    CharacterNameComponent(name = "Rick Sanchez")
}
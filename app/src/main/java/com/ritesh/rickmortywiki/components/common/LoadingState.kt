package com.ritesh.rickmortywiki.components.common

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ritesh.rickmortywiki.ui.theme.RickAction

@Composable
fun LoadingState(){
    CircularProgressIndicator(
        modifier = Modifier.fillMaxSize().padding(all = 100.dp),
        color = RickAction
    )
}
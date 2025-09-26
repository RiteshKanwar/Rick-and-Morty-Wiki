package com.ritesh.rickmortywiki.components.common

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.ritesh.rickmortywiki.ui.theme.RickAction
import com.ritesh.rickmortywiki.ui.theme.RickTextPrimary

data class DataPoint(
    val title: String,
    val description: String,
)

@Composable
fun DataPointComponent(dataPoint: DataPoint, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = dataPoint.title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = dataPoint.description,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}


@Preview
@Composable
fun DataPointComponentPreview(){
    val data = DataPoint(title = "Last known location", description = "Citadel of Ricks")
    DataPointComponent(dataPoint = data)
}
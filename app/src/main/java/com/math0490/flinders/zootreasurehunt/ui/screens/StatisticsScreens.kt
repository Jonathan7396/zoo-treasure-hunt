package com.math0490.flinders.zootreasurehunt.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.math0490.flinders.zootreasurehunt.R
import com.math0490.flinders.zootreasurehunt.model.Sighting
import com.math0490.flinders.zootreasurehunt.ui.components.StepCounterCard

// Displays summary statistics of the sightings including total, found, remaining animals and completion percentage
@Composable
fun StatisticsScreen(sightings: List<Sighting>) {
    val total = sightings.size
    val found = sightings.count { it.isFound }
    val remaining = total - found
    val percentage = if (total > 0) (found * 100) / total else 0
    val progress = if (total > 0) found.toFloat() / total.toFloat() else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = stringResource(R.string.statistics),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Treasure Hunt Progress",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(stringResource(R.string.total_animals, total))
        Text(stringResource(R.string.found_animals, found))
        Text(stringResource(R.string.remaining_animals, remaining))
        Text(stringResource(R.string.completion_percentage, percentage))

        Spacer(modifier = Modifier.height(24.dp))

        StepCounterCard()
    }
}
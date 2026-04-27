package com.math0490.flinders.zootreasurehunt.ui.screens
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.math0490.flinders.zootreasurehunt.R
import com.math0490.flinders.zootreasurehunt.model.Sighting

//Displays summary statistics of the sightings which includes total,found, remaining animals and the completion percentage
@Composable
fun StatisticsScreen(sightings: List<Sighting>) {
    //Calculates key statistics for the list of sightings
    val total = sightings.size
    val found = sightings.count { it.isFound }
    val remaining = total - found
    val percentage = if (total > 0) (found * 100) / total else 0

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = stringResource(R.string.statistics),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(stringResource(R.string.total_animals, total))
        Text(stringResource(R.string.found_animals, found))
        Text(stringResource(R.string.remaining_animals, remaining))
        Text(stringResource(R.string.completion_percentage, percentage))
    }
}
package com.example.zootreasurehunt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zootreasurehunt.ui.theme.ZooTreasureHuntTheme

data class Sighting(
    val name: String,
    var isFound: Boolean = false,
    var notes: String = ""
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZooTreasureHuntTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SightingListScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun SightingListScreen(modifier: Modifier = Modifier) {
    val sightings = listOf(
        Sighting("Lion", true, "Running behind the Zebra"),
        Sighting("Elephant", false, "Watching the Lion chase the Zebra"),
        Sighting("Zebra", true, "Running away from the lion"),
        Sighting("Giraffe", false, "Eating the leaves of a tree"),
        Sighting("Monkey", true, "Jumping from the tree the giraffe is eating to another tree"),
        Sighting("Penguin", false, "Going in the opposite direction")
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(sightings) { sighting ->
            AnimalCard(
                sighting = sighting,
                onClick = {
                    println("${sighting.name} clicked")
                }
            )
        }
    }
}

@Composable
fun AnimalCard(sighting: Sighting, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = sighting.name)

            if (sighting.isFound) {
                Text(text = stringResource(R.string.found_label))
            }

            if (sighting.notes.isNotBlank()) {
                Text(text = sighting.notes)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AnimalCardPreview() {
    ZooTreasureHuntTheme {
        AnimalCard(
            sighting = Sighting(
                name = "Lion",
                isFound = true,
                notes = "Sleeping near the rocks"
            ),
            onClick = {}
        )
    }
}
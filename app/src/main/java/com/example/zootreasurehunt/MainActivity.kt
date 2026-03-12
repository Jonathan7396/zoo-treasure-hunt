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
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

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
        Sighting("Penguin", false, "Going in the opposite direction"),
        Sighting(name = "Hipo", isFound = true, notes = "Coming out of water"),
        Sighting(name = "Rhino", isFound = false,"Enjoying the sun")
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
    val cardColor = if (sighting.isFound) Color(0xFFE8F5E9) else Color(0xFFF5F5F5)
    val textColor = if (sighting.isFound) Color(0xFF2E7D32) else Color.Black
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            Column(modifier = Modifier.weight(1F)) {
                Text(
                    text = sighting.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                if (sighting.isFound && sighting.notes.isNotEmpty()) {
                    Text(
                        text = sighting.notes,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }


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
                notes = "Running behind the Zebra"
            ),
            onClick = {}
        )
    }
}
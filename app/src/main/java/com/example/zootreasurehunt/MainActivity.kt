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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Button
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                ZooApp()
            }
        }
    }
}

@Composable
fun ZooApp() {
    var sightings by rememberSaveable {
        mutableStateOf(
            listOf(
                Sighting("Lion"),
                Sighting("Red Panda"),
                Sighting("Giraffe"),
                Sighting("Kangaroo"),
                Sighting("Penguin")
            )
        )
    }

    var selectedSighting by remember { mutableStateOf<Sighting?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.app_name),
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            sightings.forEach { animal ->
                AnimalCard(sighting = animal) {
                    selectedSighting = animal
                    showDialog = true
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (showDialog) {
            selectedSighting?.let { sighting ->
                EditSightingDialog(
                    sighting = sighting,
                    onDismiss = { showDialog = false },
                    onSave = { updatedSighting ->
                        sightings = sightings.map {
                            if (it.name == updatedSighting.name) updatedSighting else it
                        }
                        showDialog = false
                    }
                )
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

            if (sighting.isFound) {
                Text(
                    text = stringResource(R.string.found_label),
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }
    }
}
@Composable
fun EditSightingDialog(
    sighting: Sighting,
    onDismiss: () -> Unit,
    onSave: (Sighting) -> Unit
) {
    var notesText by remember { mutableStateOf(sighting.notes) }
    var isFoundChecked by remember { mutableStateOf(sighting.isFound) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.edit_animal)) },
        text = {
            Column {
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text(stringResource(id = R.string.notes_hint)) }
                )
                Row(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isFoundChecked,
                        onCheckedChange = { isFoundChecked = it }
                    )
                    Text(text = stringResource(id = R.string.checkbox_found))
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(sighting.copy(isFound = isFoundChecked, notes = notesText))
            }) {
                Text(text = stringResource(id = R.string.save_btn))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.cancel_btn))
            }
        }
    )
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

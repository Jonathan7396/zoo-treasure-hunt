package com.example.zootreasurehunt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import com.example.zootreasurehunt.ui.theme.ZooTreasureHuntTheme
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import com.example.zootreasurehunt.data.SightingRepository
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

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
    val navController = rememberNavController()
    val context = LocalContext.current
    val repository = remember { SightingRepository(context) }
    val scope = rememberCoroutineScope()

    var sightings by remember { mutableStateOf(emptyList<Sighting>()) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            sightings = repository.loadSightings()
        }
    }
    fun saveData(newList: List<Sighting>) {
        sightings = newList
        scope.launch(Dispatchers.IO) {
            repository.saveSightings(newList)
        }
    }

    var selectedSighting by remember { mutableStateOf<Sighting?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    val bottomItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.About
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomItems.forEach { item ->
                    val isSelected = currentDestination?.hasRoute(item.route::class) == true

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<HomeDestination> {
                ListScreen(
                    sightings = sightings,
                    onEditClick = { animal ->
                        selectedSighting = animal
                        showDialog = true
                    },
                    onDelete = { animal ->
                        val newList = sightings.filter { it.id != animal.id }
                        saveData(newList)
                    }
                )
            }

            composable<AboutDestination> {
                AboutScreen()
            }
        }

        if (showDialog) {
            selectedSighting?.let { sighting ->
                EditSightingDialog(
                    sighting = sighting,
                    onDismiss = { showDialog = false },
                    onSave = { updated ->
                        val newList = sightings.map { if (it.id == updated.id) updated else it }
                        saveData(newList)
                        showDialog = false
                    }
                )
            }
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
        ) {
            AsyncImage(
                model = sighting.imageUrl,
                contentDescription = sighting.name,
                modifier = Modifier
                    .size(64.dp)
                    .padding(end = 8.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
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

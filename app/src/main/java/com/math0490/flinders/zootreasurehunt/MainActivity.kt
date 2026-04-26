package com.math0490.flinders.zootreasurehunt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem

import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Modifier

import androidx.compose.ui.tooling.preview.Preview

import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

import com.math0490.flinders.zootreasurehunt.ui.theme.ZooTreasureHuntTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import com.math0490.flinders.zootreasurehunt.viewmodel.ZooViewModel

import com.math0490.flinders.zootreasurehunt.model.Sighting
import com.math0490.flinders.zootreasurehunt.navigation.AboutDestination
import com.math0490.flinders.zootreasurehunt.navigation.BottomNavItem
import com.math0490.flinders.zootreasurehunt.navigation.HomeDestination
import com.math0490.flinders.zootreasurehunt.navigation.SettingsDestination
import com.math0490.flinders.zootreasurehunt.ui.screens.SettingsScreen

import com.math0490.flinders.zootreasurehunt.ui.components.EditSightingDialog
import com.math0490.flinders.zootreasurehunt.ui.screens.ListScreen
import com.math0490.flinders.zootreasurehunt.ui.screens.AboutScreen
import com.math0490.flinders.zootreasurehunt.ui.components.AnimalCard

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
    val viewModel: ZooViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
//    val sightings by viewModel.sightings.collectAsState()
//    val isSortByName by viewModel.isSortByName.collectAsState(initial = true)

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { }
    )



    LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

    }


//    var selectedSighting by remember { mutableStateOf<Sighting?>(null) }
//    var showDialog by remember { mutableStateOf(false) }

    val bottomItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Settings,
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
                    sightings = uiState.sightings,
                    onEditClick = { animal ->
                        viewModel.selectSightingForEdit(animal)
                    },
                    onDelete = { animal ->
                        viewModel.deleteSighting(animal)
                    }
                )
            }
            composable<SettingsDestination> {
                SettingsScreen(
                    isSortByName = uiState.isSortByName,
                    onSortChange = { viewModel.toggleSortOrder(it) }
                )
            }

            composable<AboutDestination> {
                AboutScreen()
            }
        }

        if (uiState.isDialogVisible) {
            uiState.selectedSighting?.let { sighting ->
                EditSightingDialog(
                    sighting = sighting,
                    onDismiss = { viewModel.dismissDialog() },
                    onSave = { updated ->
                        viewModel.updateSighting(updated)
                        viewModel.dismissDialog()
                    }
                )
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

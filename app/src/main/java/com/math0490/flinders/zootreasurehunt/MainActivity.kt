package com.math0490.flinders.zootreasurehunt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.math0490.flinders.zootreasurehunt.model.Sighting
import com.math0490.flinders.zootreasurehunt.navigation.AboutDestination
import com.math0490.flinders.zootreasurehunt.navigation.BottomNavItem
import com.math0490.flinders.zootreasurehunt.navigation.HomeDestination
import com.math0490.flinders.zootreasurehunt.navigation.SettingsDestination
import com.math0490.flinders.zootreasurehunt.navigation.StatisticsDestination
import com.math0490.flinders.zootreasurehunt.ui.components.AnimalCard
import com.math0490.flinders.zootreasurehunt.ui.components.EditSightingDialog
import com.math0490.flinders.zootreasurehunt.ui.screens.AboutScreen
import com.math0490.flinders.zootreasurehunt.ui.screens.ListScreen
import com.math0490.flinders.zootreasurehunt.ui.screens.SettingsScreen
import com.math0490.flinders.zootreasurehunt.ui.screens.StatisticsScreen
import com.math0490.flinders.zootreasurehunt.ui.theme.ZooTreasureHuntTheme
import com.math0490.flinders.zootreasurehunt.viewmodel.ZooViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

// Defines the navigation routes for the app
sealed class Screen(val route: String, @StringRes val titleRes: Int) {
    object Home : Screen("home", R.string.home)
    object Statistics : Screen("statistics", R.string.statistics)
    object Settings : Screen("settings", R.string.settings)
    object About : Screen("about", R.string.about)
}

// Main class that initializes the UI
@AndroidEntryPoint
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

// Composable that manages navigation, drawer state, and UI State
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZooApp() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val viewModel: ZooViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { }
    )

    LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val bottomItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Settings,
        BottomNavItem.About
    )

    val drawerItems = listOf(
        Screen.Home,
        Screen.Statistics,
        Screen.Settings,
        Screen.About
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Provides a secondary navigation through a side drawer
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = stringResource(R.string.app_name),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge
                )

                drawerItems.forEach { screen ->
                    NavigationDrawerItem(
                        label = {
                            Text(stringResource(screen.titleRes))
                        },
                        selected = false,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                            }

                            when (screen) {
                                Screen.Home -> navController.navigate(HomeDestination)
                                Screen.Statistics -> navController.navigate(StatisticsDestination)
                                Screen.Settings -> navController.navigate(SettingsDestination)
                                Screen.About -> navController.navigate(AboutDestination)
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    drawerState.open()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu"
                            )
                        }
                    }
                )
            },
            bottomBar = {
                // Provides a primary navigation bar with bottom items
                NavigationBar {
                    bottomItems.forEach { item ->
                        val isSelected = currentDestination?.hasRoute(item.route::class) == true

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                when (item) {
                                    BottomNavItem.Home -> navController.navigate(HomeDestination)
                                    BottomNavItem.Settings -> navController.navigate(SettingsDestination)
                                    BottomNavItem.About -> navController.navigate(AboutDestination)
                                }
                            },
                            icon = {
                                Icon(
                                    item.icon,
                                    contentDescription = stringResource(item.labelRes)
                                )
                            },
                            label = {
                                Text(stringResource(item.labelRes))
                            }
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
                        },
                        onAddClick = {
                            viewModel.selectSightingForEdit(
                                Sighting(name = "")
                            )
                        }
                    )
                }

                composable<StatisticsDestination> {
                    StatisticsScreen(
                        sightings = uiState.sightings
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
                        isNew = uiState.sightings.none { it.id == sighting.id },
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
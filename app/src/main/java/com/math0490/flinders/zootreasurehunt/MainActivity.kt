package com.math0490.flinders.zootreasurehunt

import android.app.Application
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.math0490.flinders.zootreasurehunt.data.RoomSightingRepository
import com.math0490.flinders.zootreasurehunt.data.SettingsRepository
import com.math0490.flinders.zootreasurehunt.data.ZooDatabase
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
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api

sealed class Screen(val route: String, @StringRes val titleRes: Int) {
    object Home : Screen("home", R.string.home)
    object Statistics : Screen("statistics", R.string.statistics)
    object Settings : Screen("settings", R.string.settings)
    object About : Screen("about", R.string.about)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = ZooDatabase.getDatabase(this)
        val repository = RoomSightingRepository(database.sightingDao())
        val settingsRepository = SettingsRepository(this)

        setContent {
            MaterialTheme {
                ZooApp(
                    repository = repository,
                    settingsRepository = settingsRepository
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZooApp(
    repository: RoomSightingRepository,
    settingsRepository: SettingsRepository
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val viewModel: ZooViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ZooViewModel(
                    repository = repository,
                    settingsRepository = settingsRepository,
                    application = context.applicationContext as Application
                ) as T
            }
        }
    )

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
package com.math0490.flinders.zootreasurehunt.navigation

import kotlinx.serialization.Serializable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.math0490.flinders.zootreasurehunt.R


//Defines Navigation routes and bottom navigation items for the app
@Serializable
object HomeDestination
@Serializable
object StatisticsDestination
@Serializable
object SettingsDestination{
}
@Serializable
object AboutDestination


sealed class BottomNavItem(
    val labelRes: Int,
    val icon: ImageVector,
    val route: Any
) {
    data object Home : BottomNavItem(
        R.string.home,
        Icons.Default.Home,
        HomeDestination
    )


    data object Settings: BottomNavItem(
        R.string.settings,
        Icons.Filled.Settings,
        SettingsDestination
    )

    data object About : BottomNavItem(
        R.string.about,
        Icons.Default.Info,
        AboutDestination
    )

}
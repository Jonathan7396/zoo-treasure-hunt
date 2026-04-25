package com.math0490.flinders.zootreasurehunt

import kotlinx.serialization.Serializable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

@Serializable
object HomeDestination
@Serializable
object SettingsDestination{

}
@Serializable
object AboutDestination


sealed class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: Any
) {
    data object Home : BottomNavItem(
        "Home",
        Icons.Default.Home,
        HomeDestination
    )
    data object Settings: BottomNavItem(
        "Settings",
        Icons.Filled.Settings,
        SettingsDestination
    )
    data object About : BottomNavItem(
        "About",
        Icons.Default.Info,
        AboutDestination
    )


}
package com.math0490.flinders.zootreasurehunt.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(
    isSortByName: Boolean,
    onSortChange: (Boolean) -> Unit
) {
    Column {

        Text("Settings", fontSize = 28.sp)
        Text("Sort Order", modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))

        Row {
            RadioButton(
                selected = isSortByName,
                onClick = { onSortChange(true) }
            )
            Text("Sort by name")
        }

        Row {
            RadioButton(
                selected = !isSortByName,
                onClick = { onSortChange(false) }
            )
            Text("Sort by Recency/Found Status")
        }
    }
}

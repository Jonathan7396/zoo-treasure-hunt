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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.math0490.flinders.zootreasurehunt.R

//Displays the sorting screen allowing the user to change the sorting order of the sighting preferences
@Composable
fun SettingsScreen(
    isSortByName: Boolean,
    onSortChange: (Boolean) -> Unit
) {
    Column (modifier = Modifier.padding(16.dp)){

        Text(stringResource(R.string.settings_title), fontSize = 28.sp)
        Text(stringResource(R.string.sort_order), fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))

        Row {
            RadioButton(
                selected = isSortByName,
                onClick = { onSortChange(true) }
            )
            Text(stringResource(R.string.sort_by_name))
        }

        Row {
            RadioButton(
                selected = !isSortByName,
                onClick = { onSortChange(false) }
            )
            Text(stringResource(R.string.sort_by_found))
        }
    }
}

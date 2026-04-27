package com.math0490.flinders.zootreasurehunt.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.math0490.flinders.zootreasurehunt.R
import androidx.compose.ui.res.stringResource

//Displays the apps name along with the author
@Composable
fun AboutScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.about_text),
            fontSize = 24.sp,
            textAlign = TextAlign.Center
        )
    }
}
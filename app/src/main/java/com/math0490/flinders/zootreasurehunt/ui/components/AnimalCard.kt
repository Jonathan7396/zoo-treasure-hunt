package com.math0490.flinders.zootreasurehunt.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.math0490.flinders.zootreasurehunt.R
import com.math0490.flinders.zootreasurehunt.model.Sighting
import java.text.DateFormat
import java.util.Date
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.scale

// Helper function to get the correct string resource ID for the animal name
fun getAnimalNameRes(name: String): Int? {
    return when (name) {
        "Lion" -> R.string.animal_lion
        "Red Panda" -> R.string.animal_red_panda
        "Kangaroo" -> R.string.animal_kangaroo
        "Giraffe" -> R.string.animal_giraffe
        "Penguin" -> R.string.animal_penguin
        else -> null
    }
}
// Displays a single sighting card with image, details and a visual feedback along with a found label when an animal is marked as found
@Composable
fun AnimalCard(sighting: Sighting, onClick: () -> Unit) {
   // Adds a colour when animal is marked as found
    val cardColor by animateColorAsState(
        targetValue = if (sighting.isFound) Color(0xFFE8F5E9) else Color(0xFFF5F5F5),
        label = "cardColorAnimation"
    )
    // Adds a scale animation when the animal is marked as found
    val cardScale by animateFloatAsState(
        targetValue = if (sighting.isFound) 1.02f else 1f,
        label = "cardScaleAnimation"
    )
    val textColor = if (sighting.isFound) Color(0xFF2E7D32) else Color.Black
    val imageModel = sighting.photoPath ?: "https://wilk0077.github.io/comp2012-images/assets-sm/african-lion-ai.jpg"
    val formattedTime = DateFormat.getDateTimeInstance().format(Date(sighting.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(cardScale)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = imageModel,
                contentDescription = getAnimalNameRes(sighting.name)?.let {
                    stringResource(it)
                } ?: sighting.name,
                modifier = Modifier
                    .size(64.dp)
                    .padding(end = 8.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = getAnimalNameRes(sighting.name)?.let {
                        stringResource(it)
                    } ?: sighting.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                if (sighting.notes.isNotEmpty()) {
                    Text(
                        text = sighting.notes,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                Text(
                    text = formattedTime,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            AnimatedVisibility(
                visible = sighting.isFound,
                enter = fadeIn() + scaleIn()
            ) {
                Text(
                    text = stringResource(R.string.found_label),
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }
    }
}

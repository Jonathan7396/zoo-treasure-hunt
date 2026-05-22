package com.math0490.flinders.zootreasurehunt.ui.components

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.math0490.flinders.zootreasurehunt.R
import com.math0490.flinders.zootreasurehunt.model.Sighting
import com.math0490.flinders.zootreasurehunt.utils.FileUtils
import com.math0490.flinders.zootreasurehunt.utils.LocationUtils
import kotlinx.coroutines.launch

// Displays a dialog that allows the users to add or edit a sighting.
@Composable
fun EditSightingDialog(
    sighting: Sighting,
    isNew: Boolean,
    onDismiss: () -> Unit,
    onSave: (Sighting) -> Unit
) {
    var nameText by remember { mutableStateOf(sighting.name) }
    var latText by remember { mutableStateOf(if (isNew) "" else sighting.latitude.toString()) }
    var lngText by remember { mutableStateOf(if (isNew) "" else sighting.longitude.toString()) }
    var notesText by remember { mutableStateOf(sighting.notes) }
    var isFoundChecked by remember { mutableStateOf(sighting.isFound) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fileUtils = remember { FileUtils(context) }

    var currentPhotoPath by remember { mutableStateOf(sighting.photoPath) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    // Launches the camera and updates the photo path when an image is captured.
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            currentPhotoPath = tempPhotoUri.toString()
            isFoundChecked = true
            feedbackMessage = "Photo captured. Sighting marked as found."
        }
    }

    fun launchCamera() {
        val file = fileUtils.createImageFile()
        val uri = fileUtils.getUriForFile(file)
        tempPhotoUri = uri
        cameraLauncher.launch(uri)
    }

    suspend fun checkLocationAndLaunchCamera() {
        val currentLocation = LocationUtils.getCurrentLocation(context)

        if (currentLocation == null) {
            feedbackMessage = "Unable to get your current location. Please check location settings and try again."
            return
        }

        if (isNew) {
            // For new animals, we use the current location as the enclosure location
            latText = currentLocation.latitude.toString()
            lngText = currentLocation.longitude.toString()
            feedbackMessage = "Enclosure location recorded at your current position."
            launchCamera()
            return
        }

        val distance = LocationUtils.distanceBetweenMeters(
            userLatitude = currentLocation.latitude,
            userLongitude = currentLocation.longitude,
            animalLatitude = sighting.latitude,
            animalLongitude = sighting.longitude
        )

        if (distance <= 50f) {
            feedbackMessage = "Location verified. You can capture the animal photo."
            launchCamera()
        } else {
            feedbackMessage = "You are too far from the ${sighting.name} enclosure! Distance: ${distance.toInt()} m."
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            scope.launch {
                checkLocationAndLaunchCamera()
            }
        } else {
            feedbackMessage = "Location permission is required to verify the animal sighting."
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(id = if (isNew) R.string.add_sighting else R.string.edit_animal))
        },
        text = {
            Column {
                if (isNew) {
                    OutlinedTextField(
                        value = nameText,
                        onValueChange = { nameText = it },
                        label = { Text(stringResource(id = R.string.animal_name_hint)) },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = latText,
                        onValueChange = { latText = it },
                        label = { Text(stringResource(id = R.string.latitude_hint)) },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = lngText,
                        onValueChange = { lngText = it },
                        label = { Text(stringResource(id = R.string.longitude_hint)) },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                } else {
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        label = { Text(stringResource(id = R.string.notes_hint)) },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = if (isFoundChecked) {
                            "Status: Found"
                        } else {
                            "Status: Not found"
                        },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Button(
                    onClick = {
                        if (LocationUtils.hasLocationPermission(context)) {
                            scope.launch {
                                checkLocationAndLaunchCamera()
                            }
                        } else {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    }
                ) {
                    Text(
                        text = if (currentPhotoPath == null)
                            stringResource(id = R.string.take_photo)
                        else
                            stringResource(id = R.string.retake_photo)
                    )
                }

                feedbackMessage?.let {
                    Text(
                        text = it,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (nameText.isNotBlank()) {
                    onSave(
                        sighting.copy(
                            name = nameText,
                            latitude = latText.toDoubleOrNull() ?: 0.0,
                            longitude = lngText.toDoubleOrNull() ?: 0.0,
                            isFound = isFoundChecked,
                            notes = notesText,
                            photoPath = currentPhotoPath
                        )
                    )
                }
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
package com.math0490.flinders.zootreasurehunt.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.math0490.flinders.zootreasurehunt.R
import com.math0490.flinders.zootreasurehunt.model.Sighting
import com.math0490.flinders.zootreasurehunt.utils.FileUtils

// Displays a dialog that allows the users to add or edit a sighting.
// When adding, only the name is visible. When editing, the name is hidden and only notes/found/photo are shown.
@Composable
fun EditSightingDialog(
    sighting: Sighting,
    isNew: Boolean,
    onDismiss: () -> Unit,
    onSave: (Sighting) -> Unit
) {
    var nameText by remember { mutableStateOf(sighting.name) }
    var notesText by remember { mutableStateOf(sighting.notes) }
    var isFoundChecked by remember { mutableStateOf(sighting.isFound) }
    val context = LocalContext.current
    val fileUtils = remember { FileUtils(context) }
    var currentPhotoPath by remember { mutableStateOf(sighting.photoPath) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    //Launches a camera and updates the photo path when an image is captured
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            currentPhotoPath = tempPhotoUri.toString()
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
                } else {
                    // Show other features only when editing an existing sighting
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        label = { Text(stringResource(id = R.string.notes_hint)) },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isFoundChecked,
                            onCheckedChange = { isFoundChecked = it }
                        )
                        Text(text = stringResource(id = R.string.checkbox_found))
                    }
                    Button(
                        onClick = {
                            val file = fileUtils.createImageFile()
                            val uri = fileUtils.getUriForFile(file)
                            tempPhotoUri = uri
                            cameraLauncher.launch(uri)
                        }
                    ) {
                        Text(
                            text = if (currentPhotoPath == null)
                                stringResource(id = R.string.take_photo)
                            else
                                stringResource(id = R.string.retake_photo)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (nameText.isNotBlank()) {
                    onSave(
                        sighting.copy(
                            name = nameText,
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

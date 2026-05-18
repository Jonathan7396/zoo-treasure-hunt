package com.math0490.flinders.zootreasurehunt.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlin.math.abs
import kotlin.math.sqrt

@Composable
fun StepCounterCard() {
    val context = LocalContext.current
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    val stepSensor = remember {
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    }

    val accelerometer = remember {
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    val sharedPreferences = remember {
        context.getSharedPreferences("step_counter_prefs", Context.MODE_PRIVATE)
    }

    var hasPermission by remember {
        mutableStateOf(hasActivityRecognitionPermission(context))
    }

    var currentRawSteps by remember {
        mutableIntStateOf(sharedPreferences.getInt("current_raw_steps", -1))
    }

    var safariSteps by remember {
        mutableIntStateOf(sharedPreferences.getInt("safari_steps", 0))
    }

    var lastMagnitude by remember { mutableFloatStateOf(0f) }
    var lastStepTime by remember { mutableLongStateOf(0L) }

    val usingStepCounter = stepSensor != null && hasPermission
    val usingAccelerometerFallback = stepSensor == null && accelerometer != null
    val badgeInfo = getSafariBadgeInfo(safariSteps)

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    DisposableEffect(hasPermission, stepSensor, accelerometer) {
        val sensorToUse = when {
            usingStepCounter -> stepSensor
            usingAccelerometerFallback -> accelerometer
            else -> null
        }

        if (sensorToUse == null) {
            onDispose { }
        } else {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    event ?: return

                    if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                        val rawSteps = event.values.firstOrNull()?.toInt() ?: return
                        currentRawSteps = rawSteps

                        var baseline = sharedPreferences.getInt("step_baseline", -1)

                        if (baseline == -1) {
                            baseline = rawSteps
                            sharedPreferences.edit()
                                .putInt("step_baseline", baseline)
                                .apply()
                        }

                        val calculatedSteps = (rawSteps - baseline).coerceAtLeast(0)
                        safariSteps = calculatedSteps

                        sharedPreferences.edit()
                            .putInt("current_raw_steps", rawSteps)
                            .putInt("safari_steps", calculatedSteps)
                            .apply()
                    }

                    if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                        val x = event.values[0]
                        val y = event.values[1]
                        val z = event.values[2]

                        val magnitude = sqrt(x * x + y * y + z * z)
                        val movementChange = abs(magnitude - lastMagnitude)
                        val now = System.currentTimeMillis()

                        if (movementChange > 2.0f && now - lastStepTime > 250L) {
                            safariSteps += 1
                            lastStepTime = now

                            sharedPreferences.edit()
                                .putInt("safari_steps", safariSteps)
                                .apply()
                        }

                        lastMagnitude = magnitude
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }

            sensorManager.registerListener(
                listener,
                sensorToUse,
                SensorManager.SENSOR_DELAY_UI
            )

            onDispose {
                sensorManager.unregisterListener(listener)
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Safari Explorer Progress",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            when {
                stepSensor != null && !hasPermission -> {
                    Text(
                        text = "Allow step tracking to unlock safari explorer badges.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                            }
                        }
                    ) {
                        Text(text = "Allow Step Tracking")
                    }
                }

                usingStepCounter || usingAccelerometerFallback -> {
                    Text(
                        text = "Movement steps: $safariSteps",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Current badge: ${badgeInfo.currentBadge}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = badgeInfo.nextBadgeText,
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { badgeInfo.progress },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (currentRawSteps >= 0) {
                                sharedPreferences.edit()
                                    .putInt("step_baseline", currentRawSteps)
                                    .putInt("safari_steps", 0)
                                    .apply()
                            } else {
                                sharedPreferences.edit()
                                    .putInt("safari_steps", 0)
                                    .apply()
                            }

                            safariSteps = 0
                        }
                    ) {
                        Text(text = "Reset Safari Steps")
                    }
                }

                else -> {
                    Text(
                        text = "Movement tracking is not available on this device.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

private data class SafariBadgeInfo(
    val currentBadge: String,
    val nextBadgeText: String,
    val progress: Float
)

private fun hasActivityRecognitionPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}

private fun getSafariBadgeInfo(steps: Int): SafariBadgeInfo {
    return when {
        steps >= 1000 -> SafariBadgeInfo(
            currentBadge = "Zoo Champion",
            nextBadgeText = "Highest badge unlocked!",
            progress = 1f
        )

        steps >= 500 -> SafariBadgeInfo(
            currentBadge = "Safari Tracker",
            nextBadgeText = "Next badge: Zoo Champion at 1000 steps",
            progress = ((steps - 500).toFloat() / 500f).coerceIn(0f, 1f)
        )

        steps >= 100 -> SafariBadgeInfo(
            currentBadge = "Junior Explorer",
            nextBadgeText = "Next badge: Safari Tracker at 500 steps",
            progress = ((steps - 100).toFloat() / 400f).coerceIn(0f, 1f)
        )

        else -> SafariBadgeInfo(
            currentBadge = "New Explorer",
            nextBadgeText = "Next badge: Junior Explorer at 100 steps",
            progress = (steps.toFloat() / 100f).coerceIn(0f, 1f)
        )
    }
}
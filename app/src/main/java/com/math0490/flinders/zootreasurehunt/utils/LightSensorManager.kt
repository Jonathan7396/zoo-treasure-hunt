package com.math0490.flinders.zootreasurehunt.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

class LightSensorManager(context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    private val _isLowLight = mutableStateOf(false)
    val isLowLight: State<Boolean> = _isLowLight

    // Threshold for nocturnal
    private val NOCTURNAL_THRESHOLD = 10f

    private val lightListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                val lux = it.values[0]
                _isLowLight.value = lux < NOCTURNAL_THRESHOLD
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    fun startListening() {
        lightSensor?.let {
            sensorManager.registerListener(lightListener, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(lightListener)
    }
}

@Composable
fun rememberNocturnalMode(): Boolean {
    val context = LocalContext.current
    val manager = remember { LightSensorManager(context) }

    DisposableEffect(Unit) {
        manager.startListening()
        onDispose {
            manager.stopListening()
        }
    }

    return manager.isLowLight.value
}
package com.math0490.flinders.zootreasurehunt.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object LocationUtils {

    fun hasLocationPermission(context: Context): Boolean {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocationGranted || coarseLocationGranted
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): Location? {
        if (!hasLocationPermission(context)) {
            return null
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        return suspendCancellableCoroutine { continuation ->
            val cancellationTokenSource = CancellationTokenSource()

            fun resumeIfActive(location: Location?) {
                if (continuation.isActive) {
                    continuation.resume(location)
                }
            }

            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location ->
                if (location != null) {
                    resumeIfActive(location)
                } else {
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { lastLocation ->
                            resumeIfActive(lastLocation)
                        }
                        .addOnFailureListener {
                            resumeIfActive(null)
                        }
                }
            }.addOnFailureListener {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { lastLocation ->
                        resumeIfActive(lastLocation)
                    }
                    .addOnFailureListener {
                        resumeIfActive(null)
                    }
            }

            continuation.invokeOnCancellation {
                cancellationTokenSource.cancel()
            }
        }
    }

    fun distanceBetweenMeters(
        userLatitude: Double,
        userLongitude: Double,
        animalLatitude: Double,
        animalLongitude: Double
    ): Float {
        val results = FloatArray(1)

        Location.distanceBetween(
            userLatitude,
            userLongitude,
            animalLatitude,
            animalLongitude,
            results
        )

        return results[0]
    }
}
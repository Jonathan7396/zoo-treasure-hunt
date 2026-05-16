package com.math0490.flinders.zootreasurehunt.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Sighting(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    var isFound: Boolean = false,
    var notes: String = "",
    val imageUrl: String = "https://wilk0077.github.io/comp2012-images/assets-sm/african-lion-ai.jpg",
    val photoPath: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)
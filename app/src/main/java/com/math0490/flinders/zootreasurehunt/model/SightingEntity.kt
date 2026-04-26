package com.math0490.flinders.zootreasurehunt.model


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sightings")
data class SightingEntity(
    @PrimaryKey val id: String,
    val name: String,
    val imageUrl: String,
    val photoPath: String?,
    val isFound: Boolean,
    val notes: String
)
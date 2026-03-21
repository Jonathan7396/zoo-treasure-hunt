package com.example.zootreasurehunt
import java.util.UUID

data class Sighting(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    var isFound: Boolean = false,
    var notes: String = ""
)
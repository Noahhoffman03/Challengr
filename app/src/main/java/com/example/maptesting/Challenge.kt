package com.example.maptesting

import java.io.File

data class Challenge(
    val id: String = "",
    val creatorId: String = "",
    val title: String,
    val desc: String,
    val photo: String,
    val lat: Double,
    val lng: Double
)

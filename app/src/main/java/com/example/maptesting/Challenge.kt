package com.example.maptesting

import android.net.Uri
import java.io.File

data class Challenge(
    val id: String = "",
    val creatorId: String = "",
    val title: String,
    val desc: String,
    val photo: Uri?,
    val lat: Double,
    val lng: Double
)

package com.example.maptesting

data class User(
    val id: String = "",
    val username: String,
    val password: String,
    val email: String,
    val bio: String,
    val mainLocation: String,
    val completedChallenge: List<String> = mutableListOf()
    )

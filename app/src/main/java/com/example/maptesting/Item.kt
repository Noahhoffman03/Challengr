package com.example.maptesting

data class Item(
    val photo: String,
    val text: String,
    val desc: String,
    val creatorId: String,  //Added these cause we need them now
    val id: String
)

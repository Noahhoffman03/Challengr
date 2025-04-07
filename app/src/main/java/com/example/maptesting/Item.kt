package com.example.maptesting

data class Item(
    val image: Int,
    val text: String,
    val desc: String,
    val creatorId: String,  //Added these cause we need them now
    val id: String
)

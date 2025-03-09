// Rocket.kt
package com.example.spacex.model

data class Rocket(
    val id: String,
    val name: String?,
    val type: String?,
    val description: String?,
    val flickr_images: List<String>?
)

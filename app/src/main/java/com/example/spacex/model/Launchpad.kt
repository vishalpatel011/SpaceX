package com.example.spacex.model

data class Launchpad(
    val id: String,
    val name: String?,
    val full_name: String?,
    val locality: String?,
    val region: String?,
    val latitude: Double?,
    val longitude: Double?,
    val status: String?
)
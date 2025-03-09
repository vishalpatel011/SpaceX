package com.example.spacex.model

data class Launch(
    val id: String,
    val name: String?,
    val date_utc: String?,
    val rocket: String?,
    val success: Boolean?,
    val details: String?,
    val links: Links?,
    val upcoming: Boolean,
    val launchpad: String?
)

data class Links(
    val patch: Patch?,
    val webcast: String?,
    val article: String?,
    val wikipedia: String?
)

data class Patch(
    val small: String?,
    val large: String?
)

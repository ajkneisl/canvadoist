package dev.ajkneisl

import kotlinx.serialization.Serializable

@Serializable
data class Config(
    val canvasUrl: String? = null,
    val canvasToken: String? = null,
    val todoistToken: String? = null,
    val allowedCourses: List<Int>? = null
)

package dev.ajkneisl

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Course(
    val id: Int? = null,
    val name: String? = null,
    @SerialName("course_color") var courseColor: String? = null,
)

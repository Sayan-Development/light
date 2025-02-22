package org.sayandev.light.dependency

import kotlinx.serialization.Serializable

@Serializable
data class Version(
    val value: String
) {
    fun isSnapshot(): Boolean {
        return value.endsWith("-SNAPSHOT")
    }
}
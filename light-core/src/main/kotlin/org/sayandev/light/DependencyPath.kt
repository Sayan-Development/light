package org.sayandev.light

import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class DependencyPath(
    val path: String
) {
    fun file(relativeDirectory: File): File {
        return File(relativeDirectory, path)
    }
}
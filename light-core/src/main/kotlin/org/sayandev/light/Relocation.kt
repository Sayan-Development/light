package org.sayandev.light

import kotlinx.serialization.Serializable

@Serializable
data class Relocation(
    var pattern: String,
    var relocatedPattern: String,
    val includes: List<String> = emptyList(),
    val excludes: List<String> = emptyList()
) {
    init {
        this.pattern = pattern.replace("{}", ".")
        this.relocatedPattern = relocatedPattern.replace("{}", ".")
    }
}
package org.sayandev.repository

data class Version(
    val value: String
) {
    fun isSnapshot(): Boolean {
        return value.endsWith("-SNAPSHOT")
    }
}
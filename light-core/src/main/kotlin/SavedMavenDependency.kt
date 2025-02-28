package org.sayandev.light

import kotlinx.serialization.Serializable
import org.sayandev.light.dependency.dependencies.MavenDependency
import org.sayandev.light.repository.repositories.MavenRepository

@Serializable
data class SavedMavenDependency(
    val dependency: MavenDependency,
    val repository: MavenRepository,
    val path: DependencyPath,
    val relocate: Boolean,
)
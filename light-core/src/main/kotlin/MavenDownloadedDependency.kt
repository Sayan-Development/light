package org.sayandev.light

import org.sayandev.light.dependency.dependencies.MavenDependency
import org.sayandev.light.repository.repositories.MavenRepository
import java.io.File

data class MavenDownloadedDependency(
    val dependency: MavenDependency,
    val repository: MavenRepository,
    val file: File,
)
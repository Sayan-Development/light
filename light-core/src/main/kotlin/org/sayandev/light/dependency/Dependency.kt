package org.sayandev.light.dependency

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Deferred
import org.sayandev.LightClassLoader
import org.sayandev.light.Relocation
import org.sayandev.light.SavedMavenDependency
import org.sayandev.light.repository.repositories.MavenRepository
import java.io.File

interface Dependency {
    val group: String
    val artifact: String
    val version: Version
    val relocations: MutableList<Relocation>
    val isKotlinNative: Boolean

    fun saveData(asmDirectory: File, file: File, data: SavedMavenDependency): File

    fun dependencyDirectory(baseDirectory: File): File

    fun load(classLoader: LightClassLoader, file: File)

    fun resolveRepository(dispatcher: CoroutineDispatcher, repositories: List<MavenRepository>): Deferred<MavenRepository?>

    fun versionMetaURI(repository: MavenRepository): String
    fun downloadURI(repository: MavenRepository): String
    fun checksumURI(repository: MavenRepository): String

    fun addRelocation(relocation: Relocation) {
        relocations.add(relocation)
    }

    fun removeRelocation(relocation: Relocation) {
        relocations.remove(relocation)
    }
}
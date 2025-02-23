package org.sayandev.light.dependency

import kotlinx.coroutines.Deferred
import org.sayandev.light.repository.Repository
import org.sayandev.light.AsyncDispatcher
import java.io.File

interface Dependency {
    val group: String
    val artifact: String
    val version: Version
    val isKotlinNative: Boolean

    fun saveData(directory: File): File

    fun dependencyDirectory(baseDirectory: File): File

    fun load(file: File)

    fun resolveRepository(dispatcher: AsyncDispatcher, repositories: List<Repository>): Deferred<Repository?>

    fun versionMetaURI(repository: Repository): String
    fun downloadURI(repository: Repository): String
}
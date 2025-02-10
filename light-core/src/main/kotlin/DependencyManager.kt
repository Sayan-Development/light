package org.sayandev

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.awaitAll
import org.sayandev.repository.AsyncDownload
import org.sayandev.repository.Dependency
import org.sayandev.repository.Repository
import java.io.File

data class DependencyManager(
    val directory: File
) {
    val downloadedDependencies = mutableListOf<Dependency>()

    init {
        directory.mkdir()
    }

    val repositories = mutableListOf<Repository>()
    val dependencies = mutableListOf<Dependency>()

    fun addRepository(repository: Repository) {
        repositories.add(repository)
    }

    fun removeRepository(repository: Repository) {
        repositories.remove(repository)
    }

    fun addDependency(dependency: Dependency) {
        dependencies.add(dependency)
    }

    fun removeDependency(dependency: Dependency) {
        dependencies.remove(dependency)
    }

    fun downloadDependencies() {
        for (dependency in dependencies) {
            dependency.download(this, true, false)
        }
    }

    suspend fun downloadDependenciesAsync(): Deferred<List<File>> {
        return CompletableDeferred(dependencies.map { AsyncDownload.download(this, it) }.awaitAll())
    }

    fun loadDependencies() {
        val classLoader = ClassLoader.getSystemClassLoader()
        val method = classLoader::class.java.getDeclaredMethod("appendToClassPathForInstrumentation", String::class.java)
        method.isAccessible = true

        /*for (dependency in dependencies) {
            method.invoke(classLoader, dependency.file!!.path)
        }*/
        for (file in directory.walk().filter { it.isFile }) {
            method.invoke(classLoader, file.path)
        }
    }
}
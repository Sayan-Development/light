package org.sayandev

import org.sayandev.download.AsyncDownloadService
import org.sayandev.download.DownloadService
import org.sayandev.repository.Dependency
import org.sayandev.repository.Repository
import java.io.File

data class DependencyManager<T: DownloadService>(
    val directory: File,
    val downloadService: T
) {
    val downloadedDependencies = mutableListOf<Dependency>()

    init {
        directory.mkdir()

        if (downloadService is AsyncDownloadService) {
            downloadService.initializeDownloadTask(this) {

            }
        }
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
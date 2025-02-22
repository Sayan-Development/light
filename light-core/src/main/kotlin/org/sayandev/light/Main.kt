package org.sayandev.light

import org.sayandev.light.dependency.dependencies.MavenDependency
import RequiredTorches
import org.sayandev.light.repository.Repository
import org.sayandev.light.repository.repositories.MavenRepository
import org.sayandev.light.AsyncDispatcher
import java.io.File

fun main() {
    val downloadsDirectory = File("./downloads")

    RequiredTorches(downloadsDirectory)
        .download()
        .load()

    val repositories = listOf<Repository>(
        MavenRepository("sayandev-snapshots", "https://repo.sayandev.org/snapshots"),
        MavenRepository("sayandev-releases", "https://repo.sayandev.org/releases"),
    )

    val downloadDispatcher = AsyncDispatcher("downloader-thread", 5)
    val resolverDispatcher = AsyncDispatcher("resolver-thread", 5)

    val dependency = MavenDependency("org.sayandev", "stickynote-core", "1.8.9.92")
    CoroutineUtils.launch(AsyncDispatcher("light-main-thread", 1)) {
        println("Downloading ${dependency.group}:${dependency.artifact}:${dependency.version}")
        val dependencyRepository = dependency.resolveRepository(resolverDispatcher, repositories).await() ?: let {
            println("Failed to resolve repository for ${dependency.group}:${dependency.artifact}:${dependency.version}")
            return@launch
        }
        println("Downloading ${dependency.group}:${dependency.artifact}:${dependency.version} from ${dependencyRepository.name}")
        dependencyRepository.download(downloadsDirectory, downloadDispatcher, dependency).await()
        println("Downloaded ${dependency.group}:${dependency.artifact}:${dependency.version} from ${dependencyRepository.name}")
        dependency.saveData(downloadsDirectory)
    }


    /*val syncDownloadService = DownloadService.sync()
    val asyncDownloadService = DownloadService.async()

    val dependency = Dependency.maven("org.sayandev", "stickynote-core", "1.0.0", isKotlinDependency = true)

    val repositories = mutableListOf<Repository>()
    val snapshotRepository = Repository.maven("https://repo.sayandev.org/snapshots")
    val releaseRepository = Repository.maven("https://repo.sayandev.org/releases")
    repositories.add(snapshotRepository)
    repositories.add(releaseRepository)

    val dependencyRepository = dependency.findRepository(repositories)
    dependencyRepository.download(dependency).await()
    dependency.save(downloadsDirectory)
    dependency.load(downloadsDirectory)*/
}
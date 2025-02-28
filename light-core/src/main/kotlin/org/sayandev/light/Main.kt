package org.sayandev.light

import org.sayandev.light.dependency.Version
import org.sayandev.light.dependency.dependencies.MavenDependency
import org.sayandev.light.repository.repositories.MavenRepository
import java.io.File
import java.util.logging.Logger

fun main() {
    val downloadsDirectory = File("./downloads")

    val loader = KClassLoaderManager

    RequiredLights(downloadsDirectory)
        .download()
        .load(loader)

    val manager = DependencyManager(
        KClassLoaderManager,
        mainDispatcher = AsyncDispatcher("main-thread", 1),
        downloadDispatcher = AsyncDispatcher("downloader-thread", 5),
        loadDispatcher = AsyncDispatcher("loader-thread", 5),
        resolveDispatcher = AsyncDispatcher("resolver-thread", 5),
        baseDirectory = downloadsDirectory,
        logger = Logger.getAnonymousLogger(),
        true
    )

    manager.addRepository(MavenRepository("mavencentral", "https://repo1.maven.org/maven2/"))
    manager.addRepository(MavenRepository("sayandev-snapshots", "https://repo.sayandev.org/snapshots"))
    manager.addRepository(MavenRepository("sayandev-releases", "https://repo.sayandev.org/releases"))
    manager.addRepository(MavenRepository("trove", "https://maven.artifacts.atlassian.com/"))

    manager.addDependency(MavenDependency("org.sayandev", "stickynote-core", Version("1.8.9.92")).apply {
        this.addRelocation(Relocation("org.sayandev", "ir.syrent"))
    })

    CoroutineUtils.launch(AsyncDispatcher("light-main-thread", 1)) {
        val startTime = System.currentTimeMillis()
        println("downloading...")
        manager.downloadAll().await()
        println("loading...")
        manager.loadAll().await()
        println("saving...")
        manager.saveAll()
        val endTime = System.currentTimeMillis()

        println("finished. took: ${endTime - startTime}ms")
    }
}
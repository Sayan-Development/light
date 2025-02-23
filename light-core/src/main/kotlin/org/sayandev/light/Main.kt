package org.sayandev.light

import com.charleskorn.kaml.Yaml
import com.google.gson.JsonParser
import org.sayandev.Relocation
import org.sayandev.RelocationHelper
import org.sayandev.light.dependency.Version
import org.sayandev.light.dependency.dependencies.MavenDependency
import org.sayandev.light.repository.Repository
import org.sayandev.light.repository.repositories.MavenRepository
import java.io.File
import java.util.*
import kotlin.io.path.absolutePathString

fun main() {
    val downloadsDirectory = File("./downloads")

    RequiredTorches(downloadsDirectory)
        .download()
        .load()

    val repositories = listOf<Repository>(
        MavenRepository("mavencentral", "https://repo1.maven.org/maven2/"),
        MavenRepository("sayandev-snapshots", "https://repo.sayandev.org/snapshots"),
        MavenRepository("sayandev-releases", "https://repo.sayandev.org/releases"),
        MavenRepository("trove", "https://maven.artifacts.atlassian.com/"),
    )

    val downloadDispatcher = AsyncDispatcher("downloader-thread", 5)
    val resolverDispatcher = AsyncDispatcher("resolver-thread", 5)

    val dependency = MavenDependency("org.sayandev", "stickynote-core", Version("1.8.9.92"))
    val gson = MavenDependency("com.google.code.gson", "gson", Version("2.12.1"))
    CoroutineUtils.launch(AsyncDispatcher("light-main-thread", 1)) {
        println("Downloading ${dependency.group}:${dependency.artifact}:${dependency.version}")
        val dependencyRepository = dependency.resolveRepository(resolverDispatcher, repositories).await() ?: let {
            println("Failed to resolve repository for ${dependency.group}:${dependency.artifact}:${dependency.version}")
            return@launch
        }
        println("Downloading ${dependency.group}:${dependency.artifact}:${dependency.version} from ${dependencyRepository.name}")
        val dependencyFile = dependencyRepository.download(downloadsDirectory, downloadDispatcher, dependency).await()
        println("Downloaded ${dependency.group}:${dependency.artifact}:${dependency.version} from ${dependencyRepository.name}")
        val dataFile = dependency.saveData(dependencyFile)
        gson.apply {
            val repository = this.resolveRepository(resolverDispatcher, repositories).await()
            val gsonFile = repository!!.download(downloadsDirectory, downloadDispatcher, this).await()
            gson.load(gsonFile)
            println("example: ${JsonParser.parseString("{\"a\": 1}").asJsonObject}")
        }

        val result = Yaml.default.decodeFromString(MavenDependency.SavedMavenDependency.serializer(), dataFile.readText())
        val keintor = result.file.file(dataFile.parentFile)
        println("ke intor: ${keintor.absolutePath}")

        val out = File(dependencyFile.parentFile, "relocated.jar").toPath()
        val relocationHelper = RelocationHelper(downloadsDirectory)
        relocationHelper.relocate(dependencyFile.toPath(), out, mutableListOf(
            Relocation("org.sayandev", "ir.syrent")
        ))
        println("out: ${out.absolutePathString()}")
        KClassLoaderManager.load(out.toFile())
        val stateEnumClass = Class.forName("ir.syrent.stickynote.core.messaging.publisher.PayloadWrapper\$State")

        println(
            Class.forName("ir.syrent.stickynote.core.messaging.publisher.PayloadWrapper")
                .getConstructor(
                    UUID::class.java,
                    Any::class.java,
                    stateEnumClass,
                    String::class.java,
                    String::class.java,
                    Boolean::class.java
                )
                .newInstance(
                    UUID.randomUUID(),
                    "test",
                    stateEnumClass.getField("PROXY").get(null),
                    "test",
                    "test",
                    true
                ).toString()
        )
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
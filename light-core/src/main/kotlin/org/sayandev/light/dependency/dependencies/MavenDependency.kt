package org.sayandev.light.dependency.dependencies

import com.charleskorn.kaml.Yaml
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Deferred
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import org.sayandev.LightClassLoader
import org.sayandev.light.Relocation
import org.sayandev.light.*
import org.sayandev.light.dependency.Dependency
import org.sayandev.light.dependency.Version
import org.sayandev.light.repository.repositories.MavenRepository
import java.io.File

@Serializable
data class MavenDependency(
    override val group: String,
    override val artifact: String,
    override val version: Version,
    override val isKotlinNative: Boolean = false
) : Dependency {
    override val relocations = mutableListOf<Relocation>()

    fun getAndCreateRelocated(relocationHelper: RelocationHelper, file: File): File {
        return File(file.parentFile, "${file.nameWithoutExtension}-relocated.jar").apply {
            relocationHelper.relocate(file.toPath(), this.toPath(), relocations)
        }
    }

    override fun saveData(relocationHelper: RelocationHelper, baseDirectory: File, file: File, data: SavedMavenDependency): File {
        val dataFile = File(file.parentFile, "${file.nameWithoutExtension}.yml")
        println(dataFile.absolutePath)
        dataFile.writeText(Yaml.default.encodeToString(data))
        if (data.relocate) {
            getAndCreateRelocated(relocationHelper, file)
        }
        return dataFile
    }

    override fun dependencyDirectory(baseDirectory: File): File {
        return baseDirectory.resolve(File(group))
    }

    override fun load(classLoader: LightClassLoader, file: File) {
        classLoader.load(file)
    }

    override fun resolveRepository(resolverDispatcher: CoroutineDispatcher, repositories: List<MavenRepository>): Deferred<MavenRepository?> {
        val deferred = CompletableDeferred<MavenRepository?>()
        val toCheckRepositories = repositories.toMutableList()
        for (repository in repositories.sortedBy { it.uri.startsWith("file:/") }) {
            if (deferred.isCompleted) break
            CoroutineUtils.launch(resolverDispatcher) {
                if (repository.uri.startsWith("file:/")) {
                    val file = File(downloadURI(repository).replace("file:/", ""))
                    if (file.exists()) {
                        deferred.complete(repository)
                    }
                } else {
                    if (URLConnection(downloadURI(repository)).apply { this.open() }.isValid()) {
                        deferred.complete(repository)
                    }
                }
                toCheckRepositories.remove(repository)
                if (toCheckRepositories.isEmpty() && !deferred.isCompleted) {
                    deferred.complete(null)
                }
            }
        }
        return deferred
    }

    override fun versionMetaURI(repository: MavenRepository): String {
        return "${repository.uri.removeSuffix("/")}/${group.replace('.', '/')}/${artifact}/${version.value}/${artifact}-${version.value}.pom"
    }

    override fun downloadURI(repository: MavenRepository): String {
        return if (!isKotlinNative) "${repository.uri.removeSuffix("/")}/${group.replace(".", "/")}/${artifact}/${version.value}/${artifact}-${version.value}.jar"
        else "${repository.uri.removeSuffix("/")}/${group.replace(".", "/")}/${artifact}/${version.value}/${artifact}-${version.value}-jvm.jar"
    }

    override fun checksumURI(repository: MavenRepository): String {
        return "${downloadURI(repository)}.sha256"
    }

     companion object {
         fun getData(file: File): SavedMavenDependency? {
             return runCatching { Yaml.default.decodeFromString<SavedMavenDependency>(SavedMavenDependency.serializer(), file.readText()) }.getOrNull()
         }
     }
}
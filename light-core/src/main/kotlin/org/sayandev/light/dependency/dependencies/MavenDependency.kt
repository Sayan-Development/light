package org.sayandev.light.dependency.dependencies

import com.charleskorn.kaml.Yaml
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import org.sayandev.light.CoroutineUtils
import org.sayandev.light.DependencyPath
import org.sayandev.light.KClassLoaderManager
import org.sayandev.light.URLConnection
import org.sayandev.light.dependency.Dependency
import org.sayandev.light.dependency.Version
import org.sayandev.light.repository.Repository
import org.sayandev.light.AsyncDispatcher
import java.io.File

@Serializable
data class MavenDependency(
    override val group: String,
    override val artifact: String,
    override val version: Version,
    override val isKotlin: Boolean = false
) : Dependency {
    override fun saveData(file: File) {
        val file = File(file.parentFile, "${file.nameWithoutExtension}.yml")
        file.writeText(Yaml.default.encodeToString(SavedMavenDependency(this, DependencyPath(file.name))))
    }

    override fun dependencyDirectory(baseDirectory: File): File {
        return baseDirectory.resolve(File(group))
    }

    override fun load(file: File) {
        KClassLoaderManager.load(file)
    }

    override fun resolveRepository(resolverDispatcher: AsyncDispatcher, repositories: List<Repository>): Deferred<Repository?> {
        val deferred = CompletableDeferred<Repository?>()
        val toCheckRepositories = repositories.toMutableList()
        for (repository in repositories) {
            if (deferred.isCompleted) break
            CoroutineUtils.launch(resolverDispatcher) {
                if (URLConnection(downloadURI(repository)).apply { this.open() }.isValid()) {
                    deferred.complete(repository)
                }
                toCheckRepositories.remove(repository)
                if (toCheckRepositories.isEmpty() && !deferred.isCompleted) {
                    deferred.complete(null)
                }
            }
        }
        return deferred
    }

    override fun versionMetaURI(repository: Repository): String {
        return "${repository.uri}/${group.replace('.', '/')}/${artifact}/${version.value}/${artifact}-${version.value}.pom"
    }

    override fun downloadURI(repository: Repository): String {
        return if (!isKotlin) "${repository.uri}/${group.replace('.', '/')}/${artifact}/${version.value}/${artifact}-${version.value}.jar"
        else "${repository.uri}/${group.replace('.', '/')}/${artifact}/${version.value}/${artifact}-${version.value}-jvm.jar"
    }

    @Serializable
    data class SavedMavenDependency(
        val dependency: MavenDependency,
        val file: DependencyPath
    )
}
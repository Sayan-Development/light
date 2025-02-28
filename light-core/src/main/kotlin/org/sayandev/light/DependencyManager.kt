package org.sayandev.light

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import org.sayandev.LightClassLoader
import org.sayandev.light.dependency.dependencies.MavenDependency
import org.sayandev.light.repository.repositories.MavenRepository
import java.io.File
import java.util.logging.Logger
import kotlin.math.log

data class DependencyManager(
    val classLoader: LightClassLoader,
    private val mainDispatcher: CoroutineDispatcher,
    private val downloadDispatcher: CoroutineDispatcher,
    private val loadDispatcher: CoroutineDispatcher,
    private val resolveDispatcher: CoroutineDispatcher,
    val baseDirectory: File,
    val logger: Logger,
    val verifyChecksum: Boolean
) {
    val resolvedDependencies = mutableListOf<MavenDependency>()
    val dependencies = mutableListOf<MavenDependency>()
    val repositories = mutableListOf<MavenRepository>()
    val downloadedDependencies = mutableListOf<MavenDownloadedDependency>()

    val relocationHelper = RelocationHelper(baseDirectory)

    fun addRepository(repository: MavenRepository) {
        repositories.add(repository)
    }

    fun addDependency(dependency: MavenDependency) {
        dependencies.add(dependency)
    }

    suspend fun fetchDependencyRepositories(): Map<MavenDependency, MavenRepository?> {
        val resolvedRepositories = mutableMapOf<MavenDependency, Deferred<MavenRepository?>>()
        for (dependency in dependencies.filter { !resolvedDependencies.contains(it) }) {
            resolvedRepositories[dependency] = dependency.resolveRepository(resolveDispatcher, repositories)
        }
        resolvedRepositories.values.awaitAll()
        return resolvedRepositories.map { (dependency, repository) -> dependency to repository.await() }.toMap()
    }

    fun downloadAll(): Deferred<List<MavenDownloadedDependency>> {
        val filesDeferred = CompletableDeferred<List<MavenDownloadedDependency>>()
        val downloadsQueue = mutableMapOf<Pair<MavenDependency, MavenRepository>, Deferred<File>>()
        val deferred = mutableListOf<MavenDownloadedDependency>()
        // TODO: re-relocation if the relocations are mismatched
        CoroutineUtils.launch(downloadDispatcher) {
            for (dependencyFile in baseDirectory.walk().filter { it.isFile && it.extension == "yml" }) {
                val dependency = MavenDependency.getData(dependencyFile)
                if (dependency != null) {
                    if (verifyChecksum) {
                        val checksumConnection = URLConnection(dependency.dependency.checksumURI(dependency.repository)).openAndConnect()
                        if (checksumConnection.responseCode != 200) {
                            logger.severe("Failed to verify checksum from repository for ${dependency.dependency.group}:${dependency.dependency.artifact}:${dependency.dependency.version.value}")
                            continue
                        }
                        val checksum = checksumConnection.inputStream.readBytes()

                        if (dependency.path.getChecksum() != String(checksum)) {
                            logger.severe("checksum is mismatched for ${dependency.dependency.group}:${dependency.dependency.artifact}:${dependency.dependency.version.value} (expected: ${dependency.path.getChecksum()}, actual: ${String(checksum)})")
                            continue
                        }
                    }
                    resolvedDependencies.add(dependency.dependency)
                    deferred.add(MavenDownloadedDependency(dependency.dependency, dependency.repository, dependency.path.file()))
                }
            }
            for ((dependency, repository) in fetchDependencyRepositories()) {
                if (repository == null) {
                    logger.severe("Failed to resolve repository for dependency $dependency")
                    continue
                }
                logger.info("Downloading dependency $dependency from repository $repository")
                // TODO: think of something to do this non-blocking
                downloadsQueue[dependency to repository] = repository.download(baseDirectory, downloadDispatcher, dependency).apply {
                    this.invokeOnCompletion {
                        logger.info("Downloaded dependency $dependency from repository $repository")
                    }
                }
            }
            downloadsQueue.values.awaitAll()
            for (downloadQueue in downloadsQueue) {
                // TODO: create a dataclass for downloadQueue map
                val dependency = downloadQueue.key.first
                val repository = downloadQueue.key.second
                val file = downloadQueue.value
                deferred.add(MavenDownloadedDependency(dependency, repository, file.await()))
            }
            downloadedDependencies.addAll(deferred)
            filesDeferred.complete(deferred)
        }
        return filesDeferred
    }

    fun loadAll(): Deferred<Unit> {
        val deferred = CompletableDeferred<Unit>()

        for (dependency in downloadedDependencies) {
            logger.info("Loading dependency $dependency")
            dependency.dependency.load(classLoader, if (dependency.dependency.relocations.isNotEmpty()) dependency.dependency.getAndCreateRelocated(relocationHelper, dependency.file) else dependency.file)
        }
        deferred.complete(Unit)

        return deferred
    }

    fun saveAll(): CompletableDeferred<Unit> {
        val deferred = CompletableDeferred<Unit>()
        val tempDependencies = downloadedDependencies.toMutableList()
        for (dependency in downloadedDependencies) {
            CoroutineUtils.launch(resolveDispatcher) {
                dependency.dependency.saveData(relocationHelper, baseDirectory, dependency.file, SavedMavenDependency(
                    dependency.dependency,
                    dependency.repository,
                    DependencyPath(dependency.file.path),
                    dependency.dependency.relocations.isNotEmpty()
                ))
                tempDependencies.remove(dependency)
                println("dependencies: ${tempDependencies.joinToString(", ") { it.dependency.artifact }}")
                // TODO: this doesn't always work, sometimes one dependency is left on the list. race condition or something
                if (tempDependencies.isEmpty()) {
                    deferred.complete(Unit)
                }
            }
        }
        return deferred
    }
}
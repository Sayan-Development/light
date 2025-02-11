package org.sayandev.repository

import org.sayandev.CoroutineUtils
import org.sayandev.DependencyManager
import org.sayandev.StreamUtils.output
import org.sayandev.URLConnection
import org.sayandev.download.AsyncDownloadService
import org.sayandev.download.SimpleDownloadService
import org.sayandev.xml.XMLReader
import org.w3c.dom.Element
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.charset.StandardCharsets

data class MavenDependency(
    override val group: String,
    override val artifact: String,
    override val version: Version
): Dependency {
    override fun download(manager: DependencyManager<*>, kotlinLibrary: Boolean, includeTransitives: Boolean): File {
        println("starting download for ${this.artifact}")
        val downloadDirectory = File(manager.directory, group).also { it.mkdirs() }
        val file = File(downloadDirectory, "${artifact}-${version.value}.jar")
        /*if (!includeTransitives && file.exists()) {
            println("skipping download of ${this.artifact} as it already exists")
            return file
        }*/
        if (file.exists()) {
            return file
        }

        for (repository in manager.repositories) {
            val url = if (kotlinLibrary) jvmDownloadURI(repository, version) else downloadURI(repository, version)
            val fallbackURL = if (kotlinLibrary) downloadURI(repository, version) else null
            when (manager.downloadService) {
                is AsyncDownloadService -> {
                    CoroutineUtils.launch(manager.downloadService.dispatcher) {
                        manager.downloadService.fetch(this@MavenDependency, url, fallbackURL)
                    }
                }
                is SimpleDownloadService -> {
                    manager.downloadService.fetch(url, fallbackURL)
                }
            }
        }

        if (includeTransitives) {
            transitiveDependencies(manager).forEach { (dependency, repository) ->
                val url = if (kotlinLibrary) jvmDownloadURI(repository, version) else downloadURI(repository, version)
                val fallbackURL = if (kotlinLibrary) downloadURI(repository, version) else null
                when (manager.downloadService) {
                    is AsyncDownloadService -> {
                        manager.downloadService.fetch(dependency, url, fallbackURL)
                    }
                    is SimpleDownloadService -> {
                        manager.downloadService.fetch(url, fallbackURL)
                    }
                }
            }
        }

        return file
    }

    fun transitiveDependencies(manager: DependencyManager<*>): Map<MavenDependency, Repository> {
        return buildMap {
            when (manager.downloadService) {
                is AsyncDownloadService -> {
                    for (repository in manager.repositories) {
                        CoroutineUtils.launch(manager.downloadService.dispatcher) {
                            val depenndencies = fetchTransitivesFromRepository(manager, repository)
                            for (dependency in depenndencies) {
                                put(dependency, repository)
                            }
                        }
                    }
                }
                is SimpleDownloadService -> {
                    for (repository in manager.repositories) {
                        val dependencies = fetchTransitivesFromRepository(manager, repository)
                        for (dependency in dependencies) {
                            put(dependency, repository)
                        }
                    }
                }
            }
        }
    }

    fun fetchTransitivesFromRepository(manager: DependencyManager<*>, repository: Repository): List<MavenDependency> {
        val uri = versionMetaURI(repository, version)
        val url = URLConnection(uri)
        val connection = url.openAndConnect()
        if (!url.isValid()) return emptyList()
        val result = XMLReader(ByteArrayInputStream(connection.inputStream.output().toByteArray(StandardCharsets.UTF_8))).read()
        val dependencies = result.getElementsByTagName("dependency")
        return buildList {
            for (i in 0 until dependencies.length) {
                val transitiveDependency = dependencies.item(i) as Element
                val scope = transitiveDependency.getElementsByTagName("scope").item(0)?.textContent
                var groupId = transitiveDependency.getElementsByTagName("groupId").item(0)?.textContent ?: continue
                if (groupId.contains("\$")) {
                    groupId = this@MavenDependency.group
                }
                val artifactId = transitiveDependency.getElementsByTagName("artifactId").item(0)?.textContent ?: continue
                if (scope != "compile" && scope != "runtime" && scope != null) {
                    println("ignored transitive with artifact: ${artifactId} and scope: ${scope}")
                    continue
                }
                var version = transitiveDependency.getElementsByTagName("version").item(0)?.textContent ?: continue
                if (version.contains("\$")) {
                    version = this@MavenDependency.version.value
                }
                if (!group.contains("\$") && !version.contains("\$")) {
                    val dependency = MavenDependency(groupId, artifactId, Version(version))
                    add(dependency)
                } else {
                    println("found broken dependency at url ${uri} with artifact: ${artifactId} and version: ${version} and group: ${groupId}")
                }
            }
        }
    }

    override fun versionMetaURI(repository: Repository, version: Version): String {
        return "${repository.url}/${group.replace('.', '/')}/${artifact}/${version.value}/${artifact}-${version.value}.pom"
    }

    override fun downloadURI(repository: Repository, version: Version): String {
        return "${repository.url}/${group.replace('.', '/')}/${artifact}/${version.value}/${artifact}-${version.value}.jar"
    }

    override fun jvmDownloadURI(repository: Repository, version: Version): String {
        return "${repository.url}/${group.replace('.', '/')}/${artifact}/${version.value}/${artifact}-${version.value}-jvm.jar"
    }
}
package org.sayandev.repository

import org.sayandev.CoroutineUtils
import org.sayandev.DependencyManager
import org.sayandev.StreamUtils.output
import org.sayandev.URLConnection
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
    override var file: File? = null

    override fun download(manager: DependencyManager, includeTransitives: Boolean, async: Boolean): File {
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
            var url = URLConnection(downloadURI(repository, version)).also { it.open() }
            if (!url.isValid()) continue
            URLConnection(jvmDownloadURI(repository, version)).apply {
                this.open()
                if (url.isValid()) url = this
            }

            url.connect()

            if (!file.exists()) {
                if (async) {
                    AsyncDownload.download(manager, this)
                }
                repository.download(this, downloadDirectory)
                manager.downloadedDependencies.add(this)
                println("downloaded ${this.artifact} from ${url}")
            }

            this.file = file
            break
        }

        if (includeTransitives) {
            CoroutineUtils.launch(AsyncDownload.dispatcher) {
                transitiveDependencies(manager).forEach {
                    it.download(manager, true, async)
                }
            }
        }

        return file
    }

    fun transitiveDependencies(manager: DependencyManager): List<MavenDependency> {
        return buildList {
            for (repository in manager.repositories) {
                val uri = versionMetaURI(repository, version)
                val url = URLConnection(uri)
                val connection = url.openAndConnect()
                if (!url.isValid()) continue
                val result = XMLReader(ByteArrayInputStream(connection.inputStream.output().toByteArray(StandardCharsets.UTF_8))).read()
                val dependencies = result.getElementsByTagName("dependency")
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
                        if (!manager.downloadedDependencies.contains(dependency)) {
                            this.add(dependency)
                            manager.downloadedDependencies.add(dependency)
                        } else {
                            println("skipping download of ${dependency.artifact} : ${groupId} as it already exists at")
                        }
                    } else {
                        println("found broken dependency at url ${uri} with artifact: ${artifactId} and version: ${version} and group: ${groupId}")
                    }
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
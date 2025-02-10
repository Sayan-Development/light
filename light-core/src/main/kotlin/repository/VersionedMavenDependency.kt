package org.sayandev.repository

import org.sayandev.StreamUtils.output
import org.sayandev.URLConnection
import org.sayandev.xml.XMLReader
import org.w3c.dom.Element
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.charset.StandardCharsets

data class VersionedMavenDependency(
    val dependency: MavenDependency,
    val version: Version
) {
    fun download(repository: Repository, directory: File) {
        repository.download(dependency, version, directory)
    }

    fun transitiveDependencies(repository: Repository): List<VersionedMavenDependency> {
        val uri = dependency.versionMetaURI(repository, version)
        val connection = URLConnection(uri).openAndConnect()
        val result = XMLReader(ByteArrayInputStream(connection.inputStream.output().toByteArray(StandardCharsets.UTF_8))).read()
        val dependencies = result.getElementsByTagName("dependency")
        return buildList {
            for (i in 0 until dependencies.length) {
                val transitiveDependency = dependencies.item(i) as Element
                val groupId = transitiveDependency.getElementsByTagName("groupId").item(0).textContent
                val artifactId = transitiveDependency.getElementsByTagName("artifactId").item(0).textContent
                val version = transitiveDependency.getElementsByTagName("version").item(0).textContent
                add(VersionedMavenDependency(MavenDependency(groupId, artifactId), Version(version)))
            }
        }
    }
}
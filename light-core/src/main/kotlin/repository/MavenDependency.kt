package org.sayandev.repository

import org.sayandev.StreamUtils.output
import org.sayandev.URLConnection
import org.sayandev.xml.XMLReader
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

data class MavenDependency(
    override val group: String,
    override val artifact: String,
): Dependency {
    override fun versions(repository: Repository): List<Version> {
        val connection = URLConnection(metaURI(repository)).openAndConnect()
        val result = connection.inputStream.output()

        val document = XMLReader(ByteArrayInputStream(result.toByteArray(StandardCharsets.UTF_8))).read()

        return buildList {
            val versionNodes = document.getElementsByTagName("version")
            for (i in 0 until versionNodes.length) {
                add(Version(versionNodes.item(i).textContent))
            }
        }
    }

    override fun lastVersion(repository: Repository): Version {
        val connection = URLConnection(metaURI(repository)).openAndConnect()
        val result = connection.inputStream.output()

        val document = XMLReader(ByteArrayInputStream(result.toByteArray(StandardCharsets.UTF_8))).read()

        return Version(document.getElementsByTagName("latest").item(0).textContent)
    }

    override fun lastReleaseVersion(repository: Repository): Version {
        val connection = URLConnection(metaURI(repository)).openAndConnect()
        val result = connection.inputStream.output()

        val document = XMLReader(ByteArrayInputStream(result.toByteArray(StandardCharsets.UTF_8))).read()

        return Version(document.getElementsByTagName("release").item(0).textContent)
    }

    override fun metaURI(repository: Repository): String {
        return "${repository.url}/${group.replace('.', '/')}/${artifact}/maven-metadata.xml"
    }

    override fun versionMetaURI(repository: Repository, version: Version): String {
        return "${repository.url}/${group.replace('.', '/')}/${artifact}/${version.value}/${artifact}-${version.value}.pom"
    }

    override fun downloadURI(repository: Repository, version: Version): String {
        return "${repository.url}/${group.replace('.', '/')}/${artifact}/${version.value}/${artifact}-${version.value}.jar"
    }
}
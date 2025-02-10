package org.sayandev.repository

import org.sayandev.URLConnection
import java.io.File


data class MavenRepository(
    override val url: String
): Repository {
    override fun download(dependency: Dependency, directory: File) {
        val downloadURI = dependency.downloadURI(this, dependency.version)
        val connection = URLConnection(downloadURI).openAndConnect()
        val file = File(directory, "${dependency.artifact}-${dependency.version.value}.jar")
        if (file.exists()) return
        connection.inputStream.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }
}
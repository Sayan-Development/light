package org.sayandev.light.repository.repositories

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.serialization.Serializable
import org.sayandev.light.CoroutineUtils
import org.sayandev.light.URLConnection
import org.sayandev.light.dependency.Dependency
import org.sayandev.light.repository.Repository
import org.sayandev.light.AsyncDispatcher
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File

@Serializable
data class MavenRepository(
    override val name: String,
    override val uri: String
) : Repository {
    constructor(uri: String) : this(uri, uri)

    override fun download(directory: File, dispatcher: AsyncDispatcher, dependency: Dependency): Deferred<File> {
        val deferred = CompletableDeferred<File>()
        val downloadURI = dependency.downloadURI(this)
        CoroutineUtils.launch(dispatcher) {
            val connection = URLConnection(downloadURI).open()

            connection.connect()
            val buffer = ByteArrayOutputStream()
            connection.inputStream.use { it.copyTo(buffer) }
            val byteArrayInputStream = ByteArrayInputStream(buffer.toByteArray())
            val dependencyDirectory = dependency.dependencyDirectory(directory).also { it.mkdirs() }
            deferred.complete(dependencyDirectory.resolve("${dependency.artifact}-${dependency.version.value}.jar").apply {
                outputStream().use { byteArrayInputStream.copyTo(it) }
            })
            connection.disconnect()
        }
        return deferred
    }
}
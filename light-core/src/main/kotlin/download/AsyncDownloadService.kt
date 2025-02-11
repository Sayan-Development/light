package org.sayandev.download

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import org.sayandev.AsyncDispatcher
import org.sayandev.CoroutineUtils
import org.sayandev.DependencyManager
import org.sayandev.URLConnection
import org.sayandev.repository.Dependency
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream

class AsyncDownloadService(threadCount: Int) : DownloadService {
    val dispatcher = AsyncDispatcher("light-download", threadCount)

    val fetchQueue = mutableListOf<CompletableDeferred<FetchData?>>()
    val downloadQueue = mutableListOf<CompletableDeferred<File>>()

    fun initializeDownloadTask(manager: DependencyManager<AsyncDownloadService>, onDownload: (File) -> Unit) {
        CoroutineUtils.launch(dispatcher) {
            while (true) {
                for (fetchRequest in fetchQueue) {
                    CoroutineUtils.launch(dispatcher) {
                        val fetchData = fetchRequest.await() ?: return@launch
                        val fileName = fetchData.uri.split("/").last()
                        onDownload(download(fetchData.inputStream, File(File(manager.directory, fetchData.dependency.group).also { it.mkdirs() }, fileName)).await())
                    }
                }
            }
        }
    }

    fun fetch(dependency: Dependency, uri: String, fallbackURI: String?): CompletableDeferred<FetchData?> {
        val deferred = CompletableDeferred<FetchData?>()
        fetchQueue.add(deferred)
        println("fetching ${uri} with fallback ${fallbackURI}")
        CoroutineUtils.launch(dispatcher) {
            val connection = let {
                val connection = URLConnection(uri).open()
                if (connection.responseCode != 200 && fallbackURI != null) {
                    connection.disconnect()
                    val fallbackConnection = URLConnection(fallbackURI).open()
                    if (fallbackConnection.responseCode != 200) {
                        fallbackConnection.disconnect()
                        null
                    } else fallbackConnection
                } else connection
            }

            if (connection != null) {
                connection.connect()
                val buffer = ByteArrayOutputStream()
                connection.inputStream.use { it.copyTo(buffer) }
                val byteArrayInputStream = ByteArrayInputStream(buffer.toByteArray())
                deferred.complete(FetchData(dependency, connection.url.path, byteArrayInputStream))
                connection.disconnect()
            } else {
                fetchQueue.remove(deferred)
            }
        }
        return deferred
    }

    fun download(inputStream: InputStream, file: File): Deferred<File> {
        val deferred = CompletableDeferred<File>()
        downloadQueue.add(deferred)
        CoroutineUtils.launch(dispatcher) {
            file.outputStream().use { inputStream.copyTo(it) }
            deferred.complete(file)
        }
        return deferred
    }

    fun download(manager: DependencyManager<AsyncDownloadService>, dependency: Dependency): CompletableDeferred<File> {
        val deferred = CompletableDeferred<File>()
        CoroutineUtils.launch(dispatcher) {
            println("downloading ${dependency.artifact}")
            deferred.complete(dependency.download(manager, true, true))
        }
        return deferred
    }
}
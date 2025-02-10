package org.sayandev.repository

import kotlinx.coroutines.CompletableDeferred
import org.sayandev.AsyncDispatcher
import org.sayandev.CoroutineUtils
import org.sayandev.DependencyManager
import java.io.File

object AsyncDownload {
    val dispatcher = AsyncDispatcher("light-download", 20)

    fun download(manager: DependencyManager, dependency: Dependency): CompletableDeferred<File> {
        val deferred = CompletableDeferred<File>()
        CoroutineUtils.launch(dispatcher) {
            println("downloading ${dependency.artifact}")
            deferred.complete(dependency.download(manager, true, true))
        }
        return deferred
    }
}
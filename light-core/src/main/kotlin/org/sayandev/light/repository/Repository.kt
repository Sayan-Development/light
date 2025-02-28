package org.sayandev.light.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Deferred
import org.sayandev.light.dependency.Dependency
import java.io.File

interface Repository {
    val name: String
    val uri: String

    fun download(directory: File, dispatcher: CoroutineDispatcher, dependency: Dependency): Deferred<File>
}